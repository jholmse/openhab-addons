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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A Component in the Nobø Hub can be a Owen, a floor or a switch.
 * 
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public final class Component {

    private final SerialNumber serialNumber;
    private final String name;
    private final boolean reverse;
    private final int zoneId;
    private final int temperatureSensorForZoneId;
    private double temperature;

    public Component(SerialNumber serialNumber, String name, boolean reverse, int zoneId, int temperatureSensorForZoneId) {
        this.serialNumber = serialNumber;
        this.name = name;
        this.reverse = reverse;
        this.zoneId = zoneId;
        this.temperatureSensorForZoneId = temperatureSensorForZoneId;
    }

    public static Component fromH02(String h02) throws NoboDataException {
        String parts[] = h02.split(" ", 8);

        if (parts.length != 8) {
            throw new NoboDataException(String.format("Unexpected number of parts from hub on H2 call: %d", parts.length));
        }

        SerialNumber serial = new SerialNumber(ModelHelper.toJavaString(parts[1]));
        if (!serial.isWellFormed()) {
            throw new NoboDataException(String.format("Illegal serial number: '%s'"));
        }

        return new Component(serial,
                             ModelHelper.toJavaString(parts[3]),
                             "1".equals(parts[4]),
                             Integer.parseInt(parts[5]),
                             Integer.parseInt(parts[7]));
    }

    public String generateCommandString(final String command) {
        return String.join(" ", 
            command,
            ModelHelper.toHubString(serialNumber.toString()),
            "0", // Status not yet implemented in hub
            ModelHelper.toHubString(name),
            reverse ? "1" : "0",
            Integer.toString(zoneId),
            "-1", // Active Override Id not implemented in hub for components yet
            Integer.toString(temperatureSensorForZoneId));
    }
    
    public SerialNumber getSerialNumber() {
        return serialNumber;
    }

    public String getName() {
        return name;
    }

    public boolean inReverse() {
        return reverse;
    }

    public int getZoneId() {
        return zoneId;
    }

    public int getTemperatureSensorForZoneId() {
        return temperatureSensorForZoneId;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature)
    {
        this.temperature = temperature;
    }
}
