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

    private final String serialNumber;
    private final String name;
    private final boolean reverse;
    private final int zoneId;
    private final int temperatureSensorForZoneComponentId;
    private double temperature;

    public Component(String serialNumber, String name, boolean reverse, int zoneId, int temperatureSensorForZoneComponentId) {
        this.serialNumber = serialNumber;
        this.name = name;
        this.reverse = reverse;
        this.zoneId = zoneId;
        this.temperatureSensorForZoneComponentId = temperatureSensorForZoneComponentId;
    }

    public static Component fromH02(String h02) throws NoboDataException {
        String parts[] = h02.split(" ", 8);

        if (parts.length != 8) {
            throw new NoboDataException(String.format("Unexpected number of parts from hub on H2 call: %d", parts.length));
        }

        return new Component(ModelHelper.toJavaString(parts[1]),
                             ModelHelper.toJavaString(parts[3]),
                             "1".equals(parts[4]),
                             Integer.parseInt(parts[5]),
                             Integer.parseInt(parts[7]));
    }

    public String getSerialNumber() {
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

    public int getTemperatureSensorForZoneComponentId() {
        return temperatureSensorForZoneComponentId;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature)
    {
        this.temperature = temperature;
    }
}
