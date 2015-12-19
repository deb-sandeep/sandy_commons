package com.sandy.common.bus;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;
import java.util.Map.Entry ;

import org.apache.log4j.Logger ;

/**
 * This class can register multiple subscribers and dispatch events to the
 * registered subscribers in a synchronous or asynchronous fashion based on the
 * registration preferences.
 * 
 * Note that this class is intentionally not made a singleton. It is up to the
 * using application to scope the instance(s) as appropriate. 
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class EventBus {

    public static final Logger logger = Logger.getLogger( EventBus.class ) ;
    
    public static final int ALL_EVENTS = 0xCAFEBABE ;
    
    private static class EventRange {
        
        private int lowerBoundEventId ;
        private int upperBoundEventId ;
        
        EventRange( int lowerEventId, int upperEventId ) {
            this.lowerBoundEventId = lowerEventId ;
            this.upperBoundEventId = upperEventId ;
        }
        
        boolean containsEventId( int eventId ) {
            return ( eventId >= lowerBoundEventId ) && 
                   ( eventId <= upperBoundEventId ) ;
        }
        
        @Override
        public boolean equals( Object o ) {
            
            EventRange r = ( EventRange )o ;
            return ( r.lowerBoundEventId == lowerBoundEventId ) &&
                    ( r.upperBoundEventId == upperBoundEventId ) ;
        }
    }

    private Map<Integer, List<EventSubscriber>> eventSubscriberMap =
             new HashMap<Integer, List<EventSubscriber>>() ;
    
    private Map<EventRange, List<EventSubscriber>> eventRangeSubscriberMap = 
             new HashMap<EventBus.EventRange, List<EventSubscriber>>() ;
    
    
    private boolean isSubscriberPresent( List<EventSubscriber> subscribers, 
                                         EventSubscriber subscriber ) {
        
        for( EventSubscriber aSubscriberInList : subscribers ) {
            if( aSubscriberInList.equals( subscriber ) ) {
                return true ;
            }
        }
        return false ;
    }
    
    private void addSubscriberToEventMap( 
            EventSubscriber subscriber, boolean asyncDispatch, int event ) {
     
         List<EventSubscriber> subscribers = null ;
         
         subscribers = eventSubscriberMap.get( event ) ;
         if( subscribers == null ) {
             subscribers = new ArrayList<EventSubscriber>() ;
             eventSubscriberMap.put( event, subscribers ) ;
         }
         
         if( !isSubscriberPresent( subscribers, subscriber ) ) {
             if( asyncDispatch ) {
                 subscribers.add( new AsyncEventDispatchProxy( subscriber ) ) ;
             }
             else {
                 subscribers.add( subscriber ) ;
             }
         }
     }
    
    private void removeSubscriberFromEventMap(
                                       EventSubscriber subscriber, int event ) {
        
        List<EventSubscriber> subscribers   = null ;
        EventSubscriber       regSubscriber = null ;
        AsyncEventDispatchProxy asyncProxy  = null ;
        
        subscribers = eventSubscriberMap.get( event ) ;
        if( subscribers != null ) {
            for( Iterator<EventSubscriber> esIter = subscribers.iterator(); 
                 esIter.hasNext(); ) {
                
                regSubscriber = esIter.next() ;
                if( regSubscriber.equals( subscriber ) ) {
                    esIter.remove() ;
                    if( regSubscriber instanceof AsyncEventDispatchProxy ) {
                        asyncProxy = ( AsyncEventDispatchProxy )regSubscriber ;
                        asyncProxy.stop() ;
                    }
                }
            }
        }
    }

    /**
     * Returns a list of all registered events for the given subscriber. Note 
     * that a subscriber can be registered for a specific event and also for
     * all events. In such a case the returned list will have both specific and
     * ALL_EVENTS id.
     */
    public synchronized List<Integer> getRegisteredEventsForSubscriber( 
                                                  EventSubscriber subscriber ) {
        
        List<Integer> registeredEvents = new ArrayList<Integer>() ;
        for( Map.Entry<Integer, List<EventSubscriber>> entry : 
             eventSubscriberMap.entrySet() ) {
            
            if( isSubscriberPresent( entry.getValue(), subscriber ) ) {
                registeredEvents.add( entry.getKey() ) ;
            }
        }
        return registeredEvents ;
    }
    
    public synchronized List<EventRange> getRegisteredEventRangesForSubscriber(
                                                EventSubscriber subscriber ) {
        
        List<EventRange> registeredEventRanges = new ArrayList<EventBus.EventRange>() ;
        for( Map.Entry<EventRange, List<EventSubscriber>> entry : 
             eventRangeSubscriberMap.entrySet() ) {
            
            if( isSubscriberPresent( entry.getValue(), subscriber ) ) {
                registeredEventRanges.add( entry.getKey() ) ;
            }
        }
        return registeredEventRanges ;
    }
    
    public synchronized List<EventSubscriber> getSubscribersForEvent( int event ) {
        
        List<EventSubscriber> subscribers = new ArrayList<EventSubscriber>() ;
        if( eventSubscriberMap.containsKey( event ) ) {
            subscribers.addAll( eventSubscriberMap.get( event ) ) ;
        }
        
        List<EventSubscriber> allEventSubscribers = eventSubscriberMap.get( ALL_EVENTS ) ;
        if( allEventSubscribers != null ) {
            for( EventSubscriber anAllEvtSubscriber : allEventSubscribers ) {
                if( !isSubscriberPresent( subscribers, anAllEvtSubscriber ) ) {
                    subscribers.add( anAllEvtSubscriber ) ;
                }
            }
        }
        
        for( Map.Entry<EventRange, List<EventSubscriber>> entry : 
            eventRangeSubscriberMap.entrySet() ) {
            
            if( entry.getKey().containsEventId( event ) ) {
                for( EventSubscriber aRangeSubscriber : entry.getValue() ) {
                    if( !isSubscriberPresent( subscribers, aRangeSubscriber ) ) {
                        subscribers.add( aRangeSubscriber ) ;
                    }
                }
            }
        }
        
        return subscribers ;
    }
    
    /**
     * Register a subscriber with a variable number of interested event types.
     * The added subscriber will be notified if an event is generated for
     * any of the interested event types.
     *
     * @param subscriber The subscriber instance to register.
     * 
     * @param asyncDispatch A boolean flag indicating if the subscriber prefers
     *        to receive the events in the same thread as the publisher or
     *        asynchronously.
     *
     * @param eventTypes The interested event types for which this subscriber
     *        will be notified by the bus. If the event types is null,
     *        this subscriber will be notified on all the events.
     */
    public synchronized void addSubscriberForEventTypes( 
                                            final EventSubscriber subscriber,
                                            final boolean asyncDispatch,
                                            final int... eventTypes ) {

        if( eventTypes == null || eventTypes.length == 0 ) {
            addSubscriberToEventMap( subscriber, asyncDispatch, ALL_EVENTS ) ;
        }
        else {
            for( final int type : eventTypes ) {
                addSubscriberToEventMap( subscriber, asyncDispatch, type ) ;
            }
        }
    }
    
    public synchronized void addSubscriberForEventRange( 
                                            final EventSubscriber subscriber,
                                            final boolean asyncDispatch,
                                            final int lowerRangeEventId,
                                            final int upperRangeEventId ) {
        
        EventRange range = new EventRange( lowerRangeEventId, upperRangeEventId ) ;
        if( eventRangeSubscriberMap.containsKey( range ) ) {
            eventRangeSubscriberMap.get( range ).add( subscriber ) ;
        }
        else {
            List<EventSubscriber> subscribers = new ArrayList<EventSubscriber>() ;
            subscribers.add( subscriber ) ;
            eventRangeSubscriberMap.put( range, subscribers ) ;
        }
    }

    /**
     * Removes the specified subscriber from the provided event types. Once this
     * method is called, notifications to the subscriber will not be sent for
     * the event types for which the subscriber is being removed.
     *
     * @param subscriber The subscriber instance to de-register.
     *
     * @param eventTypes The event types for which this subscriber
     *        will not be notified by the bus. If the event types is null,
     *        this subscriber will be removed from all existing registrations
     */
    public synchronized void removeSubscriber( 
                                              final EventSubscriber subscriber,
                                              int... eventTypes ) {
        
        if( eventTypes == null || eventTypes.length == 0 ) {
            removeSubscriberFromEventMap( subscriber, ALL_EVENTS ) ;
            for( Integer eventId : eventSubscriberMap.keySet() ) {
                removeSubscriberFromEventMap( subscriber, eventId ) ;
                
                for( Entry<EventRange, List<EventSubscriber>> entry : 
                     eventRangeSubscriberMap.entrySet() ) {
                    
                    if( isSubscriberPresent( entry.getValue(), subscriber ) ) {
                        entry.getValue().remove( subscriber ) ;
                    }
                }
            }
        }
        else {
            for( final int type : eventTypes ) {
                removeSubscriberFromEventMap( subscriber, type ) ;
                
                for( Entry<EventRange, List<EventSubscriber>> entry : 
                    eventRangeSubscriberMap.entrySet() ) {
                   
                   if( entry.getKey().containsEventId( type ) ) {
                       if( isSubscriberPresent( entry.getValue(), subscriber ) ) {
                           entry.getValue().remove( subscriber ) ;
                       }
                   }
               }
            }
        }
    }

    /** Removes all the subscribers and attempts to stop them gracefully. */
    public synchronized void clear() {

        AsyncEventDispatchProxy proxy = null ;

        for( Map.Entry<Integer, List<EventSubscriber>> entry : 
             eventSubscriberMap .entrySet() ) {
            
            for( EventSubscriber subscriber : entry.getValue() ) {

                if( subscriber instanceof AsyncEventDispatchProxy ) {
                    proxy = (AsyncEventDispatchProxy) subscriber ;
                    proxy.stop() ;
                }
            }
        }

        for( Map.Entry<EventRange, List<EventSubscriber>> entry : 
             eventRangeSubscriberMap.entrySet() ) {
            
            for( EventSubscriber subscriber : entry.getValue() ) {
                if( subscriber instanceof AsyncEventDispatchProxy ) {
                    proxy = (AsyncEventDispatchProxy) subscriber ;
                    proxy.stop() ;
                }
            }
        }

        eventSubscriberMap.clear() ;
        eventRangeSubscriberMap.clear() ;
    }
    
    /**
     * Publishes an event. All the subscribers registered to the given event
     * type are notified of the event. The notification happens either
     * synchronously or asynchronously depending upon the way the subscriber
     * was added.
     *
     * @param eventType The type of event being publishes.
     *
     * @param value The value associated with this event.
     */
    public synchronized void publishEvent( final int eventType, final Object value ) {
        
        Event event = new Event( eventType, value ) ;
        List<EventSubscriber> subscribers = getSubscribersForEvent( eventType ) ;
        if( !subscribers.isEmpty() ) {
            for( EventSubscriber aSubscriber : subscribers ) {
                aSubscriber.handleEvent( event ) ;
            }
        }
    }
}
