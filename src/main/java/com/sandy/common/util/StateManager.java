package com.sandy.common.util;

import static com.sandy.common.util.ReflectionUtil.getResourceAsStream ;

import java.io.File ;
import java.io.FileInputStream ;
import java.io.FileOutputStream ;
import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;
import java.util.Properties ;

import org.apache.commons.beanutils.BeanUtilsBean ;
import org.apache.commons.io.IOUtils ;

public class StateManager {

    private Object              appInstance     = null ;
    private WorkspaceManager    wkspMgr         = null ;
    private File                stateFile       = null ;
    private List<String>        stateAttributes = new ArrayList<>() ;
    private Map<String, Object> statefulObjects = new HashMap<String, Object>() ;
    private BeanUtilsBean       beanUtils       = new BeanUtilsBean() ;
    
    public StateManager( Object appInstance, WorkspaceManager wkspMgr ) {
        this.appInstance = appInstance ;
        this.wkspMgr = wkspMgr ;
    }
    
    public void registerObject( String key, Object obj ) {
        this.statefulObjects.put( key, obj ) ;
    }
    
    public void initialize() throws Exception {
        
        stateFile = wkspMgr.getFile( "app-state.properties" ) ;
        InputStream is = getResourceAsStream( appInstance.getClass(), 
                                              "state-config.list" ) ;
        if( is == null ) {
            throw new Exception( "state-config.list not found." ) ;
        }
        else {
            List<String> lines = IOUtils.readLines( is ) ;
            for( String line : lines ) {
                line = line.trim() ;
                if( line.isEmpty() || line.startsWith( "#" ) ) {
                    continue ;
                }
                else {
                    this.stateAttributes.add( line ) ;
                }
            }
        }
    }
    
    public void saveState() throws Exception {
        
        Properties stateValues = new Properties() ;
        for( String path : this.stateAttributes ) {
            String objKey     = path.substring( 0, path.indexOf( '.' ) ) ;
            String attribPath = path.substring( path.indexOf( '.' ) + 1 ) ;
            
            String value = getValue( objKey, attribPath ) ;
            if( value != null ) {
                stateValues.put( path, value ) ;
            }
        }
        stateValues.store( new FileOutputStream( stateFile ), 
                           "State saved on " + new Date().toString() ) ;
    }
    
    public void loadState() throws Exception {
        
        if( !stateFile.exists() ) {
            return ;
        }
        
        Properties stateValues = new Properties() ;
        stateValues.load( new FileInputStream( stateFile ) ) ;
        
        for( Object pathObj : stateValues.keySet() ) {
            
            String path = pathObj.toString() ;
            
            String objKey     = path.substring( 0, path.indexOf( '.' ) ) ;
            String attribPath = path.substring( path.indexOf( '.' ) + 1 ) ;
            String value      = stateValues.getProperty( path ) ;
            
            setValue( objKey, attribPath, value ) ;
        }
    }

    public String getValue( String objKey, String attribPath ) 
            throws Exception {
            
        if( !this.statefulObjects.containsKey( objKey ) ) {
            throw new Exception( "No object registered against the key " + objKey ) ;
        }
        
        Object obj   = this.statefulObjects.get( objKey ) ;
        String value = beanUtils.getProperty( obj, attribPath ) ;
        return value ;
    }
        
    public void setValue( String objKey, String attribPath, String value )
        throws Exception {
        
        if( !this.statefulObjects.containsKey( objKey ) ) {
            throw new Exception( "No object registered against the key " + objKey ) ;
        }
        Object obj = this.statefulObjects.get( objKey ) ;
        beanUtils.setProperty( obj, attribPath, value ) ;
    }
}
