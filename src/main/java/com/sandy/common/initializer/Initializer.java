package com.sandy.common.initializer;

import java.net.URL;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanWrapperImpl;

import com.sandy.common.contracts.Initializable ;
import com.sandy.common.util.ReflectionUtil ;

public class Initializer {

    private static final Logger logger = Logger.getLogger( Initializer.class ) ;

    public static final String DEF_INITIALIZER_CONFIG  = "/initializer-config.xml" ;

    private final BeanWrapperImpl beanWrapper = new BeanWrapperImpl() ;

    public Initializer() {
        super() ;
    }

    public void initialize() throws InitializationException {

        final URL cfgURL = Initializer.class.getResource( DEF_INITIALIZER_CONFIG ) ;
        initialize( cfgURL ) ;
    }

    public void initialize( final URL cfgURL )
        throws InitializationException {

        if( cfgURL == null ) {
            final String msg = "Initializer configuration could not be found." ;
            logger.error( msg ) ;
            throw new InitializationException( msg ) ;
        }

        logger.debug( "Initializing from URL " + cfgURL.toExternalForm() ) ;

        try {
            final XMLConfiguration config = new XMLConfiguration();
            final URL dtdURL = ReflectionUtil.getResource( Initializer.class, 
                                                           "initializer-config.dtd" ) ;

            config.registerEntityId( "-//Initializer//DTD 1.0//EN", dtdURL );
            config.setValidating( true ) ;
            config.setURL( cfgURL ) ;
            config.load();

            initBootstrapElements( config ) ;
        }
        catch ( final Exception e ) {
            InitializationException.wrapAndThrow( e, 
                                 "Initialization failure :" + e.getMessage() ) ;
        }
    }

    private void initBootstrapElements( final XMLConfiguration config )
        throws InitializationException {

        HierarchicalConfiguration cfg = null ;
        final int numElements = config.getList( "initializable[@class]" ).size() ;

        for( int i=0; i<numElements; i++ ) {

            cfg = config.configurationAt( "initializable(" + i + ")" ) ;
            initElement( cfg ) ;
        }
    }

    private void initElement( final HierarchicalConfiguration config )
        throws InitializationException {

        String                    className = null ;
        String                    propName  = null ;
        Object                    propVal   = null ;
        HierarchicalConfiguration propCfg   = null ;
        Initializable             element   = null ;

        try {
            className = config.getString( "[@class]" ) ;
            element   = ( Initializable )ReflectionUtil.createInstance( className.trim() ) ;

            this.beanWrapper.setWrappedInstance( element ) ;
            final int numProps = config.getList( "property[@name]" ).size() ;
            for( int i=0; i<numProps; i++ ) {
                propCfg  = config.configurationAt( "property(" + i + ")" ) ;
                propName = propCfg.getString( "[@name]" ) ;
                propVal  = propCfg.getProperty( "[@value]" ) ;
                if( propVal == null ) {
                    propVal = propCfg.getList( "value" ) ;
                }
                this.beanWrapper.setPropertyValue( propName, propVal ) ;
            }

            element.initialize() ;
        }
        catch ( final Exception e ) {
            throw new InitializationException( 
                   "Initializable " + className + " could not be created", e ) ;
        }
    }
}
