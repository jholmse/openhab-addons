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

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public static final String VENDOR = "Glen Dimplex Nobø";

    public static final int NOBO_HUB_TCP_PORT = 27779;

    public static final Duration TIME_BETWEEN_FULL_SCANS = Duration.ofMinutes(10);
    public static final Duration TIME_BETWEEN_RETRIES_ON_ERROR = Duration.ofSeconds(10);

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HUB = new ThingTypeUID(BINDING_ID, "nobohub");
    public static final ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(BINDING_ID, "zone");
    public static final ThingTypeUID THING_TYPE_COMPONENT = new ThingTypeUID(BINDING_ID, "component");

    public static final Set<ThingTypeUID> AUTODISCOVERED_THING_TYPES_UIDS
        = new HashSet<ThingTypeUID>(Arrays.asList(THING_TYPE_ZONE, THING_TYPE_COMPONENT));

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS
        = new HashSet<ThingTypeUID>(Arrays.asList(THING_TYPE_HUB, THING_TYPE_ZONE, THING_TYPE_COMPONENT));

    // List of all Channel ids

    // Hub
    public static final String CHANNEL_HUB_ACTIVE_OVERRIDE_NAME = "activeOverrideName";

    // Zone
    public static final String CHANNEL_ZONE_WEEK_PROFILE_NAME = "activeWeekProfile";
    public static final String CHANNEL_ZONE_CALCULATED_WEEK_PROFILE_STATUS = "calculatedWeekProfileStatus";
    public static final String CHANNEL_ZONE_COMFORT_TEMPERATURE = "comfortTemperature";
    public static final String CHANNEL_ZONE_ECO_TEMPERATURE = "ecoTemperature";
    public static final String CHANNEL_ZONE_CURRENT_TEMPERATURE = "currentTemperature";

    // Component
    public static final String CHANNEL_COMPONENT_CURRENT_TEMPERATURE = "currentTemperature";

    // Date/time
    public static final DateTimeFormatter DATE_FORMAT_SECONDS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static final DateTimeFormatter DATE_FORMAT_MINUTES = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    public static final DateTimeFormatter TIME_FORMAT_MINUTES = DateTimeFormatter.ofPattern("HHmm");

    // Discovery
    public static final int NOBO_HUB_BROADCAST_PORT = 10000;
    public static final String NOBO_HUB_BROADCAST_ADDRESS = "0.0.0.0";
    public static final int NOBO_HUB_MULTICAST_PORT = 10001;
    public static final String NOBO_HUB_MULTICAST_ADDRESS = "239.0.1.187";

    // Mappings

    public static final Map<String, String> REJECT_REASONS = Stream.of(new String[][] { 
        { "0", "Client command set too old, run it in with debug logs and let the maintainer know" }, 
        { "1", "Hub serial number mismatch (should be 12 digits, if hub was autodetected, plase add the last three)" },
        { "2", "Wrong number of arguments, run it in with debug logs and let the maintainer know" }, 
        { "3", "Timestamp incorrectly formatted, run it in with debug logs and let the maintainer know" }, 
    }).collect(Collectors.collectingAndThen(
        Collectors.toMap(data -> data[0], data -> data[1]), 
        Collections::<String, String> unmodifiableMap));

    // Full list of units: http://help.nobo.no/skriver/?chapterid=344&chapterlanguageid=2
    public static final Map<String, String> SERIALNUMBERS_FOR_TYPES = Stream.of(new String[][] { 
            { "120", "RS-700" }, 
            { "168", "NCU-2R" },
            { "184", "NCU-1R" },
            { "186", "NTD-4R" },
            { "192", "TXF" }, 
            { "198", "NCU-ER" }, 
            { "210", "NTB-2R" }, 
            { "234", "Nobø Switch" }, 
        }).collect(Collectors.collectingAndThen(
            Collectors.toMap(data -> data[0], data -> data[1]), 
            Collections::<String, String> unmodifiableMap));
}
