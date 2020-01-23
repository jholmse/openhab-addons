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
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.nobohub.model.WeekProfile;
import org.openhab.binding.nobohub.model.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows information about a named Zone in the Nobø Hub.
 * 
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class ZoneHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ZoneHandler.class);

    protected @Nullable Integer id;

    public ZoneHandler(Thing thing) {
        super(thing);
    }

    public void onUpdate(Zone zone) {

        updateStatus(ThingStatus.ONLINE);

        DecimalType activeWeekProfile = new DecimalType(zone.getActiveWeekProfileId());
        updateState(CHANNEL_ZONE_WEEK_PROFILE_ID, activeWeekProfile);
        DecimalType comfortTemperature = new DecimalType(zone.getComfortTemperature());
        updateState(CHANNEL_ZONE_COMFORT_TEMPERATURE, comfortTemperature);
        DecimalType ecoTemperature = new DecimalType(zone.getEcoTemperature());
        updateState(CHANNEL_ZONE_ECO_TEMPERATURE, ecoTemperature);

        updateProperty("name", zone.getName());
        updateProperty("id", Integer.toString(zone.getId()));
    }

    @Override 
    public void initialize() {
        this.id = getConfigAs(ZoneConfiguration.class).id;
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refreshing channel {}", channelUID);
            Bridge noboHub = getBridge();
            NoboHubBridgeHandler hubHandler = (NoboHubBridgeHandler) noboHub.getHandler();

            if (null != id) {
                Integer realId = id;
                Zone zone = hubHandler.getZone(realId);
                if (null == zone) {
                    logger.error("Could not find Zone with id {} for channel {}", id, channelUID);
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.GONE);
                } else {
                    onUpdate(zone);
                    WeekProfile weekProfile = hubHandler.getWeekProfile(zone.getActiveWeekProfileId());
                    if (weekProfile != null) {
                        String weekProfileName = weekProfile.getName(); 
                        if (weekProfileName != null) {
                            StringType weekProfileValue = StringType.valueOf(weekProfileName);
                            updateState(CHANNEL_ZONE_WEEK_PROFILE_NAME, weekProfileValue);
                        }
                    }
                }    
            } else {
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.GONE);
                logger.error("id not set for channel {}", channelUID);
            }

            return;
        }

        logger.debug("The sensor is a read-only device and cannot handle commands.");
    }
}