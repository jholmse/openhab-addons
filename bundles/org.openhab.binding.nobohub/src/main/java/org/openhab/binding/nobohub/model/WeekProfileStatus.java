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

/**
 * The status of the {@link WeekProfile}. What the value is in the week profile.
 * 
 * @author Jørgen Austvik - Initial contribution
 */
public enum WeekProfileStatus {

    ECO(0),
    COMFORT(1),
    AWAY(2),
    OFF(3);

    private int numValue;

    private WeekProfileStatus(int numValue) {
        this.numValue = numValue;
    }

    public static WeekProfileStatus getByNumber(int value) throws NoboDataException
    {
        switch (value) {
            case 0: return ECO;
            case 1: return COMFORT;
            case 2: return AWAY;
            case 3: return OFF;
            default: throw new NoboDataException(String.format("Unknown week profile status  %d", value));
        }
    }

    public int getNumValue() {
        return numValue;
    }
}
