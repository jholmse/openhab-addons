/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.nobohub.model;

import static org.junit.Assert.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;

/**
 * Unit tests for Hub model object.
 * 
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class HubTest {

    @Test
    public void testParseH05() throws NoboDataException
    {
        Hub hub = Hub.fromH05("H05 102000092118 My Eco Hub 2880 4 114 11123610_rev._1 20190426");
        assertEquals(new SerialNumber("102000092118"), hub.getSerialNumber());
        assertEquals("My Eco Hub", hub.getName());
        assertEquals(4, hub.getActiveOverrideId());
        assertEquals("114", hub.getSoftwareVersion());
        assertEquals("11123610_rev._1", hub.getHardwareVersion());
        assertEquals("20190426", hub.getProductionDate());
    }

    @Test
    public void testParseV03() throws NoboDataException
    {
        Hub hub = Hub.fromH05("V03 102000092118 My Eco Hub 2880 14 114 11123610_rev._1 20190426");
        assertEquals(new SerialNumber("102000092118"), hub.getSerialNumber());
        assertEquals("My Eco Hub", hub.getName());
        assertEquals(14, hub.getActiveOverrideId());
        assertEquals("114", hub.getSoftwareVersion());
        assertEquals("11123610_rev._1", hub.getHardwareVersion());
        assertEquals("20190426", hub.getProductionDate());
    }

}
