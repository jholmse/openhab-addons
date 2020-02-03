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
 * Unit tests for WeekProfile model object.
 * 
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class WeekProfileTest {

    private static LocalDateTime MONDAY = LocalDateTime.of(2020, Month.MAY, 11, 00, 00);
    private static LocalDateTime WEDNESDAY = LocalDateTime.of(2020, Month.MAY, 13, 00, 00);
    private static LocalDateTime SUNDAY = LocalDateTime.of(2020, Month.MAY, 17, 23, 59);

    @Test
    public void testParseH03() throws NoboDataException
    {
        WeekProfile weekProfile = WeekProfile.fromH03("H03 1 Default 00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,00000,07001,00000,07001,23000");
        assertEquals(1, weekProfile.getId());
        assertEquals("Default", weekProfile.getName());
    }

    @Test
    public void testFindFirstStatus() throws NoboDataException {
        WeekProfile weekProfile = WeekProfile.fromH03("H03 1 Default 00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,00000,07001,00000,07001,23000");
        WeekProfileStatus status = weekProfile.getStatusAt(MONDAY);
        assertEquals(WeekProfileStatus.ECO, status);
    }

    @Test
    public void testFindLastStatus() throws NoboDataException {
        WeekProfile weekProfile = WeekProfile.fromH03("H03 1 Default 00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,00000,07001,00000,07001,23000");
        WeekProfileStatus status = weekProfile.getStatusAt(SUNDAY);
        assertEquals(WeekProfileStatus.ECO, status);
    }

    @Test
    public void testFindEmptyDayStatus() throws NoboDataException {
        WeekProfile weekProfile = WeekProfile.fromH03("H03 1 Default 00000,00000,00001,00000,00000,00000,00000");
        WeekProfileStatus status = weekProfile.getStatusAt(WEDNESDAY);
        assertEquals(WeekProfileStatus.COMFORT, status);
    }

    @Test
    public void testFindStartingNowStatus() throws NoboDataException {
        WeekProfile weekProfile = WeekProfile.fromH03("H03 1 Default 00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,00000,07001,00000,07001,23000");
        WeekProfileStatus status = weekProfile.getStatusAt(MONDAY.plusHours(6));
        assertEquals(WeekProfileStatus.COMFORT, status);

        status = weekProfile.getStatusAt(MONDAY.plusHours(6).plusMinutes(1));
        assertEquals(WeekProfileStatus.COMFORT, status);

        status = weekProfile.getStatusAt(MONDAY.plusHours(6).minusMinutes(1));
        assertEquals(WeekProfileStatus.ECO, status);
    }

    @Test
    public void testFindNormalStatus() throws NoboDataException {
        WeekProfile weekProfile = WeekProfile.fromH03("H03 1 Default 00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,00000,07001,00000,07001,23000");
        WeekProfileStatus status = weekProfile.getStatusAt(WEDNESDAY.plusHours(7).plusMinutes(13));
        assertEquals(WeekProfileStatus.COMFORT, status);
    }
}
