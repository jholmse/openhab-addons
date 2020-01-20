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
 * Unit tests for Zone model object.
 * 
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class ZoneTest {

    @Test
    public void testParseH01Simple() throws NoboDataException
    {
        Zone zone = Zone.fromH01("H01 1 1. etage 20 22 16 1 -1");
        assertEquals(1, zone.getId());
        assertEquals("1. etage", zone.getName());
        assertEquals(20, zone.getActiveWeekProfileId());
        assertEquals(true, zone.getAllowOverrides());
        assertEquals(16, zone.getEcoTemperature());
        assertEquals(22, zone.getComfortTemperature());
    }

}
