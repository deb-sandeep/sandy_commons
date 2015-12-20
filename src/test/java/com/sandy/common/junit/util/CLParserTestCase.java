package com.sandy.common.junit.util;

import static org.junit.Assert.* ;
import static org.hamcrest.CoreMatchers.* ;

import org.junit.Test ;

import com.sandy.common.junit.util.helper.DummyCLParser ;

public class CLParserTestCase {

    @Test public void usage() throws Exception {
        
        String[] args = { "-i", "--firstName=Sandeep" } ;
        DummyCLParser parser = new DummyCLParser() ;
        parser.parse( args ) ;
        
        assertThat( parser.isInteractive(), is( equalTo( true ) ) );
        assertThat( parser.getFirstName(), is( equalTo( "Sandeep" ) ) ) ;
    }
}
