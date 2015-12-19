package com.sandy.common.junit.bus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sandy.common.bus.Event ;
import com.sandy.common.bus.EventSubscriber ;

/**
 * Implementation of a mock event subscriber used to test the event bus
 * functionality.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class MockEventSubscriber implements EventSubscriber {

    public static final Logger logger = Logger.getLogger( MockEventSubscriber.class ) ;

    private final Map<Integer, List<Event>> eventMap = 
                                         new HashMap<Integer, List<Event>>() ;

    public void handleEvent( final Event event ) {
        List<Event> evtList = this.eventMap.get( event.getEventType() ) ;
        if( evtList == null ) {
            evtList = new ArrayList<Event>() ;
            this.eventMap.put( event.getEventType(), evtList ) ;
        }
        evtList.add( event ) ;
    }

    /**
     * Returns the list of events received for the given event type.
     *
     * @param type The type of the event
     * @return A list of events received for this event type. Can be null
     */
    public List<Event> getEvents( final Integer type ) {
        List<Event> events = this.eventMap.get( type ) ;
        if( events == null ) {
            events = Collections.emptyList() ;
        }
        return events ;
    }

    public void clearEvents() {
        this.eventMap.clear() ;
    }
}