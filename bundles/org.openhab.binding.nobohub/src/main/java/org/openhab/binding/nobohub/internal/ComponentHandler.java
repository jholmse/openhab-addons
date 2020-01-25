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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.nobohub.model.Component;
import org.openhab.binding.nobohub.model.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows information about a Component in the Nobø Hub.
 * 
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class ComponentHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ComponentHandler.class);

    protected @Nullable String serialNumber;

    public ComponentHandler(Thing thing) {
        super(thing);
    }

    public void onUpdate(Component component) {
        updateStatus(ThingStatus.ONLINE);

        Double temp = component.getTemperature();
        if (temp != null) {
            DecimalType currentTemperature = new DecimalType(temp);
            updateState(CHANNEL_COMPONENT_CURRENT_TEMPERATURE, currentTemperature);
        }

        updateProperty("serialNumber", component.getSerialNumber());
        updateProperty("name", component.getName());

        String zoneName = getZoneName(component.getZoneId());
        if (zoneName != null) {
            updateProperty("zone", zoneName);
        }

        String tempForZoneName = getZoneName(component.getTemperatureSensorForZoneId());
        if (tempForZoneName != null) {
            updateProperty("temperatureSensorForZone", tempForZoneName);
        }
    }

    private @Nullable String getZoneName(int zoneId) {
        Bridge noboHub = getBridge();
        NoboHubBridgeHandler hubHandler = (NoboHubBridgeHandler) noboHub.getHandler();
        Zone zone = hubHandler.getZone(zoneId);
        if (null == zone) {
            return null;
        }

        return zone.getName();
    }

    @Override 
    public void initialize() {
        this.serialNumber = getConfigAs(ComponentConfiguration.class).serialNumber;
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refreshing channel {}", channelUID);
            Bridge noboHub = getBridge();
            NoboHubBridgeHandler hubHandler = (NoboHubBridgeHandler) noboHub.getHandler();

            if (null != serialNumber) {
                String realSerialNumber = serialNumber;
                Component component = hubHandler.getComponent(realSerialNumber);
                if (null == component) {
                    logger.error("Could not find Component with serial number {} for channel {}", serialNumber, channelUID);
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.GONE);
                } else {
                    onUpdate(component);
                }
            } else {
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.GONE);
                logger.error("id not set for channel {}", channelUID);
            }

            return;
        }

        logger.debug("The component is a read-only device and cannot handle commands.");
    }

    public @Nullable String getSerialNumber() {
        return serialNumber;
    }
}
