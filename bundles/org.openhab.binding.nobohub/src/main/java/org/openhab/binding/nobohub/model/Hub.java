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

public class Hub {

    private final String serialNumber;

    private final String name;

    private final int activeOverrideId;

    private final String softwareVersion;

    private final String hardwareVersion;

    private final String productionDate;

    public Hub(String serialNumber, String name, int activeOverrideId, String softwareVersion, String hardwareVersion, String productionDate) {
        this.serialNumber = serialNumber;
        this.name = name;
        this.activeOverrideId = activeOverrideId;
        this.softwareVersion = softwareVersion;
        this.hardwareVersion = hardwareVersion;
        this.productionDate = productionDate;
    }

    public static Hub fromH05(String h05) throws NoboDataException
    {
        String parts[] = h05.split(" ", 8);

        if (parts.length != 8) {
            throw new NoboDataException(String.format("Unexpected number of parts from hub on H5 call: %d", parts.length));
        }

        return new Hub(ModelHelper.toJavaString(parts[1]),
                       ModelHelper.toJavaString(parts[2]),
                       Integer.parseInt(parts[4]),
                       ModelHelper.toJavaString(parts[5]),
                       ModelHelper.toJavaString(parts[6]),
                       ModelHelper.toJavaString(parts[7]));
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getName() {
        return name;
    }

    public int getActiveOverrideId() {
        return activeOverrideId;
    }
    
    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public String getProductionDate() {
        return productionDate;
    }
}