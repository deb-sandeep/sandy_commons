package com.sandy.common.junit.initializer.helper;

import java.util.List ;

import com.sandy.common.contracts.Initializable ;

public class DummyInitializable implements Initializable {
    
    private String  privateString = null ;
    private int     intValue      = 0 ;
    private boolean booleanValue  = true ;
    private List<String> listOfValues = null ;

    public void initialize() throws Exception {
        SingletonRepository.register( "instance", this );
    }

    public String getPrivateString() {
        return privateString ;
    }

    public void setPrivateString( String privateString ) {
        this.privateString = privateString ;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue( int intValue ) {
        this.intValue = intValue;
    }

    public boolean isBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue( boolean booleanValue ) {
        this.booleanValue = booleanValue;
    }

    public List<String> getListOfValues() {
        return listOfValues;
    }

    public void setListOfValues( List<String> listOfValues ) {
        this.listOfValues = listOfValues;
    }
}
