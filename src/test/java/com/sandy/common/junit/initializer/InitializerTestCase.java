package com.sandy.common.junit.initializer ;

import static org.junit.Assert.* ;
import static org.hamcrest.CoreMatchers.* ;

import java.net.URL ;

import org.junit.Before ;
import org.junit.Test ;

import com.sandy.common.initializer.Initializer ;
import com.sandy.common.junit.initializer.helper.DummyInitializable ;
import com.sandy.common.junit.initializer.helper.SingletonRepository ;
import com.sandy.common.util.ReflectionUtil ;


public class InitializerTestCase {
    
    private DummyInitializable dummy = null ;
    
    @Before
    public void setUp() throws Exception {
        
        SingletonRepository.clear() ;
        URL resUrl = ReflectionUtil.getTestConfigResource( 
                InitializerTestCase.class, 
                "initializer-test.xml" ) ;
        Initializer initializer = new Initializer() ;
        initializer.initialize( resUrl );
        
        Object o = SingletonRepository.get( "instance" ) ;
        assertThat( o, is( notNullValue() ) );
        assertThat( o, instanceOf( DummyInitializable.class ) ) ;
        
        dummy = ( DummyInitializable )o ;
    }
    
    @Test
    public void setStringProperty() {
        assertEquals( "privateValue",  dummy.getPrivateString() ) ;
    }
    
    @Test
    public void setIntProperty() {
        assertEquals( 10, dummy.getIntValue() ) ;
    }
    
    @Test
    public void setBooleanProperty() {
        assertEquals( false, dummy.isBooleanValue() ) ;
    }
    
    @Test
    public void setListProperty() {
        assertThat( dummy.getListOfValues(), is( notNullValue() ) ) ;
        assertEquals( 3, dummy.getListOfValues().size() ) ;
        assertThat( dummy.getListOfValues(), hasItem( "Two" ) ) ;
    }
}
