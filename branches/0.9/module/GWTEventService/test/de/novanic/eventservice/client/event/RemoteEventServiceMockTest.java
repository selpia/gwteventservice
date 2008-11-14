/*
 * GWTEventService
 * Copyright (c) 2008, GWTEventService Committers
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.novanic.eventservice.client.event;

import de.novanic.eventservice.client.event.filter.EventFilter;
import de.novanic.eventservice.client.event.domain.DomainFactory;
import de.novanic.eventservice.client.event.domain.Domain;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * @author sstrohschein
 * Date: 03.08.2008
 * Time: 22:55:08
 */
public class RemoteEventServiceMockTest extends AbstractRemoteEventServiceMockTest
{
    private static final Domain TEST_DOMAIN = DomainFactory.getDomain("test_domain");
    private static final Domain TEST_DOMAIN_2 = DomainFactory.getDomain("test_domain_2");

    private RemoteEventService myRemoteEventService;

    public void setUp() {
        super.setUp();
        myRemoteEventService = new DefaultRemoteEventService(new GWTRemoteEventConnector(myEventServiceAsyncMock));
    }

    public void testInit_Error() {
        try {
            new DefaultRemoteEventService(new GWTRemoteEventConnector());
            fail("Exception expected, because the GWTService is instantiated in a non GWT context!");
        } catch(Throwable e) {}
    }

