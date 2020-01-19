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

public enum OverrideType {
    
    NOW(0),
    TIMER(1),
    FROM_TO(2),
    CONSTANT(3);

    private int numValue;

    private OverrideType(int numValue) {
        this.numValue = numValue;
    }

    public static OverrideType getByNumber(int value) throws NoboDataException
    {
        switch (value) {
            case 0: return NOW;
            case 1: return TIMER;
            case 2: return FROM_TO;
            case 3: return CONSTANT;
            default: throw new NoboDataException(String.format("Unknown override type %d", value));
        }
    }

    public int getNumValue() {
        return numValue;
    }
}