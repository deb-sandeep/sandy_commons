package com.sandy.common.bus;
import org.apache.log4j.Logger;

/**
 * A class encapsulating the event information.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class Event {

    public static final Logger logger = Logger.getLogger( Event.class ) ;

    private final int eventType ;
    private final Object value ;
    private final long eventTime ;

    public Event( final int eventType, final Object value ) {
        this.eventType = eventType ;
        this.value = value ;
        this.eventTime = System.currentTimeMillis() ;
    }

    public int getEventType() {
        return this.eventType ;
    }

    public Object getValue() {
        return this.value ;
    }

    public long getEventTime() {
        return this.eventTime ;
    }
}
