package com.sandy.common.junit.util.helper;

import com.sandy.common.util.AbstractCLParser ;

public class DummyCLParser extends AbstractCLParser {
    
    private boolean interactive = false ;
    private String firstName = null ;
    
    public boolean isInteractive() {
        return interactive;
    }

    public void setInteractive( boolean interactive ) {
        this.interactive = interactive;
    }
    
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName( String firstName ) {
        this.firstName = firstName;
    }

    @Override
    protected void prepareOptions( OptionCfgCollection options ) {
        options.addOption( "i", "interactive", false, false, "[O] Sets interactive mode" ) ;
        options.addOption( null, "firstName", true, true, "[M] First name" ) ;
    }

    @Override
    protected String getUsageString() {
        return "DummyCLParser [options]" ;
    }
}
