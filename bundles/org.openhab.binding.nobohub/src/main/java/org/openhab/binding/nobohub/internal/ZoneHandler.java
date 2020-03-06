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

import java.time.LocalDateTime;

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
import org.openhab.binding.nobohub.model.NoboDataException;
import org.openhab.binding.nobohub.model.WeekProfile;
import org.openhab.binding.nobohub.model.WeekProfileStatus;
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

        DecimalType comfortTemperature = new DecimalType(zone.getComfortTemperature());
        updateState(CHANNEL_ZONE_COMFORT_TEMPERATURE, comfortTemperature);
        DecimalType ecoTemperature = new DecimalType(zone.getEcoTemperature());
        updateState(CHANNEL_ZONE_ECO_TEMPERATURE, ecoTemperature);

        Double temp = zone.getTemperature();
        if (temp != null) {
            DecimalType currentTemperature = new DecimalType(temp);
            updateState(CHANNEL_ZONE_CURRENT_TEMPERATURE, currentTemperature);
        }

        int activeWeekProfileId = zone.getActiveWeekProfileId();
        Bridge noboHub = getBridge();
        if (null != noboHub) {
            NoboHubBridgeHandler hubHandler = (NoboHubBridgeHandler) noboHub.getHandler();
            if (hubHandler != null) {
                WeekProfile weekProfile = hubHandler.getWeekProfile(activeWeekProfileId);
                if (null != weekProfile) {
                    updateState(CHANNEL_ZONE_WEEK_PROFILE_NAME,  StringType.valueOf(weekProfile.getName()));
                    try {
                        WeekProfileStatus weekProfileStatus = weekProfile.getStatusAt(LocalDateTime.now());
                        updateState(CHANNEL_ZONE_CALCULATED_WEEK_PROFILE_STATUS, StringType.valueOf(weekProfileStatus.name()));
                    } catch (NoboDataException nde) {
                        logger.error("Failed getting current week profile status", nde);
                    }    
                }
            }
        }

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

            if (null != id) {
                Zone zone = getZone();
                if (null == zone) {
                    logger.error("Could not find Zone with id {} for channel {}", id, channelUID);
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.GONE);
                } else {
                    onUpdate(zone);
                    Bridge noboHub = getBridge();
                    if (null != noboHub) {
                        NoboHubBridgeHandler hubHandler = (NoboHubBridgeHandler) noboHub.getHandler();
                        if (null != hubHandler) {
                            WeekProfile weekProfile = hubHandler.getWeekProfile(zone.getActiveWeekProfileId());
                            if (null != weekProfile) {
                                String weekProfileName = weekProfile.getName(); 
                                StringType weekProfileValue = StringType.valueOf(weekProfileName);
                                updateState(CHANNEL_ZONE_WEEK_PROFILE_NAME, weekProfileValue);
                            }
                        }
                    }
                }
            } else {
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.GONE);
                logger.error("id not set for channel {}", channelUID);
            }

            return;
        }

        if (CHANNEL_ZONE_COMFORT_TEMPERATURE.equals(channelUID.getId())) {
            Zone zone = getZone();
            if (zone != null) {
                if (command instanceof DecimalType) {
                    DecimalType comfortTemp = (DecimalType) command;
                    logger.debug("Set comfort temp for zone {} to {}", zone.getName(), comfortTemp.doubleValue());
                    zone.setComfortTemperature(comfortTemp.intValue());
                    sendCommand(zone.generateCommandString("U00"));
                }    
            }

            return;
        }

        if (CHANNEL_ZONE_ECO_TEMPERATURE.equals(channelUID.getId())) {
            Zone zone = getZone();
            if (zone != null) {
                if (command instanceof DecimalType) {
                    DecimalType ecoTemp = (DecimalType) command;
                    logger.debug("Set eco temp for zone {} to {}", zone.getName(), ecoTemp.doubleValue());
                    zone.setEcoTemperature(ecoTemp.intValue());
                    sendCommand(zone.generateCommandString("U00"));
                }
            }

            return;
        }

        logger.debug("Unhandled zone command {}: {}", channelUID.getId(), command);
    }

    public @Nullable Integer getZoneId() {
        return id;
    }

    private void sendCommand(String command) {
        Bridge noboHub = getBridge();
        if (null != noboHub) {
            NoboHubBridgeHandler hubHandler = (NoboHubBridgeHandler) noboHub.getHandler();
            if (null != hubHandler) {
                hubHandler.sendCommand(command);    
            }
        }
    }

    private @Nullable Zone getZone() {
        Bridge noboHub = getBridge();
        if (null != noboHub) {
            NoboHubBridgeHandler hubHandler = (NoboHubBridgeHandler) noboHub.getHandler();
            if (null != hubHandler && null != id) {
                Integer zid = Helpers.castToNonNull(id, "id");
                return hubHandler.getZone(zid);
            }
        }

        return null;
    }
}
