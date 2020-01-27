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
package org.openhab.binding.nobohub.internal.discovery;

import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.*;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.nobohub.internal.NoboHubBridgeHandler;
import org.openhab.binding.nobohub.model.Component;
import org.openhab.binding.nobohub.model.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class identifies devices that are available on the Nobø hub and adds discovery results for them.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class NoboThingDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(NoboThingDiscoveryService.class);

    private final NoboHubBridgeHandler bridgeHandler;

    public NoboThingDiscoveryService(NoboHubBridgeHandler bridgeHandler) {
        super(AUTODISCOVERED_THING_TYPES_UIDS, 10, true);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void startScan() {
        bridgeHandler.startScan();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime());
    }

    public void detectZones(Collection<Zone> zones) {
        ThingUID bridge = bridgeHandler.getThing().getUID();
        ThingTypeUID thingType = THING_TYPE_ZONE;

        for (Zone zone : zones) {
            ThingUID thingId = new ThingUID(thingType, bridge, Integer.toString(zone.getId()));
            String label = zone.getName();

            Map<String, Object> properties = new HashMap<>(1);
            properties.put("id", Integer.toString(zone.getId()));
            properties.put("name", zone.getName());
            properties.put("vendor", VENDOR);

            logger.debug("Adding device {} to inbox", thingId);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingId).withBridge(bridge)
                    .withLabel(label).withProperties(properties).withRepresentationProperty("id").build();
            thingDiscovered(discoveryResult);
        }
    }

    public void detectComponents(Collection<Component> components) {
        ThingUID bridge = bridgeHandler.getThing().getUID();
        ThingTypeUID thingType = THING_TYPE_COMPONENT;

        for (Component component : components) {
            ThingUID thingId = new ThingUID(thingType, bridge, component.getSerialNumber().toString());
            String label = component.getName();

            Map<String, Object> properties = new HashMap<>(1);
            properties.put("serialNumber", component.getSerialNumber().toString());
            properties.put("name", component.getName());
            properties.put("vendor", VENDOR);
            properties.put("model", component.getSerialNumber().getComponentType());

            String zoneName = getZoneName(component.getZoneId());
            if (zoneName != null) {
                properties.put("zone", zoneName);
            }
        
            String tempForZoneName = getZoneName(component.getTemperatureSensorForZoneId());
            if (tempForZoneName != null) {
                properties.put("temperatureSensorForZone", tempForZoneName);
            }
        
            logger.debug("Adding device {} to inbox", thingId);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingId).withBridge(bridge)
                    .withLabel(label).withProperties(properties).withRepresentationProperty("serialNumber").build();
            thingDiscovered(discoveryResult);
        }
    }

    private @Nullable String getZoneName(int zoneId) {
        Zone zone = bridgeHandler.getZone(zoneId);
        if (null == zone) {
            return null;
        }

        return zone.getName();
    }
}
