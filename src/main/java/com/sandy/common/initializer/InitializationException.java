package com.sandy.common.initializer;

public class InitializationException extends Exception {

    private static final long serialVersionUID = -2664827418282471711L ;

    public InitializationException() {
        super() ;
    }

    public InitializationException( String message ) {
        super( message ) ;
    }

    public InitializationException( Throwable cause ) {
        super( cause ) ;
    }

    public InitializationException( String message, Throwable cause ) {
        super( message, cause ) ;
    }
    
    public static void wrapAndThrow( final Exception exception ) 
        throws InitializationException {
        
        wrapAndThrow( exception, null ) ;
    }

    public static void wrapAndThrow( final Exception exception,
                                     final String msg ) 
        throws InitializationException {
        
        if( exception instanceof InitializationException ) {
            throw ( InitializationException )exception ;
        }
        else if( msg != null ){
            throw new InitializationException( msg, exception ) ;
        }
        else {
            throw new InitializationException( exception ) ;
        }
    }
}
