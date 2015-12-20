package com.sandy.common.junit.objfactory.helper;

public class SampleBean {

    private String firstName = null ;
    private String lastName  = null ;
    private int    age       = 0 ;
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName( String firstName ) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName( String lastName ) {
        this.lastName = lastName;
    }
    public int getAge() {
        return age;
    }
    public void setAge( int age ) {
        this.age = age;
    }
}
