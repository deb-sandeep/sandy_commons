package com.sandy.common.junit.objfactory;

import static org.junit.Assert.* ;
import static org.hamcrest.CoreMatchers.* ;

import org.junit.Test ;

import com.sandy.common.junit.objfactory.helper.SampleBean ;
import com.sandy.common.objfactory.SpringObjectFactory ;

public class SpringObjectFactoryTestCase {

    @Test public void initialization() throws Exception {
        
        SpringObjectFactory objFactory = new SpringObjectFactory() ;
        objFactory.addResourcePath( "classpath:com/sandy/common/junit/objfactory/sof-test.xml" );
        objFactory.initialize() ;
        
        SampleBean bean = ( SampleBean )objFactory.getBean( "testBean" ) ;
        assertThat( bean.getFirstName(), is( equalTo( "Sandeep" ) ) ) ;
        assertThat( bean.getLastName(), is( equalTo( "Deb" ) ) ) ;
        assertThat( bean.getAge(), is( equalTo( 10 ) ) ) ;
    }
}
