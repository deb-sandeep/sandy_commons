package com.sandy.common.util;

import org.apache.commons.beanutils.BeanUtilsBean ;
import org.apache.commons.cli.CommandLine ;
import org.apache.commons.cli.DefaultParser ;
import org.apache.commons.cli.HelpFormatter ;
import org.apache.commons.cli.MissingOptionException ;
import org.apache.commons.cli.Option ;
import org.apache.commons.cli.Options ;
import org.apache.log4j.Logger ;

public abstract class AbstractCLParser {

    private static Logger logger = Logger.getLogger( AbstractCLParser.class ) ;
    
    protected static class OptionCfgCollection {
        
        private Options options = null ;
        
        public OptionCfgCollection( Options options ) {
            this.options = options ;
        }
        
        public void addOption( String shortName, String longName, 
                               boolean isRequired, boolean hasArgument,
                               String description ) {
            
            if( StringUtil.isEmptyOrNull( longName ) ) {
                throw new IllegalArgumentException( "Option long name can't be null." ) ;
            }
            
            Option option = new Option( shortName, longName, hasArgument, description ) ;
            option.setRequired( isRequired ) ;
            options.addOption( option ) ;
        }
    }
    
    private BeanUtilsBean beanUtils = BeanUtilsBean.getInstance() ;
    private Options clOptions = new Options() ;
    
    public AbstractCLParser() {
        prepareOptions( new OptionCfgCollection( this.clOptions ) ) ;
    }
    
    protected abstract void prepareOptions( OptionCfgCollection options ) ;
    
    protected abstract String getUsageString() ;
    
    public void printUsage() {
        
        String usageStr = getUsageString() ;
        
        HelpFormatter helpFormatter = new HelpFormatter() ;
        helpFormatter.printHelp( 80, usageStr, null, this.clOptions, null ) ;
    }

    public void parse( String[] args ) throws Exception {

        try {
            CommandLine cmdLine = new DefaultParser().parse( this.clOptions, args ) ;
            for( Option opt : this.clOptions.getOptions() ) {
                processOption( opt, cmdLine ) ;
            }
        }
        catch ( MissingOptionException me ) {
            logger.error( me.getMessage() ) ;
            printUsage() ;
            throw new Exception( me.getMessage() ) ;
        }
        catch ( Exception e ) {
            logger.error( "Error parsing command line arguments.", e ) ;
            printUsage() ;
            throw e ;
        }
    }
    
    private void processOption( Option opt, CommandLine cmdLine )
        throws Exception {
        
        String key = opt.getLongOpt() ;
        
        if( opt.isRequired() ) {
            if( !cmdLine.hasOption( key ) ) {
                throw new Exception( "Mandatory option " + key + 
                                     " not set" ) ;
            }
        }
        
        if( cmdLine.hasOption( key ) ) {
            if( !opt.hasArg() ) {
                setProperty( key, true ) ;
            }
            else {
                String value = cmdLine.getOptionValue( key ) ;
                if( !opt.hasOptionalArg() && value == null ) {
                    throw new Exception( "Arguments not specified for " + 
                                         "option " + key ) ;
                }
                else {
                    setProperty( key, value ) ;
                }
            }
        }
    }
    
    private void setProperty( String key, Object value )
        throws Exception {
        beanUtils.setProperty( this, key, value ) ;
    }
}

