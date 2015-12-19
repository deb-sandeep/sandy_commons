package com.sandy.common.junit.initializer.helper;

import java.util.HashMap ;
import java.util.Map ;

public class SingletonRepository {

    private static Map<String, Object> repository = new HashMap<String, Object>() ;
    
    private SingletonRepository(){}
    
    public static void register( String key, Object obj ) {
        repository.put( key, obj ) ;
    }
    
    public static Object get( String key ) {
        return repository.get( key ) ;
    }
    
    public static void clear() {
        repository.clear() ;
    }
}
