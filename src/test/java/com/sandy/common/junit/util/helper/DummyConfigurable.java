package com.sandy.common.junit.util.helper;

public class DummyConfigurable {
    
    private String name = null ;
    private int age = 0 ;
    private NestedClass nested = new NestedClass() ;
    private boolean interactive = false ;
    
    public boolean isInteractive() {
        return interactive ;
    }

    public void setInteractive( boolean interactive ) {
        this.interactive = interactive ;
    }

    public String getName() {
        return name;
    }
    
    public void setName( String name ) {
        this.name = name;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge( int age ) {
        this.age = age;
    }
    
    public NestedClass getNested() {
        return nested;
    }
    
    public void setNested( NestedClass nested ) {
        this.nested = nested;
    }
}
