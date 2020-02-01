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

import java.time.LocalDateTime;
import java.time.Month;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;

/**
 * Unit tests for Override model object.
 * 
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class OverrideTest {

    @Test
    public void testParseH04DefaultOverride() throws NoboDataException {
        Override parsed = Override.fromH04("H04 4 0 0 -1 -1 0 -1");
        assertEquals(4, parsed.getId());
        assertEquals(OverrideMode.NORMAL, parsed.getMode());
        assertEquals(OverrideType.NOW, parsed.getType());
        assertEquals(OverrideTarget.HUB, parsed.getTarget());
        assertEquals(-1, parsed.getTargetId());
        assertEquals(null, parsed.startTime());
        assertEquals(null, parsed.endTime());
    }

    @Test
    public void testParseB03WithStartDate() throws NoboDataException {
        Override parsed = Override.fromH04("B03 9 3 1 202001221930 -1 0 -1");
        assertEquals(9, parsed.getId());
        assertEquals(OverrideMode.AWAY, parsed.getMode());
        assertEquals(OverrideType.TIMER, parsed.getType());
        assertEquals(OverrideTarget.HUB, parsed.getTarget());
        assertEquals(-1, parsed.getTargetId());
        LocalDateTime date = LocalDateTime.of(2020, Month.JANUARY, 22, 19, 30);
        assertEquals(date, parsed.startTime());
        assertEquals(null, parsed.endTime());
    }

    @Test
    public void testParseS03NoDate() throws NoboDataException {
        Override parsed = Override.fromH04("S03 13 0 0 -1 -1 0 -1");
        assertEquals(13, parsed.getId());
        assertEquals(OverrideMode.NORMAL, parsed.getMode());
        assertEquals(OverrideType.NOW, parsed.getType());
        assertEquals(OverrideTarget.HUB, parsed.getTarget());
        assertEquals(-1, parsed.getTargetId());
        assertEquals(null, parsed.startTime());
        assertEquals(null, parsed.endTime());
    }

    @Test
    public void testAddA03WithStartDate() throws NoboDataException {
        Override parsed = Override.fromH04("B03 9 3 1 202001221930 -1 0 -1");
        assertEquals("A03 9 3 1 202001221930 -1 0 -1", parsed.generateCommandString("A03"));
    }
}
