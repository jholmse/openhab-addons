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
 * Unit tests for WeekProfile model object.
 * 
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class WeekProfileTest {

    @Test
    public void testParseH03() throws NoboDataException
    {
        WeekProfile weekProfile = WeekProfile.fromH03("H03 1 Default 00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,00000,07001,00000,07001,23000");
        assertEquals(1, weekProfile.getId());
        assertEquals("Default", weekProfile.getName());
    }
}
