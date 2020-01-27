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
 * Unit tests for serial number model object.
 * 
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class SerialNumberTest {

    @Test
    public void testIsWellFormed()
    {
        assertEquals(true, new SerialNumber("123123123123").isWellFormed());
        assertEquals(false, new SerialNumber("123123123").isWellFormed());
        assertEquals(false, new SerialNumber("123 123 123 123").isWellFormed());
        assertEquals(false, new SerialNumber("123123123xyz").isWellFormed());
        assertEquals(false, new SerialNumber("123123123987").isWellFormed());
    }

    @Test
    public void testGetTypeIdentifier()
    {
        assertEquals("123", new SerialNumber("123123123123").getTypeIdentifier());
        assertEquals("Unknown", new SerialNumber("xyz").getTypeIdentifier());
    }

    @Test
    public void testGetComponentType()
    {
        assertEquals("NTD-4R", new SerialNumber("186170024143").getComponentType());
        assertEquals("Nobø Switch", new SerialNumber("234001021010").getComponentType());
        assertEquals("Unknown, please contact maintainer to add a new type for 123123123123", new SerialNumber("123123123123").getComponentType());
        assertEquals("Unknown, please contact maintainer to add a new type for foobar", new SerialNumber("foobar").getComponentType());
    }

}
