package com.sandy.common.junit.util;

import static org.junit.Assert.* ;

import java.net.URL ;

import static org.hamcrest.CoreMatchers.* ;

import org.junit.Before ;
import org.junit.Test ;

import com.sandy.common.junit.util.helper.DummyCLParser ;
import com.sandy.common.junit.util.helper.DummyConfigurable ;
import com.sandy.common.util.Configurator ;
import com.sandy.common.util.ReflectionUtil ;

public class ConfiguratorTestCase {

    private Configurator configurator = new Configurator() ;
    
    @Before public void setUp() {
        configurator = new Configurator() ;
    }
    
    @Test
    public void registerConfigurableObjectsWithDefaultConflictStrategy() 
            throws Exception {
        
        configurator.registerConfigurableObject( "keyA", "Obj1" ) ;
        configurator.registerConfigurableObject( "keyA", "Obj2" ) ;
        
        assertThat( configurator.getConfigurableObject( "keyA" ), 
                is( equalTo( (Object)"Obj2" ) ) ) ;
    }
    
    @Test
    public void registerConfigurableObjectsWithIgnoreConflictStrategy() 
            throws Exception {
        
        configurator.setConflictResolutionStrategy( Configurator.IGNORE_ON_CONFLICT ) ;
        configurator.registerConfigurableObject( "keyA", "Obj1" ) ;
        configurator.registerConfigurableObject( "keyA", "Obj2" ) ;
        
        assertThat( configurator.getConfigurableObject( "keyA" ), 
                is( equalTo( (Object)"Obj1" ) ) ) ;
    }
    
    @Test( expected=Exception.class )
    public void registerConfigurableObjectsWithExceptionConflictStrategy() 
        throws Exception {
        
        configurator.setConflictResolutionStrategy( Configurator.EXCEPTION_ON_CONFLICT ) ;
        configurator.registerConfigurableObject( "keyA", "Obj1" ) ;
        configurator.registerConfigurableObject( "keyA", "Obj2" ) ;
        
        assertThat( configurator.getConfigurableObject( "keyA" ), 
                    is( equalTo( (Object)"Obj1" ) ) ) ;
    }
    
    @Test
    public void initialize() throws Exception {
        
        DummyConfigurable obj = new DummyConfigurable() ;
        URL url = ReflectionUtil.getTestConfigResource( 
                                              ConfiguratorTestCase.class, 
                                              "configurator-test.properties" ) ;
        
        configurator.registerConfigResourceURL( url ) ;
        configurator.registerConfigurableObject( "ObjA", obj ) ;
        configurator.initialize() ;
        
        assertThat( obj.getName(), is( equalTo( "Sandeep" ) ) ) ;
        assertThat( obj.getAge(), is( equalTo( 10 ) ) ) ;
        assertThat( obj.getNested().getNestedProperty(), is( equalTo( "NestedValue" ) ) ) ;
    }
    
    @Test
    public void clpInjection() throws Exception {
        
        String[] args = { "-i", "--firstName=Sandeep" } ;
        DummyCLParser cmdLine = new DummyCLParser() ;
        cmdLine.parse( args ) ;
        
        DummyConfigurable obj = new DummyConfigurable() ;
        URL url = ReflectionUtil.getTestConfigResource( 
                                              ConfiguratorTestCase.class, 
                                              "configurator-clp-test.properties" ) ;
        
        configurator.registerConfigResourceURL( url ) ;
        configurator.registerConfigurableObject( "ObjA", obj ) ;
        configurator.setCommandLine( cmdLine ) ;
        configurator.initialize() ;
        
        assertThat( obj.isInteractive(), is( equalTo( true ) ) ) ;
    }
}
