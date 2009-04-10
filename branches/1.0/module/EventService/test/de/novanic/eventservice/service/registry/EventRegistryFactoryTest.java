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
package de.novanic.eventservice.service.registry;

import de.novanic.eventservice.config.EventServiceConfiguration;
import de.novanic.eventservice.config.RemoteEventServiceConfiguration;
import de.novanic.eventservice.config.EventServiceConfigurationFactory;
import de.novanic.eventservice.config.loader.ConfigurationLoader;
import de.novanic.eventservice.config.loader.ConfigurationException;
import de.novanic.eventservice.EventServiceTestCase;

import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Level;

/**
 * @author sstrohschein
 * Date: 28.07.2008
 * <br>Time: 22:00:52
 */
public class EventRegistryFactoryTest extends EventServiceTestCase
{
    public void tearDown() {
        tearDownEventServiceConfiguration();

        EventRegistryFactory.reset();
    }

    public void testGetInstance() {
        EventRegistryFactory theEventRegistryFactory = EventRegistryFactory.getInstance();
        assertSame(theEventRegistryFactory, EventRegistryFactory.getInstance());

        EventRegistry theEventRegistry = theEventRegistryFactory.getEventRegistry();
        assertSame(theEventRegistry, theEventRegistryFactory.getEventRegistry());
    }

    public void testResetEventRegistry() {
        EventRegistry theEventRegistry = EventRegistryFactory.getInstance().getEventRegistry();
        assertSame(theEventRegistry, EventRegistryFactory.getInstance().getEventRegistry());

        EventRegistryFactory.reset();
        assertNotSame(theEventRegistry, EventRegistryFactory.getInstance().getEventRegistry());
    }

    public void testInit() {
        EventRegistryFactory theEventRegistryFactory = EventRegistryFactory.getInstance();
        EventRegistry theEventRegistry = theEventRegistryFactory.getEventRegistry();

        EventServiceConfiguration theConfiguration = theEventRegistry.getConfiguration();
        assertSame(theConfiguration, theEventRegistryFactory.getEventRegistry().getConfiguration());

        tearDownEventServiceConfiguration();
        EventServiceConfiguration theNewConfiguration = new RemoteEventServiceConfiguration(0, 1, 2);
        setUp(theNewConfiguration);

        theEventRegistryFactory = EventRegistryFactory.getInstance();

        assertNotSame(theNewConfiguration, theConfiguration);
        assertNotSame(theConfiguration, theEventRegistryFactory.getEventRegistry().getConfiguration());
        assertSame(theNewConfiguration, theEventRegistryFactory.getEventRegistry().getConfiguration());
    }

    public void testInit_Log() {
        EventServiceConfiguration theNewConfiguration = new RemoteEventServiceConfiguration(0, 1, 2);

        final TestLoggingHandler theTestLoggingHandler = new TestLoggingHandler();
        
        Logger theLogger = Logger.getLogger(DefaultEventRegistry.class.getName());
        final Level theOldLevel = theLogger.getLevel();

        try {
            theLogger.setLevel(Level.FINEST);
            theLogger.addHandler(theTestLoggingHandler);

            tearDownEventServiceConfiguration();
            setUp(theNewConfiguration);

            EventRegistryFactory.getInstance().getEventRegistry();

            assertEquals("Server: Configuration changed - EventServiceConfiguration. Min.: 0ms; Max.: 1ms; Timeout: 2ms", theTestLoggingHandler.getLastMessage());
        } finally {
            theLogger.setLevel(theOldLevel);
            theLogger.removeHandler(theTestLoggingHandler);
        }
    }

    public void testGetEventRegistryError() {
        EventServiceConfigurationFactory.getInstance().addCustomConfigurationLoader(new TestErrorConfigurationLoader());

        try {
            EventRegistryFactory.getInstance().getEventRegistry();
            fail("Exception expected!");
        } catch(ConfigurationException e) {}
    }

    private class TestLoggingHandler extends Handler
    {
        private String myLastMessage;

        public void publish(LogRecord aRecord) {
            myLastMessage = aRecord.getMessage();
        }

        public void flush() {}

        public void close() throws SecurityException {}

        public String getLastMessage() {
            return myLastMessage;
        }
    }

    private class TestErrorConfigurationLoader implements ConfigurationLoader
    {
        public boolean isAvailable() {
            return true;
        }

        public EventServiceConfiguration load() {
            throw new ConfigurationException("testException");
        }
    }
}