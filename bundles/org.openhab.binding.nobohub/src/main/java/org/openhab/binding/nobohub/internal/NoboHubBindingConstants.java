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
package org.openhab.binding.nobohub.internal;

import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link NoboHubBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class NoboHubBindingConstants {

    private static final String BINDING_ID = "nobohub";

    public static final String API_VERSION = "1.1";

    public static final int NOBO_HUB_TCP_PORT = 27779;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "sample");

    // List of all Channel ids
    public static final String CHANNEL_SERIAL_NUMBER = "serialNumber";
    public static final String CHANNEL_NAME = "name";
    public static final String CHANNEL_ACTIVE_OVERRIDE_ID = "activeOverrideId";
    public static final String CHANNEL_SOFTWARE_VERSION = "softwareVersion";
    public static final String CHANNEL_HARDWARE_VERSION = "hardwareVersion";
    public static final String CHANNEL_PRODUCTION_DATE = "productionDate";

    public static final DateTimeFormatter DATE_FORMAT_SECONDS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static final DateTimeFormatter DATE_FORMAT_MINUTES = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
}
