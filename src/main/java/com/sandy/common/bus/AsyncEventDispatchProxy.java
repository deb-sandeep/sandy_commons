package com.sandy.common.bus;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

/**
 * An implementation of {@link EventSubscriber}, which wraps around concrete
 * implementations of subscribers and dispatches events to them in an
 * asynchronous fashion.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
class AsyncEventDispatchProxy implements EventSubscriber, Runnable {

    public static final Logger logger = Logger.getLogger( AsyncEventDispatchProxy.class ) ;

    private EventSubscriber subscriber = null ;
    private Thread dispatchThread = null ;
    private boolean stop = false ;

    /** The unbounded queue in which events are stored before dispatching. */
    private final LinkedBlockingQueue<Event> eventQueue = 
                                              new LinkedBlockingQueue<Event>() ;

    public AsyncEventDispatchProxy( final EventSubscriber subscriber ) {
        
        this.subscriber = subscriber ;
        this.dispatchThread = new Thread( this ) ;
        this.dispatchThread.setDaemon( true ) ;
        this.dispatchThread.start() ;
    }

    public void run() {
        while( !this.stop ) {
            Event evt = null ;
            try {
                evt = this.eventQueue.take() ;
                this.subscriber.handleEvent( evt ) ;
            }
            catch( InterruptedException ie ) {
                // IE can be generated either for a graceful shutdown or by
                // a burst of cosmic rays. In the former case, the stop 
                // flag would be set to true and this loop will gracefully 
                // terminate. In case of cosmic rays, we gobble up the exception
                // without a burp and let the loop continue.
            }
            catch ( Throwable e ) {
                logger.error( "Dispatch failed for event " + evt, e ) ;
            }
        }
        this.eventQueue.clear() ;
    }

    public void handleEvent( final Event event ) {
        this.eventQueue.add( event ) ;
    }

    public EventSubscriber getSubscriber() {
        return this.subscriber ;
    }

    public void stop() {
        this.stop = true ;
        this.dispatchThread.interrupt() ;
    }

    public boolean equals( final Object obj ) {
        return this.subscriber.equals( obj ) ;
    }

    public int hashCode() {
        return this.subscriber.hashCode() ;
    }
}
