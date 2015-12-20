package com.sandy.common.objfactory ;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.springframework.beans.BeansException ;
import org.springframework.beans.factory.NoSuchBeanDefinitionException ;
import org.springframework.context.ApplicationContext ;
import org.springframework.context.support.GenericXmlApplicationContext ;

import com.sandy.common.contracts.Initializable ;

/**
 * Loads a consolidated application context from multiple resource paths.
 * The resource paths need to be qualified as per the following guidelines:
 * 
 * Prefix     Example                        Description
 * -----------------------------------------------------------------------------
 * classpath: classpath:com/myapp/config.xml Loaded from the classpath.
 * file:      file:/data/config.xml          Loaded as a URL, from the filesystem.
 * http:      http://myserver/logo.png       Loaded as a URL.
 * 
 */
public class SpringObjectFactory implements Initializable {

    private static final Logger logger = Logger.getLogger( SpringObjectFactory.class ) ;
    
    private GenericXmlApplicationContext appCtx = new GenericXmlApplicationContext() ;
    
    private List<String> resourcePaths = new ArrayList<>() ;
    
    public List<String> getResourcePaths() {
        return resourcePaths;
    }
    
    public void addResourcePath( String path ) {
        this.resourcePaths.add( path ) ;
    }

    public void setResourcePaths( List<String> resourcePaths ) {
        this.resourcePaths = resourcePaths;
    }

    public void initialize() throws Exception {
        if( this.resourcePaths == null || this.resourcePaths.isEmpty() ) {
            return ;
        }
        
        for( String path : this.resourcePaths ) {
            logger.debug( "Loading spring context from " + path ) ;
            this.appCtx.load( path );
        }
        this.appCtx.refresh() ;
    }

    public ApplicationContext getApplicationContext() {
        return this.appCtx ;
    }
    
    public Object getBean( String name, Class<?> requiredType )
            throws BeansException {
        
        return appCtx.getBean( name, requiredType ) ;
    }

    public Object getBean( String name ) 
            throws BeansException {
        
        return appCtx.getBean( name ) ;
    }

    public boolean isPrototype( String name )
            throws NoSuchBeanDefinitionException {
        
        return appCtx.isPrototype( name ) ;
    }

    public boolean isSingleton( String name )
            throws NoSuchBeanDefinitionException {
        
        return appCtx.isSingleton( name ) ;
    }
}
