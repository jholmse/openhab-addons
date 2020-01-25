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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
    public static final ThingTypeUID THING_TYPE_HUB = new ThingTypeUID(BINDING_ID, "nobohub");
    public static final ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(BINDING_ID, "zone");
    public static final ThingTypeUID THING_TYPE_COMPONENT = new ThingTypeUID(BINDING_ID, "component");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS
        = new HashSet<ThingTypeUID>(Arrays.asList(THING_TYPE_HUB, THING_TYPE_ZONE, THING_TYPE_COMPONENT));

    // List of all Channel ids

    // Hub
    public static final String CHANNEL_HUB_ACTIVE_OVERRIDE_NAME = "activeOverrideName";

    // Zone
    public static final String CHANNEL_ZONE_WEEK_PROFILE_NAME = "activeWeekProfile";
    public static final String CHANNEL_ZONE_COMFORT_TEMPERATURE = "comfortTemperature";
    public static final String CHANNEL_ZONE_ECO_TEMPERATURE = "ecoTemperature";
    public static final String CHANNEL_ZONE_CURRENT_TEMPERATURE = "currentTemperature";

    // Component
    public static final String CHANNEL_COMPONENT_CURRENT_TEMPERATURE = "currentTemperature";

    // Date/time
    public static final DateTimeFormatter DATE_FORMAT_SECONDS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static final DateTimeFormatter DATE_FORMAT_MINUTES = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
}
