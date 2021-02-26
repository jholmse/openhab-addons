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

import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.THING_TYPE_COMPONENT;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.THING_TYPE_HUB;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.THING_TYPE_ZONE;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.nobohub.internal.discovery.NoboThingDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NoboHubHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jørgen Austvik - Initial contribution
 * @author Espen Fossen - Added support for week profile
 */
@NonNullByDefault
@Component(configurationPid = "binding.nobohub", service = ThingHandlerFactory.class)
public class NoboHubHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(NoboHubHandlerFactory.class);
    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private @NonNullByDefault({}) WeekProfileStateDescriptionOptionsProvider stateDescriptionOptionsProvider;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_HUB.equals(thingTypeUID)) {
            NoboHubBridgeHandler handler = new NoboHubBridgeHandler((Bridge) thing);
            registerDiscoveryService(handler);
            return handler;
        } else if (THING_TYPE_ZONE.equals(thingTypeUID)) {
            return new ZoneHandler(thing, stateDescriptionOptionsProvider);
        } else if (THING_TYPE_COMPONENT.equals(thingTypeUID)) {
            return new ComponentHandler(thing);
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof NoboHubBridgeHandler) {
            unregisterDiscoveryService((NoboHubBridgeHandler) thingHandler);
        }
    }

    private synchronized void registerDiscoveryService(NoboHubBridgeHandler bridgeHandler) {
        NoboThingDiscoveryService discoveryService = new NoboThingDiscoveryService(bridgeHandler);
        bridgeHandler.setDicsoveryService(discoveryService);
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), getBundleContext()
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    private synchronized void unregisterDiscoveryService(NoboHubBridgeHandler bridgeHandler) {
        try {
            @Nullable ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(bridgeHandler.getThing().getUID());
            if (null != serviceReg) {
                ServiceRegistration<?> sr = Helpers.castToNonNull(serviceReg, "serviceReg");
                @Nullable NoboThingDiscoveryService service = (NoboThingDiscoveryService) getBundleContext().getService(sr.getReference());
                sr.unregister();
                if (service != null) {
                    NoboThingDiscoveryService s = Helpers.castToNonNull(service, "service");
                    s.deactivate();
                }
            }
        } catch (IllegalArgumentException iae) {
            logger.error("Failed to unregister service", iae);
        }
    }

    @Reference
    protected void setDynamicStateDescriptionProvider(WeekProfileStateDescriptionOptionsProvider provider) {
        this.stateDescriptionOptionsProvider = provider;
    }

    protected void unsetDynamicStateDescriptionProvider(WeekProfileStateDescriptionOptionsProvider provider) {
        this.stateDescriptionOptionsProvider = null;
    }
}
