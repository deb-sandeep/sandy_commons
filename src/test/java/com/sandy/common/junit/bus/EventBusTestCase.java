package com.sandy.common.junit.bus;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.sandy.common.bus.EventBus ;

/**
 * This test case contains test for the {@link EventBus} class.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class EventBusTestCase extends TestCase {

    public static final Logger logger = Logger.getLogger( EventBusTestCase.class ) ;

    private static Integer TEST_EVENT_1 = 1 ;
    
    private static EventBus BUS = new EventBus() ;

    public void setUp() throws Exception {
        BUS.clear() ;
    }

    /**
     * FEATURE: Register a simple subscribe and publish an event to the bus
     */
    public void testRegisterAndPublishSync() {
        
        final MockEventSubscriber subs = new MockEventSubscriber() ;

        BUS.addSubscriberForEventTypes( subs, false, TEST_EVENT_1 );
        BUS.publishEvent( TEST_EVENT_1, "Test" );
        
        assertEquals( 1, subs.getEvents( TEST_EVENT_1 ).size() ) ;
    }

    /**
     * FEATURE: Register a simple subscribe and publish an event to the bus,
     *          the event should be received. De-register the subscriber
     *          and publish the event again, the event should not be received
     */
    public void testDeRegister() {
        
        final MockEventSubscriber subs = new MockEventSubscriber() ;
        BUS.addSubscriberForEventTypes( subs, false, TEST_EVENT_1 ) ;

        BUS.addSubscriberForEventTypes( subs, false, TEST_EVENT_1 );
        BUS.publishEvent( TEST_EVENT_1, "Test" );

        subs.clearEvents() ;
        BUS.removeSubscriber( subs, TEST_EVENT_1 ) ;
        BUS.publishEvent( TEST_EVENT_1, "Test" );
        
        assertEquals( 0, subs.getEvents( TEST_EVENT_1 ).size() ) ;
    }
    
    /**
     * FEATURE: Register a simple subscribe and publish an event asynchronously
     */
    public void testRegisterAndPublishASync()
        throws Exception {

        final MockEventSubscriber subs = new MockEventSubscriber() ;

        BUS.addSubscriberForEventTypes( subs, true, TEST_EVENT_1 ) ;
        BUS.publishEvent( TEST_EVENT_1, "Test" );
        
        Thread.sleep( 100 ) ;
        assertEquals( 1, subs.getEvents( TEST_EVENT_1 ).size() ) ;
        BUS.clear() ;
    }
}