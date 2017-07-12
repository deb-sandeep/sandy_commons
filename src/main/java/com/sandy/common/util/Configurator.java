package com.sandy.common.util;

import java.net.URL ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;
import java.util.Map.Entry ;

import org.apache.commons.beanutils.BeanUtilsBean ;
import org.apache.commons.configuration.PropertiesConfiguration ;
import org.apache.log4j.Logger ;

public class Configurator {

    private static final Logger logger = Logger.getLogger( Configurator.class ) ;
    
    public static final String IGNORE_ON_CONFLICT    = "IGNORE" ;
    public static final String OVERIDE_ON_CONFLICT   = "OVERRIDE" ;
    public static final String EXCEPTION_ON_CONFLICT = "EXCEPTION" ;
    
    private String confictResolutionStrategy = OVERIDE_ON_CONFLICT ;
    
    private Map<String, Object> configurableObjects  = new HashMap<String, Object>() ;
    private List<URL>           configResourceURLs   = new ArrayList<URL>() ;
    private AbstractCLParser    cmdLine              = null ;
    
    public void setCommandLine( AbstractCLParser cmdLine ) {
        this.cmdLine = cmdLine ;
    }
    
    public String getConflictResolutionStrategy() {
        return this.confictResolutionStrategy ;
    }
    
    public void setConflictResolutionStrategy( String strategy ) {
        this.confictResolutionStrategy = strategy ;
    }
    
    public void registerConfigurableObjects( Map<String, Object> configObjects ) 
        throws Exception {
        
        for( Entry<String, Object> entry : configObjects.entrySet() ) {
            registerConfigurableObject( entry.getKey(), entry.getValue() ) ;
        }
    }
    
    public void registerConfigurableObject( String key, Object obj ) 
        throws Exception {
        
        if( this.configurableObjects.containsKey( key ) ) {
            String msg = "Object is already bound against " + key + "." ;
            switch( confictResolutionStrategy ) {
                
                case IGNORE_ON_CONFLICT:
                    logger.debug( msg + " Ignoring" ) ;
                    break ;
                    
                case OVERIDE_ON_CONFLICT:
                    logger.warn( msg + " Overriding." ) ; 
                    this.configurableObjects.put( key, obj ) ;
                    break ;
                    
                case EXCEPTION_ON_CONFLICT:
                    logger.error( msg + " Error." ) ; 
                    throw new Exception( msg ) ;
            }
        }
        else {
            this.configurableObjects.put( key, obj ) ;
        }
    }
    
    public Object getConfigurableObject( String key ) {
        return this.configurableObjects.get( key ) ;
    }
    
    public void registerConfigResourceURLs( List<URL> resourceURLs ) {
        this.configResourceURLs.addAll( resourceURLs ) ;
    }
    
    public void registerConfigResourceURL( URL resourceURL ) {
        this.configResourceURLs.add( resourceURL ) ;
    }
    
    public void initialize() throws Exception {
        
        PropertiesConfiguration configProperties = new PropertiesConfiguration() ;
        BeanUtilsBean           beanUtils        = new BeanUtilsBean() ;
        
        for( int i=configResourceURLs.size()-1; i>=0; i-- ) {
        	URL url = configResourceURLs.get( i ) ;
            configProperties.load( url ) ;
        }
        
        for( Iterator<?> keyIterator = configProperties.getKeys(); keyIterator.hasNext(); ) {
            
            String key   = ( String )keyIterator.next() ;
            String value = configProperties.getString( key ) ;
            
            String objKey = key.substring( 0, key.indexOf( '.' ) ) ;
            String path   = key.substring( key.indexOf( '.' ) ) ;
            
            String qualifiedPath = "configurableObject(" + objKey + ")" + path ;
            
            if( value.startsWith( "clp:" ) ) {
                String clpKey = value.substring( "clp:".length() ) ;
                value = beanUtils.getProperty( cmdLine, clpKey ) ;
            }
            
            beanUtils.setProperty( this, qualifiedPath, value ) ;
        }
    }
}