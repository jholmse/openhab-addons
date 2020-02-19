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
 * The mode of the {@link Override}. What the value is overridden to.
 * 
 * @author Jørgen Austvik - Initial contribution
 */
public enum OverrideMode {

    NORMAL(0),
    COMFORT(1),
    ECO(2),
    AWAY(3);

    private int numValue;

    private OverrideMode(int numValue) {
        this.numValue = numValue;
    }

    public static OverrideMode getByNumber(int value) throws NoboDataException
    {
        switch (value) {
            case 0: return NORMAL;
            case 1: return COMFORT;
            case 2: return ECO;
            case 3: return AWAY;
            default: throw new NoboDataException(String.format("Unknown override mode %d", value));
        }
    }

    public int getNumValue() {
        return numValue;
    }

    public static OverrideMode getByName(String name) throws NoboDataException {
        if (null == name) {
            throw new NoboDataException("Missing name");
        }

        if (name.equalsIgnoreCase("Normal")) {
            return NORMAL;
        } else if (name.equalsIgnoreCase("Comfort")) {
            return COMFORT;
        } else if (name.equalsIgnoreCase("Eco")) {
            return ECO;
        } else if (name.equalsIgnoreCase("Away")) {
            return AWAY;
        }

        throw new NoboDataException(String.format("Unknown name of override mode: '%s'", name));
    }
}
