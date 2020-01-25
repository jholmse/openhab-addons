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
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
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
public class NoboHubBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(NoboHubBridgeHandler.class);

    private @Nullable NoboHubBridgeConfiguration config;
    private @Nullable HubCommunicationThread hubThread;

    private @NotNull Map<Integer, Override> overrideRegister = new HashMap<Integer, Override>();
    private @NotNull Map<Integer, WeekProfile> weekProfileRegister = new HashMap<Integer, WeekProfile>();
    private @NotNull Map<Integer, Zone> zoneRegister = new HashMap<Integer, Zone>();
    private @NotNull Map<String, Component> componentRegister = new HashMap<String, Component>();

    public NoboHubBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @java.lang.Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("Handle command {} for channel {}!", command.toFullString(), channelUID);

        if (command instanceof RefreshType) {
            try {
                if (hubThread != null)
                {
                    hubThread.getConnection().refreshAll();
                }
            } catch (NoboCommunicationException noboEx) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Failed to get status: " + noboEx.getMessage());
            }

            return;
        }

        if (CHANNEL_HUB_ACTIVE_OVERRIDE_NAME.equals(channelUID.getId())) {
            logger.debug("TODO: Set override for hub {} to {}", channelUID, command);
        }
    }

    @java.lang.Override
    public void initialize() {
        config = getConfigAs(NoboHubBridgeConfiguration.class);

        if (null == config)
        {
            logger.error("Missing Configuration");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No configuration set");
            return;
        }

        String serialNumber = config.serialNumber;
        if (null == serialNumber)
        {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing serial number in configuration");
            return;
        }

        String hostName = config.hostName;
        if (null == hostName)
        {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing host name in configuration");
            return;
        }

        logger.info("Looking for Hub {} at {}", config.serialNumber, config.hostName);

        // Set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        updateStatus(ThingStatus.UNKNOWN);

        // Background handshake:
        scheduler.execute(() -> {
            try {
                HubConnection conn = new HubConnection(hostName, serialNumber, this);
                conn.connect();

                logger.debug("Done connecting to {} ({})", hostName, serialNumber);

                Duration timeout = Duration.ofSeconds(14);
                if (config.pollingInterval > 0) {
                    timeout = Duration.ofSeconds(config.pollingInterval);
                }

                logger.debug("Starting communication thread to {}", hostName);

                hubThread = new HubCommunicationThread(conn, timeout);
                hubThread.start();

                if (hubThread.getConnection().isConnected()) {
                    logger.debug("Communication thread to {} is up and running, we are online", hostName);
                    updateProperty("serialNumber", serialNumber);
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }
            } catch (NoboCommunicationException commEx) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, commEx.getMessage());
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
    
    @java.lang.Override
    public void childHandlerInitialized(ThingHandler handler, Thing thing) {
        logger.info("Adding thing: {}", thing);
    }

    @java.lang.Override
    public void childHandlerDisposed(ThingHandler handler, Thing thing) {
        logger.info("Disposing thing: {}", thing);
    }

    public void receivedData(@Nullable String line)
    {
        try {
            parseLine(line);
        } catch (NoboDataException nde) {
            logger.error("Failed parsing line '{}': {}", line, nde.getMessage());
        }
    }

    private void parseLine(@Nullable String line) throws NoboDataException
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

            Override activeOverride = overrideRegister.get(hub.getActiveOverrideId());
            if (activeOverride != null) {
                updateState(NoboHubBindingConstants.CHANNEL_HUB_ACTIVE_OVERRIDE_NAME, StringType.valueOf(activeOverride.getMode().name()));
            }

            updateProperty("name", hub.getName());
            updateProperty("serialNumber", hub.getSerialNumber());
            updateProperty("softwareVersion", hub.getSoftwareVersion());
            updateProperty("hardwareVersion", hub.getHardwareVersion());
            updateProperty("productionDate", hub.getProductionDate());
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

            updateProperty("name", hub.getName());
            updateProperty("serialNumber", hub.getSerialNumber());
            updateProperty("softwareVersion", hub.getSoftwareVersion());
            updateProperty("hardwareVersion", hub.getHardwareVersion());
            updateProperty("productionDate", hub.getProductionDate());

            Override activeOverride = overrideRegister.get(hub.getActiveOverrideId());
            if (activeOverride != null) {
                updateState(NoboHubBindingConstants.CHANNEL_HUB_ACTIVE_OVERRIDE_NAME, StringType.valueOf(activeOverride.getMode().name()));
            }
        } else if (line.startsWith("Y02")) {
            String parts[] = line.split(" ", 3);
            String serialNumber = parts[1];
            try {
                if (parts[2] == null) {
                    throw new NoboDataException("Missing temperature data");
                }

                double temp = Double.parseDouble(parts[2]);
                Component component = componentRegister.get(serialNumber);
                
                if (component != null) {
                    component.setTemperature(temp); 
                    refreshComponent(component);           
                    int zoneId = component.getTemperatureSensorForZoneId();    
                    if (zoneId >= 0) {
                        Zone zone = zoneRegister.get(zoneId);
                        if (zone != null) {
                            zone.setTemperature(temp);
                            refreshZone(zone);
                        }
                    }
                }
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

    public @Nullable Zone getZone(Integer id) {
        return zoneRegister.get(id);
    }

    public @Nullable WeekProfile getWeekProfile(Integer id) {
        return weekProfileRegister.get(id);
    }

    public @Nullable Component getComponent(String serialNumber) {
        return componentRegister.get(serialNumber);
    }

    private void refreshZone(Zone zone) {
        this.getThing().getThings().forEach(thing -> {
            if (thing.getHandler() instanceof ZoneHandler) {
                ZoneHandler handler = (ZoneHandler) thing.getHandler();
                if (handler != null && handler.getZoneId() == zone.getId()) {
                    handler.onUpdate(zone);
                }    
            }
        });
    }

    private void refreshComponent(Component component) {
        this.getThing().getThings().forEach(thing -> {
            if (thing.getHandler() instanceof ComponentHandler) {
                ComponentHandler handler = (ComponentHandler) thing.getHandler();
                if (handler != null && component.getSerialNumber().equals(handler.getSerialNumber())) {
                    handler.onUpdate(component);
                }    
            }
        });
    }
}