    public void testAddListener() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);

        //caused by callback of register
        mockListen(true);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN, new TestEventListener());
            assertTrue(myRemoteEventService.isActive());
            //a second time  
            myRemoteEventService.addListener(TEST_DOMAIN, new TestEventListener());
            assertTrue(myRemoteEventService.isActive());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testAddListener_Error() {
        //caused by first addListener / activate
        myEventServiceAsyncMock.register(TEST_DOMAIN, null, null);
        myEventServiceAsyncMockControl.setMatcher(new AsyncCallArgumentsMatcher(TestException.getInstance()));
        myEventServiceAsyncMockControl.setVoidCallable();

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN, new TestEventListener());
            assertFalse(myRemoteEventService.isActive());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testAddListener_Callback() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);

        //caused by callback of register
        mockListen(true);

        //caused by third addListener (another domain)
        mockRegister(TEST_DOMAIN_2, false);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.addListener(TEST_DOMAIN, new TestEventListener(), theRecordedCallback);
            assertTrue(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());

            //a second time
            theRecordedCallback = new RecordedCallback();
            myRemoteEventService.addListener(TEST_DOMAIN, new TestEventListener(), theRecordedCallback);
            assertTrue(myRemoteEventService.isActive());
            //nothing is called on the callback, because the user is already registered to the domain
            assertFalse(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());

            //a third time for another domain
            theRecordedCallback = new RecordedCallback();
            myRemoteEventService.addListener(TEST_DOMAIN_2, new TestEventListener(), theRecordedCallback);
            assertTrue(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testAddListener_Callback_Failure() {
        //caused by first addListener
        mockRegister(TEST_DOMAIN, TestException.getInstance(), true);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.addListener(TEST_DOMAIN, new TestEventListener(), theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertFalse(theRecordedCallback.isOnSuccessCalled());
            assertTrue(theRecordedCallback.isOnFailureCalled());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testAddListener_EventFilter() {
        final TestEventFilter theEventFilter = new TestEventFilter();
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, theEventFilter, true);

        //caused by second addListener
        mockRegisterEventFilter(TEST_DOMAIN, theEventFilter, true);

        //caused by callback of register
        mockListen(true);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN, new TestEventListener(), theEventFilter);
            assertTrue(myRemoteEventService.isActive());
            //a second time
            myRemoteEventService.addListener(TEST_DOMAIN, new TestEventListener(), theEventFilter);
            assertTrue(myRemoteEventService.isActive());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testAddListener_EventFilter_Callback() {
        final TestEventFilter theEventFilter = new TestEventFilter();
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, theEventFilter, true);

        //caused by second addListener
        mockRegisterEventFilter(TEST_DOMAIN, theEventFilter, true);

        //caused by callback of register
        mockListen(true);

        //caused by third addListener (another domain)
        mockRegister(TEST_DOMAIN_2, theEventFilter, false);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.addListener(TEST_DOMAIN, new TestEventListener(), theEventFilter, theRecordedCallback);
            assertTrue(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());

            //a second time
            theRecordedCallback = new RecordedCallback();
            myRemoteEventService.addListener(TEST_DOMAIN, new TestEventListener(), theEventFilter);
            assertTrue(myRemoteEventService.isActive());
            //nothing is called on the callback, because the user is already registered to the domain
            assertFalse(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());

            //a third time for another domain
            theRecordedCallback = new RecordedCallback();
            myRemoteEventService.addListener(TEST_DOMAIN_2, new TestEventListener(), theEventFilter, theRecordedCallback);
            assertTrue(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testAddListener_EventFilter_Callback_Failure() {
        //caused by first addListener
        final TestEventFilter theEventFilter = new TestEventFilter();
        mockRegister(TEST_DOMAIN, theEventFilter, TestException.getInstance(), true);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.addListener(TEST_DOMAIN, new TestEventListener(), theEventFilter, theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertFalse(theRecordedCallback.isOnSuccessCalled());
            assertTrue(theRecordedCallback.isOnFailureCalled());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testRemoveListener() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);

        //caused by callback of register
        mockListen(true);

        //caused by first removeListener
        mockUnlisten(TEST_DOMAIN, true);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.removeListener(TEST_DOMAIN, theRemoteListener);
            assertFalse(myRemoteEventService.isActive());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testRemoveListener_2() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);
        //caused by second addListener / reactivate
        mockRegister(TEST_DOMAIN_2, false);

        //caused by callback of register
        mockListen(true);

        //caused by first removeListener
        mockUnlisten(TEST_DOMAIN, true);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.removeListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive()); //because there is a listener in TEST_DOMAIN_2
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testRemoveListener_3() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);
        //caused by second addListener / reactivate
        mockRegister(TEST_DOMAIN, false);

        //caused by callback of register
        mockListen(true);
        //caused by second callback of register
        mockListen(false);

        //caused by first removeListener
        mockUnlisten(TEST_DOMAIN, true);
        //caused by second removeListener
        mockUnlisten(TEST_DOMAIN, false);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.removeListener(TEST_DOMAIN, theRemoteListener);
            assertFalse(myRemoteEventService.isActive());
        
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.removeListener(TEST_DOMAIN, theRemoteListener);
            assertFalse(myRemoteEventService.isActive());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testRemoveListener_4() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);

        //caused by callback of register
        mockListen(true);

        //caused by first removeListener
        mockUnlisten(TEST_DOMAIN, true);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            final TestEventListener theRemoteListener_2 = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener_2);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.removeListener(TEST_DOMAIN, theRemoteListener_2);
            //still active, because there is still another listener registered to the domain
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.removeListener(TEST_DOMAIN, theRemoteListener);
            assertFalse(myRemoteEventService.isActive());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testRemoveListener_Callback() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);
        //caused by second addListener / reactivate
        mockRegister(TEST_DOMAIN_2, false);

        //caused by callback of register
        mockListen(true);

        //caused by first removeListener
        mockUnlisten(TEST_DOMAIN, true);
        //caused by second removeListener
        mockUnlisten(TEST_DOMAIN_2, false);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListener(TEST_DOMAIN, theRemoteListener, theRecordedCallback);
            assertTrue(myRemoteEventService.isActive()); //because there is a listener in TEST_DOMAIN_2
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());

            theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListener(TEST_DOMAIN_2, theRemoteListener, theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testRemoveListener_Callback_Failure() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);

        //caused by callback of register
        mockListen(true);

        //caused by first removeListener
        mockUnlisten(TEST_DOMAIN, TestException.getInstance(), true);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListener(TEST_DOMAIN, theRemoteListener, theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertFalse(theRecordedCallback.isOnSuccessCalled());
            assertTrue(theRecordedCallback.isOnFailureCalled());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testRemoveListeners() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);
        mockRegister(TEST_DOMAIN_2, false);

        //caused by callback of register
        mockListen(true);

        //caused by removeListeners
        Set<Domain> theDomains = new HashSet<Domain>();
        theDomains.add(TEST_DOMAIN);
        theDomains.add(TEST_DOMAIN_2);
        mockUnlisten(theDomains, true);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.removeListeners();
            assertFalse(myRemoteEventService.isActive());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testRemoveListeners_Domain() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);
        mockRegister(TEST_DOMAIN_2, false);

        //caused by callback of register
        mockListen(true);

        //caused by first call to removeListeners
        mockUnlisten(TEST_DOMAIN, true);
        //caused by second call to removeListeners
        mockUnlisten(TEST_DOMAIN_2, false);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.removeListeners(TEST_DOMAIN);
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.removeListeners(TEST_DOMAIN_2);
            assertFalse(myRemoteEventService.isActive());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testRemoveListeners_Domains() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);
        mockRegister(TEST_DOMAIN_2, false);

        //caused by callback of register
        mockListen(true);

        //caused by removeListeners
        Set<Domain> theDomains = new HashSet<Domain>();
        theDomains.add(TEST_DOMAIN);
        theDomains.add(TEST_DOMAIN_2);
        mockUnlisten(theDomains, true);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.removeListeners(theDomains);
            assertFalse(myRemoteEventService.isActive());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testRemoveListeners_Domains_2() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);
        mockRegister(TEST_DOMAIN_2, false);

        //caused by callback of register
        mockListen(true);

        //caused by first call to removeListeners
        Set<Domain> theDomains = new HashSet<Domain>();
        theDomains.add(TEST_DOMAIN_2);
        mockUnlisten(theDomains, true);
        //caused by second call to removeListeners
        Set<Domain> theDomainsSecondCall = new HashSet<Domain>();
        theDomainsSecondCall.add(TEST_DOMAIN);
        mockUnlisten(theDomainsSecondCall, false);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.removeListeners(theDomains);
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.removeListeners(theDomainsSecondCall);
            assertFalse(myRemoteEventService.isActive());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testRemoveListeners_Callback() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);
        mockRegister(TEST_DOMAIN_2, false);

        //caused by callback of register
        mockListen(true);

        //caused by removeListeners
        Set<Domain> theDomains = new HashSet<Domain>();
        theDomains.add(TEST_DOMAIN);
        theDomains.add(TEST_DOMAIN_2);
        mockUnlisten(theDomains, true);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testRemoveListeners_Callback_Failure() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);

        //caused by callback of register
        mockListen(true);

        //caused by removeListeners
        Set<Domain> theDomains = new HashSet<Domain>();
        theDomains.add(TEST_DOMAIN);
        mockUnlisten(theDomains, TestException.getInstance(), true);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertFalse(theRecordedCallback.isOnSuccessCalled());
            assertTrue(theRecordedCallback.isOnFailureCalled());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testRemoveListeners_Domain_Callback() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);
        mockRegister(TEST_DOMAIN_2, false);

        //caused by callback of register
        mockListen(true);

        //caused by first call to removeListeners
        mockUnlisten(TEST_DOMAIN, true);
        //caused by second call to removeListeners
        mockUnlisten(TEST_DOMAIN_2, false);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(TEST_DOMAIN, theRecordedCallback);
            assertTrue(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());

            theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(TEST_DOMAIN_2, theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testRemoveListeners_Domain_Callback_2() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);
        mockRegister(TEST_DOMAIN_2, false);

        //caused by callback of register
        mockListen(true);

        //caused by first call to removeListeners
        mockUnlisten(TEST_DOMAIN, true);
        //caused by second call to removeListeners
        mockUnlisten(TEST_DOMAIN_2, false);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(DomainFactory.getDomain("unknownDomain"), theRecordedCallback);
            assertTrue(myRemoteEventService.isActive());
            assertFalse(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());

            theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(TEST_DOMAIN_2, theRecordedCallback);
            assertTrue(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());

            theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(TEST_DOMAIN, theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testRemoveListeners_Domain_Callback_Failure() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);

        //caused by callback of register
        mockListen(true);

        //caused by first call to removeListeners
        mockUnlisten(TEST_DOMAIN, TestException.getInstance(), true);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(TEST_DOMAIN, theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertFalse(theRecordedCallback.isOnSuccessCalled());
            assertTrue(theRecordedCallback.isOnFailureCalled());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testRemoveListeners_Domains_Callback() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);
        mockRegister(TEST_DOMAIN_2, false);

        //caused by callback of register
        mockListen(true);

        //caused by removeListeners
        Set<Domain> theDomains = new HashSet<Domain>();
        theDomains.add(TEST_DOMAIN);
        theDomains.add(TEST_DOMAIN_2);
        mockUnlisten(theDomains, true);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(theDomains, theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testRemoveListeners_Domains_Callback_2() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);
        mockRegister(TEST_DOMAIN_2, false);

        //caused by callback of register
        mockListen(true);

        //caused by removeListeners
        Set<Domain> theDomains = new HashSet<Domain>();
        theDomains.add(TEST_DOMAIN);
        theDomains.add(TEST_DOMAIN_2);
        mockUnlisten(theDomains, true);

        //that shouldn't trigger a server call
        Set<Domain> theUnknownDomains = new HashSet<Domain>(1);
        theUnknownDomains.add(DomainFactory.getDomain("unknownDomain"));

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(theUnknownDomains, theRecordedCallback);
            assertTrue(myRemoteEventService.isActive());
            assertFalse(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());

            theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(theDomains, theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testRemoveListeners_Domains_Callback_Failure() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);

        //caused by callback of register
        mockListen(true);

        //caused by removeListeners
        Set<Domain> theDomains = new HashSet<Domain>();
        theDomains.add(TEST_DOMAIN);
        mockUnlisten(theDomains, TestException.getInstance(), true);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(theDomains, theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertFalse(theRecordedCallback.isOnSuccessCalled());
            assertTrue(theRecordedCallback.isOnFailureCalled());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testUnlisten() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);
        mockRegister(TEST_DOMAIN_2, false);

        //caused by callback of register
        mockListen(true);

        //caused by removeListeners for TEST_DOMAIN
        mockUnlisten(TEST_DOMAIN, true);
        //caused by removeListeners for TEST_DOMAIN_2
        mockUnlisten(TEST_DOMAIN_2, false);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.removeListeners(TEST_DOMAIN);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.removeListeners(TEST_DOMAIN_2);
            assertFalse(myRemoteEventService.isActive());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testUnlisten_2() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);
        mockRegister(TEST_DOMAIN_2, false);

        //caused by callback of register
        mockListen(true);

        Set<Domain> theDomains = new HashSet<Domain>();
        theDomains.add(TEST_DOMAIN);
        theDomains.add(TEST_DOMAIN_2);

        //caused by removeListeners for domains
        mockUnlisten(theDomains, true);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.removeListeners(theDomains);
            assertFalse(myRemoteEventService.isActive());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testUnlisten_Error() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);

        //caused by callback of register
        mockListen(true);

        //caused by removeListeners for TEST_DOMAIN
        mockUnlisten(TEST_DOMAIN, TestException.getInstance(), true);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.removeListeners(TEST_DOMAIN);
            assertFalse(myRemoteEventService.isActive());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testListen() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);

        //caused by callback of register
        List<DomainEvent> theEvents = new ArrayList<DomainEvent>();
        theEvents.add(new DummyDomainEvent(TEST_DOMAIN));
        mockListen(theEvents, 3);
        mockListen(false);
        mockListen(false);
        mockListen(false);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            assertEquals(3, theRemoteListener.getEventCount(DummyEvent.class.getName()));
            assertTrue(myRemoteEventService.isActive());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testListen_Error() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);

        //caused by callback of register
        myEventServiceAsyncMock.listen(null);
        myEventServiceAsyncMockControl.setMatcher(new AsyncCallArgumentsMatcher(TestException.getInstance()));
        myEventServiceAsyncMockControl.setVoidCallable();

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            assertEquals(0, theRemoteListener.getEventCount(DummyEvent.class.getName()));
            assertTrue(myRemoteEventService.isActive());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testListen_Error_2() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);

        //caused by callback of register
        mockListen(null, 1);
        
        Set<Domain> theDomains = new HashSet<Domain>(1);
        theDomains.add(TEST_DOMAIN);
        mockUnlisten(theDomains, true);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());
            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertFalse(myRemoteEventService.isActive());

            assertEquals(0, theRemoteListener.getEventCount(DummyEvent.class.getName()));
            assertFalse(myRemoteEventService.isActive());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testRegisterEventFilter() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);

        //caused by callback of register
        List<DomainEvent> theEvents = new ArrayList<DomainEvent>();
        theEvents.add(new DummyDomainEvent(TEST_DOMAIN));
        theEvents.add(new DummyDomainEvent(TEST_DOMAIN));

        //listen without filter
        mockListen(theEvents, 1);
        mockListen(false);

        final TestEventFilter theEventFilter = new TestEventFilter();
        mockRegisterEventFilter(TEST_DOMAIN, theEventFilter, true);

        mockDeregisterEventFilter(TEST_DOMAIN, true);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());

            //this can not be tested in this test, because the filter function is implemented in the mocked server side.
            myRemoteEventService.registerEventFilter(TEST_DOMAIN, theEventFilter);
            myRemoteEventService.deregisterEventFilter(TEST_DOMAIN);

            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            assertEquals(2, theRemoteListener.getEventCount(DummyEvent.class.getName()));
            assertTrue(myRemoteEventService.isActive());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testRegisterEventFilter_Callback() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);

        //caused by callback of register
        List<DomainEvent> theEvents = new ArrayList<DomainEvent>();
        theEvents.add(new DummyDomainEvent(TEST_DOMAIN));
        theEvents.add(new DummyDomainEvent(TEST_DOMAIN));
        mockListen(theEvents, 1);

        //listen without filter
        mockListen(false);

        final TestEventFilter theEventFilter = new TestEventFilter();
        mockRegisterEventFilter(TEST_DOMAIN, theEventFilter, true);

        mockDeregisterEventFilter(TEST_DOMAIN, true);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());

            RecordedCallback theRegisterEventFilterCallback = new RecordedCallback();
            RecordedCallback theDeregisterEventFilterCallback = new RecordedCallback();

            //this can not be tested in this test, because the filter function is implemented in the mocked server side.
            myRemoteEventService.registerEventFilter(TEST_DOMAIN, theEventFilter, theRegisterEventFilterCallback);
            assertTrue(theRegisterEventFilterCallback.isOnSuccessCalled());
            assertFalse(theRegisterEventFilterCallback.isOnFailureCalled());

            myRemoteEventService.deregisterEventFilter(TEST_DOMAIN, theDeregisterEventFilterCallback);
            assertTrue(theDeregisterEventFilterCallback.isOnSuccessCalled());
            assertFalse(theDeregisterEventFilterCallback.isOnFailureCalled());

            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            assertEquals(2, theRemoteListener.getEventCount(DummyEvent.class.getName()));
            assertTrue(myRemoteEventService.isActive());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testRegisterEventFilter_Callback_Failure() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);

        //caused by callback of register
        List<DomainEvent> theEvents = new ArrayList<DomainEvent>();
        theEvents.add(new DummyDomainEvent(TEST_DOMAIN));
        theEvents.add(new DummyDomainEvent(TEST_DOMAIN));
        mockListen(theEvents, 1);

        //listen without filter
        mockListen(false);

        final TestEventFilter theEventFilter = new TestEventFilter();
        mockRegisterEventFilter(TEST_DOMAIN, theEventFilter, TestException.getInstance(), true);

        mockDeregisterEventFilter(TEST_DOMAIN, true);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());

            RecordedCallback theRegisterEventFilterCallback = new RecordedCallback();
            RecordedCallback theDeregisterEventFilterCallback = new RecordedCallback();

            //this can not be tested in this test, because the filter function is implemented in the mocked server side.
            myRemoteEventService.registerEventFilter(TEST_DOMAIN, theEventFilter, theRegisterEventFilterCallback);
            assertFalse(theRegisterEventFilterCallback.isOnSuccessCalled());
            assertTrue(theRegisterEventFilterCallback.isOnFailureCalled());

            myRemoteEventService.deregisterEventFilter(TEST_DOMAIN, theDeregisterEventFilterCallback);
            assertTrue(theDeregisterEventFilterCallback.isOnSuccessCalled());
            assertFalse(theDeregisterEventFilterCallback.isOnFailureCalled());

            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            assertEquals(2, theRemoteListener.getEventCount(DummyEvent.class.getName()));
            assertTrue(myRemoteEventService.isActive());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    public void testDeregisterEventFilter_Callback_Failure() {
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, true);

        //caused by callback of register
        List<DomainEvent> theEvents = new ArrayList<DomainEvent>();
        theEvents.add(new DummyDomainEvent(TEST_DOMAIN));
        theEvents.add(new DummyDomainEvent(TEST_DOMAIN));
        mockListen(theEvents, 1);

        //listen without filter
        mockListen(false);

        final TestEventFilter theEventFilter = new TestEventFilter();
        mockRegisterEventFilter(TEST_DOMAIN, theEventFilter, true);

        mockDeregisterEventFilter(TEST_DOMAIN, TestException.getInstance(), true);

        myEventServiceAsyncMockControl.replay();
            assertFalse(myRemoteEventService.isActive());

            RecordedCallback theRegisterEventFilterCallback = new RecordedCallback();
            RecordedCallback theDeregisterEventFilterCallback = new RecordedCallback();

            final TestEventListener theRemoteListener = new TestEventListener();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            assertEquals(2, theRemoteListener.getEventCount(DummyEvent.class.getName()));
            assertTrue(myRemoteEventService.isActive());

            //this can not be tested in this test, because the filter function is implemented in the mocked server side.
            myRemoteEventService.registerEventFilter(TEST_DOMAIN, theEventFilter, theRegisterEventFilterCallback);
            assertTrue(myRemoteEventService.isActive());
            assertTrue(theRegisterEventFilterCallback.isOnSuccessCalled());
            assertFalse(theRegisterEventFilterCallback.isOnFailureCalled());

            myRemoteEventService.deregisterEventFilter(TEST_DOMAIN, theDeregisterEventFilterCallback);
            assertTrue(myRemoteEventService.isActive());
            assertFalse(theDeregisterEventFilterCallback.isOnSuccessCalled());
            assertTrue(theDeregisterEventFilterCallback.isOnFailureCalled());
        myEventServiceAsyncMockControl.verify();
        myEventServiceAsyncMockControl.reset();
    }

    private class TestEventFilter implements EventFilter
    {
        public boolean match(Event anEvent) {
            return false;
        }
    }
}