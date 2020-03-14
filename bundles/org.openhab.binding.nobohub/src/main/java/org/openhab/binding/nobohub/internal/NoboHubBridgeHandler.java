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
import org.openhab.binding.nobohub.internal.discovery.NoboThingDiscoveryService;
import org.openhab.binding.nobohub.internal.model.Component;
import org.openhab.binding.nobohub.internal.model.Hub;
import org.openhab.binding.nobohub.internal.model.Override;
import org.openhab.binding.nobohub.internal.model.OverrideMode;
import org.openhab.binding.nobohub.internal.model.SerialNumber;
import org.openhab.binding.nobohub.internal.model.WeekProfile;
import org.openhab.binding.nobohub.internal.model.Zone;
import org.openhab.binding.nobohub.internal.model.NoboCommunicationException;
import org.openhab.binding.nobohub.internal.model.NoboDataException;
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
    private @Nullable NoboThingDiscoveryService discoveryService;
    private @Nullable Hub hub;

    private @NotNull Map<Integer, Override> overrideRegister = new HashMap<Integer, Override>();
    private @NotNull Map<Integer, WeekProfile> weekProfileRegister = new HashMap<Integer, WeekProfile>();
    private @NotNull Map<Integer, Zone> zoneRegister = new HashMap<Integer, Zone>();
    private @NotNull Map<SerialNumber, Component> componentRegister = new HashMap<SerialNumber, Component>();

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
                    HubCommunicationThread ht = Helpers.castToNonNull(hubThread, "hubThread");
                    ht.getConnection().refreshAll();
                }
            } catch (NoboCommunicationException noboEx) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Failed to get status: " + noboEx.getMessage());
            }

            return;
        }

        if (CHANNEL_HUB_ACTIVE_OVERRIDE_NAME.equals(channelUID.getId())) {
            if (hubThread != null && hub != null)
            {
                HubCommunicationThread ht = Helpers.castToNonNull(hubThread, "hubThread");
                Hub h = Helpers.castToNonNull(hub, "hub");
                if (command instanceof StringType) {
                    StringType strCommand = (StringType) command;
                    logger.debug("Changing override for hub {} to {}", channelUID, strCommand.toString());
                    try {
                        OverrideMode mode = OverrideMode.getByName(strCommand.toFullString());
                        ht.getConnection().setOverride(h, mode);
                    } catch (NoboCommunicationException nce) {
                        logger.error("Failed setting override mode", nce);
                    } catch (NoboDataException nde) {
                        logger.error("Date format error setting override mode", nde);
                    }
                } else {
                    logger.error("Command of wrong type: {} ({})", command, command.getClass().getName());
                }
            } else {
                if (null == hub) {
                    logger.error("Could not set override, hub not detected yet");
                }

                if (null == hubThread) {
                    logger.error("Could not set override, hub connection thread not set up yet");
                }
            }
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

        NoboHubBridgeConfiguration c = Helpers.castToNonNull(config, "config");

        String serialNumber = c.serialNumber;
        if (null == serialNumber)
        {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing serial number in configuration");
            return;
        }

        String hostName = c.hostName;
        if (null == hostName)
        {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing host name in configuration");
            return;
        }

        logger.info("Looking for Hub {} at {}", c.serialNumber, c.hostName);

        // Set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        updateStatus(ThingStatus.UNKNOWN);

        // Background handshake:
        scheduler.execute(() -> {
            try {
                HubConnection conn = new HubConnection(hostName, serialNumber, this);
                conn.connect();

                logger.debug("Done connecting to {} ({})", hostName, serialNumber);

                Duration timeout = Duration.ofSeconds(14);
                if (c.pollingInterval > 0) {
                    timeout = Duration.ofSeconds(c.pollingInterval);
                }

                logger.debug("Starting communication thread to {}", hostName);

                HubCommunicationThread ht = new HubCommunicationThread(conn, timeout);
                ht.start();
                hubThread = ht;

                if (ht.getConnection().isConnected()) {
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
        if (hubThread != null) {
            logger.info("Stopping communication thread");
            HubCommunicationThread ht = Helpers.castToNonNull(hubThread, "hubThread");
            ht.stopNow();
        }
    }

    @java.lang.Override
    public void childHandlerInitialized(ThingHandler handler, Thing thing) {
        logger.info("Adding thing: {}", thing.getLabel());
    }

    @java.lang.Override
    public void childHandlerDisposed(ThingHandler handler, Thing thing) {
        logger.info("Disposing thing: {}", thing.getLabel());
    }

    private void onUpdate(Hub hub) {
        this.hub = hub;
        Override activeOverride = getOverride(hub.getActiveOverrideId());

        if (null != activeOverride) {
            Override o = Helpers.castToNonNull(activeOverride, "activeOverride");
            updateState(NoboHubBindingConstants.CHANNEL_HUB_ACTIVE_OVERRIDE_NAME, StringType.valueOf(o.getMode().name()));
        }

        updateProperty("name", hub.getName());
        updateProperty("serialNumber", hub.getSerialNumber().toString());
        updateProperty("softwareVersion", hub.getSoftwareVersion());
        updateProperty("hardwareVersion", hub.getHardwareVersion());
        updateProperty("productionDate", hub.getProductionDate());
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
            if (null != discoveryService)
            {
                NoboThingDiscoveryService ds = Helpers.castToNonNull(discoveryService, "discoveryService");
                ds.detectZones(zoneRegister.values());
            }
        } else if (line.startsWith("H02")) {
            Component component = Component.fromH02(line);
            componentRegister.put(component.getSerialNumber(), component);
            if (null != discoveryService)
            {
                NoboThingDiscoveryService ds = Helpers.castToNonNull(discoveryService, "discoveryService");
                ds.detectComponents(componentRegister.values());
            }
        } else if (line.startsWith("H03")) {
            WeekProfile weekProfile = WeekProfile.fromH03(line);
            weekProfileRegister.put(weekProfile.getId(), weekProfile);
        } else if (line.startsWith("H04")) {
            Override override = Override.fromH04(line);
            overrideRegister.put(override.getId(), override);
        } else if (line.startsWith("H05")) {
            Hub hub = Hub.fromH05(line);
            onUpdate(hub);
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
            if (null != discoveryService)
            {
                NoboThingDiscoveryService ds = Helpers.castToNonNull(discoveryService, "discoveryService");
                ds.detectZones(zoneRegister.values());
            }
        } else if (line.startsWith("B01")) {
            Component component = Component.fromH02(line);
            componentRegister.put(component.getSerialNumber(), component);
            if (null != discoveryService)
            {
                NoboThingDiscoveryService ds = Helpers.castToNonNull(discoveryService, "discoveryService");
                ds.detectComponents(componentRegister.values());
            }
        } else if (line.startsWith("B02")) {
            WeekProfile weekProfile = WeekProfile.fromH03(line);
            weekProfileRegister.put(weekProfile.getId(), weekProfile);
        } else if (line.startsWith("B03")) {
            Override override = Override.fromH04(line);
            overrideRegister.put(override.getId(), override);
        } else if (line.startsWith("V00")) {
            Zone zone = Zone.fromH01(line);
            zoneRegister.replace(zone.getId(), zone);
            refreshZone(zone);
        } else if (line.startsWith("V01")) {
            Component component = Component.fromH02(line);
            componentRegister.replace(component.getSerialNumber(), component);
            refreshComponent(component);
        } else if (line.startsWith("V02")) {
            WeekProfile weekProfile = WeekProfile.fromH03(line);
            weekProfileRegister.replace(weekProfile.getId(), weekProfile);
        } else if (line.startsWith("V03")) {
            Hub hub = Hub.fromH05(line);
            onUpdate(hub);
        } else if (line.startsWith("Y02")) {
            String parts[] = line.split(" ", 3);
            SerialNumber serialNumber = new SerialNumber(parts[1]);
            try {
                if (parts[2] == null) {
                    throw new NoboDataException("Missing temperature data");
                }

                double temp = Double.parseDouble(parts[2]);
                Component component = getComponent(serialNumber);
                if (null != component) {
                    Component c = Helpers.castToNonNull(component, "component");
                    c.setTemperature(temp);
                    refreshComponent(c);
                    int zoneId = c.getTemperatureSensorForZoneId();
                    if (zoneId >= 0) {
                        Zone zone = getZone(zoneId);
                        if (null != zone) {
                            Zone z = Helpers.castToNonNull(zone, "zone");
                            z.setTemperature(temp);
                            refreshZone(z);
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

    public @Nullable Component getComponent(SerialNumber serialNumber) {
        return componentRegister.get(serialNumber);
    }

    public @Nullable Override getOverride(Integer id) {
        return overrideRegister.get(id);
    }

    public void sendCommand(String command) {
        if (hubThread != null) {
            HubCommunicationThread ht = Helpers.castToNonNull(hubThread, "hubThread");
            HubConnection conn = ht.getConnection();
            conn.sendCommand(command);
        }
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
                if (handler != null) {
                    SerialNumber handlerSerial = handler.getSerialNumber();
                    if (handlerSerial != null && component.getSerialNumber().equals(handlerSerial)) {
                        handler.onUpdate(component);
                    }
                }
            }
        });
    }

    public void startScan() {
        try {
            if (hubThread != null)
            {
                HubCommunicationThread ht = Helpers.castToNonNull(hubThread, "hubThread");
                ht.getConnection().refreshAll();
            }
        } catch (NoboCommunicationException noboEx) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Failed to get status: " + noboEx.getMessage());
        }
    }

    public void setDicsoveryService(NoboThingDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }
}
