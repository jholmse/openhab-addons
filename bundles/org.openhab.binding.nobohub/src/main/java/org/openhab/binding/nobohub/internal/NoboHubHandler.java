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

import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.nobohub.internal.connection.HubCommunicationThread;
import org.openhab.binding.nobohub.internal.connection.HubConnection;
import org.openhab.binding.nobohub.model.Component;
import org.openhab.binding.nobohub.model.Hub;
import org.openhab.binding.nobohub.model.Override;
import org.openhab.binding.nobohub.model.WeekProfile;
import org.openhab.binding.nobohub.model.Zone;
import org.openhab.binding.nobohub.model.NoboCommunicationException;
import org.openhab.binding.nobohub.model.NoboDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NoboHubHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class NoboHubHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(NoboHubHandler.class);

    private @Nullable NoboHubConfiguration config;
    private @Nullable HubConnection connection;
    private @Nullable HubCommunicationThread hubThread;

    private @NotNull Map<Integer, Override> overrideRegister = new HashMap<Integer, Override>();
    private @NotNull Map<Integer, WeekProfile> weekProfileRegister = new HashMap<Integer, WeekProfile>();
    private @NotNull Map<Integer, Zone> zoneRegister = new HashMap<Integer, Zone>();
    private @NotNull Map<String, Component> componentRegister = new HashMap<String, Component>();

    public NoboHubHandler(Thing thing) {
        super(thing);
    }

    @java.lang.Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("NOBØ HUB: Handle command!");
        if (CHANNEL_ACTIVE_OVERRIDE_ID.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                try {
                    if (connection != null)
                    {
                        connection.refreshAll();
                    }
                } catch (NoboCommunicationException noboEx) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Failed to get status: " + noboEx.getMessage());
                }
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @java.lang.Override
    public void initialize() {
        config = getConfigAs(NoboHubConfiguration.class);

        if (null == config)
        {
            logger.error("NOBØ HUB: Missing Configuration");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No configuration set");
        } else {
            logger.info("NOBØ HUB: Looking for HUB {} at {}", config.serialNumber, config.hostName);
        }

        // Set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        updateStatus(ThingStatus.UNKNOWN);

        // Background handshake:
        scheduler.execute(() -> {
            try {
                connection = new HubConnection(config.hostName, config.serialNumber, this);
                connection.connect();

                Duration timeout = Duration.ofSeconds(14);
                if (config.pollingInterval > 0) {
                    timeout = Duration.ofSeconds(config.pollingInterval);
                }

                hubThread = new HubCommunicationThread(connection, timeout);
                hubThread.start();
            } catch (NoboCommunicationException commEx) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, commEx.getMessage());
            }

            if (connection != null && connection.isConnected()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    @java.lang.Override
    public void dispose() {
        hubThread.stopNow();
    }

    public void receivedData(String line)
    {
        try {
            parseLine(line);
        } catch (NoboDataException nde) {
            logger.error("Failed parsing line '{}': {}", line, nde.getMessage());
        }
    }


    private void parseLine(String line) throws NoboDataException
    {
        if (null == line) {
            return;
        }

        if (line.startsWith("H01")) {
            Zone zone = Zone.fromH01(line);
            zoneRegister.put(zone.getId(), zone);
        } else if (line.startsWith("H02")) {
            Component component = Component.fromH02(line);
            componentRegister.put(component.getSerialNumber(), component);
        } else if (line.startsWith("H03")) {
            WeekProfile weekProfile = WeekProfile.fromH03(line);
            weekProfileRegister.put(weekProfile.getId(), weekProfile);
        } else if (line.startsWith("H04")) {
            Override override = Override.fromH04(line);
            overrideRegister.put(override.getId(), override);
        } else if (line.startsWith("H05")) {
            Hub hub = Hub.fromH05(line);

            updateState(NoboHubBindingConstants.CHANNEL_SERIAL_NUMBER, StringType.valueOf(hub.getSerialNumber()));
            updateState(NoboHubBindingConstants.CHANNEL_NAME, StringType.valueOf(hub.getName()));
            updateState(NoboHubBindingConstants.CHANNEL_ACTIVE_OVERRIDE_ID, new DecimalType(hub.getActiveOverrideId()));
            updateState(NoboHubBindingConstants.CHANNEL_SOFTWARE_VERSION, StringType.valueOf(hub.getSoftwareVersion()));
            updateState(NoboHubBindingConstants.CHANNEL_HARDWARE_VERSION, StringType.valueOf(hub.getHardwareVersion()));
            updateState(NoboHubBindingConstants.CHANNEL_PRODUCTION_DATE, StringType.valueOf(hub.getProductionDate()));
        } else if (line.startsWith("S00")) {
            Zone zone = Zone.fromH01(line);
            zoneRegister.remove(zone.getId());
        } else if (line.startsWith("S01")) {
            Component component = Component.fromH02(line);
            componentRegister.remove(component.getSerialNumber());
        } else if (line.startsWith("S02")) {
            WeekProfile weekProfile = WeekProfile.fromH03(line);
            weekProfileRegister.remove(weekProfile.getId());
        } else if (line.startsWith("S03")) {
            Override override = Override.fromH04(line);
            overrideRegister.remove(override.getId());
        } else if (line.startsWith("B00")) {
            Zone zone = Zone.fromH01(line);
            zoneRegister.put(zone.getId(), zone);
        } else if (line.startsWith("B01")) {
            Component component = Component.fromH02(line);
            componentRegister.put(component.getSerialNumber(), component);
        } else if (line.startsWith("B02")) {
            WeekProfile weekProfile = WeekProfile.fromH03(line);
            weekProfileRegister.put(weekProfile.getId(), weekProfile);
        } else if (line.startsWith("B03")) {
            Override override = Override.fromH04(line);
            overrideRegister.put(override.getId(), override);
        } else if (line.startsWith("V00")) {
            Zone zone = Zone.fromH01(line);
            zoneRegister.replace(zone.getId(), zone);
        } else if (line.startsWith("V01")) {
            Component component = Component.fromH02(line);
            componentRegister.replace(component.getSerialNumber(), component);
        } else if (line.startsWith("V02")) {
            WeekProfile weekProfile = WeekProfile.fromH03(line);
            weekProfileRegister.replace(weekProfile.getId(), weekProfile);
        } else if (line.startsWith("V03")) {
            Hub hub = Hub.fromH05(line);

            updateState(NoboHubBindingConstants.CHANNEL_SERIAL_NUMBER, StringType.valueOf(hub.getSerialNumber()));
            updateState(NoboHubBindingConstants.CHANNEL_NAME, StringType.valueOf(hub.getName()));
            updateState(NoboHubBindingConstants.CHANNEL_ACTIVE_OVERRIDE_ID, new DecimalType(hub.getActiveOverrideId()));
            updateState(NoboHubBindingConstants.CHANNEL_SOFTWARE_VERSION, StringType.valueOf(hub.getSoftwareVersion()));
            updateState(NoboHubBindingConstants.CHANNEL_HARDWARE_VERSION, StringType.valueOf(hub.getHardwareVersion()));
            updateState(NoboHubBindingConstants.CHANNEL_PRODUCTION_DATE, StringType.valueOf(hub.getProductionDate()));
        } else if (line.startsWith("Y02")) {
            String parts[] = line.split(" ", 3);
            String serialNumber = parts[1];
            try {
                if (parts[2] == null) {
                    throw new NoboDataException("Missing temperature data");
                }

                double temp = Double.parseDouble(parts[2]);
                componentRegister.get(serialNumber).setTemperature(temp);    
            } catch (NumberFormatException nfe) {
                throw new NoboDataException(String.format("Failed to parse temperature %s: %s", parts[2], nfe.getMessage()), nfe);
            }
        } else if (line.startsWith("E00")) {
            logger.error("Error from Hub: {}", line);
        } else {
            // HANDSHAKE: Basic part of keepalive
            // V06: Encryption key
            // H00: contains no information
            if (!line.startsWith("HANDSHAKE") && !line.startsWith("V06") && !line.startsWith("H00")) {
                logger.info("Unknown information from Hub: '{}}'", line);
            }
        }
    }
}
