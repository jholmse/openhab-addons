/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.nobohub.internal.discovery;

import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.AUTODISCOVERED_THING_TYPES_UIDS;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.THING_TYPE_COMPONENT;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.THING_TYPE_ZONE;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.VENDOR;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.nobohub.internal.NoboHubBridgeHandler;
import org.openhab.binding.nobohub.internal.model.Component;
import org.openhab.binding.nobohub.internal.model.Zone;
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
        List<Thing> things = bridgeHandler.getThing().getThings();

        for (Zone zone : zones) {
            ThingUID discoveredThingId = new ThingUID(THING_TYPE_ZONE, bridge, Integer.toString(zone.getId()));

            boolean addDiscoveredZone = true;
            for (Thing thing : things) {
                if (thing.getUID().equals(discoveredThingId)) {
                    addDiscoveredZone = false;
                }
            }

            if (addDiscoveredZone) {
                String label = zone.getName();

                Map<String, Object> properties = new HashMap<>(1);
                properties.put("id", Integer.toString(zone.getId()));
                properties.put("name", zone.getName());
                properties.put("vendor", VENDOR);

                logger.debug("Adding device {} to inbox", discoveredThingId);
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(discoveredThingId).withBridge(bridge)
                        .withLabel(label).withProperties(properties).withRepresentationProperty("id").build();
                thingDiscovered(discoveryResult);
            }
        }
    }

    public void detectComponents(Collection<Component> components) {
        ThingUID bridge = bridgeHandler.getThing().getUID();
        List<Thing> things = bridgeHandler.getThing().getThings();

        for (Component component : components) {
            ThingUID discoveredThingId = new ThingUID(THING_TYPE_COMPONENT, bridge,
                    component.getSerialNumber().toString());

            boolean addDiscoveredComponent = true;
            for (Thing thing : things) {
                if (thing.getUID().equals(discoveredThingId)) {
                    addDiscoveredComponent = false;
                }
            }

            if (addDiscoveredComponent) {
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

                int zoneId = component.getTemperatureSensorForZoneId();
                if (zoneId >= 0) {
                    String tempForZoneName = getZoneName(zoneId);
                    if (tempForZoneName != null) {
                        properties.put("temperatureSensorForZone", tempForZoneName);
                    }
                }

                logger.debug("Adding device {} to inbox", discoveredThingId);
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(discoveredThingId).withBridge(bridge)
                        .withLabel(label).withProperties(properties).withRepresentationProperty("serialNumber").build();
                thingDiscovered(discoveryResult);
            }
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
