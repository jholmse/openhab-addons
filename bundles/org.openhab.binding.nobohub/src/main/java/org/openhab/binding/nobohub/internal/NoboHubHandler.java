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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    private @Nullable Socket hubConnection;
    private @Nullable PrintWriter out;
    private @Nullable BufferedReader in;

    public NoboHubHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("NOBØ HUB: Handle command!");
        if (CHANNEL_ACTIVE_OVERRIDE_ID.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                try {
                    refreshAll();
                } catch (IOException ioex) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Failed to get status: " + ioex.getMessage());
                }
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
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
            boolean thingReachable = false;

            try {
                connectSocket();

                String hello = String.format("HELLO %s %s %s\r", NoboHubBindingConstants.API_VERSION, config.serialNumber, getDateString());
                write(hello);
                String helloRes = readLine();
                if (null == helloRes || !helloRes.startsWith("HELLO"))
                {
                    if (helloRes.startsWith("REJECT"))
                    {
                        String reject[] = helloRes.split(" ", 2);
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, String.format("Hub rejects us with reason %s", reject[1]));
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Hub rejects us");
                    }
                }

                write("HANDSHAKE\r");
                String handshakeRes = readLine();
                if (null == handshakeRes || !helloRes.startsWith("HANDSHAKE"))
                {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Hub rejects us");
                }

                refreshAll();
                thingReachable = true;

            } catch (IOException ioex) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ioex.getMessage());
            }

            if (thingReachable) {
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

    private void refreshAll() throws IOException
    {
        write("G00\r");

        String line = "";
        while (!line.startsWith("H05"))
        {
            line = readLine();
            parseLine(line);
        }
    }

    private String readLine() throws IOException {
        String line = in.readLine();
        logger.info("NOBØ HUB: Read {}", line);
        return line;
    }

    private void write(String s)
    {
        logger.info("NOBØ HUB: Sending {}", s);
        out.write(s);
        out.flush();
    }

    private void connectSocket() throws IOException
    {
        InetAddress host = InetAddress.getByName(config.hostName);
        hubConnection = new Socket(host, NoboHubBindingConstants.NOBO_HUB_TCP_PORT);
        out = new PrintWriter(hubConnection.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(hubConnection.getInputStream()));
    }

    private String getDateString()
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        return format.format(new Date());
    }

    private void parseLine(String line)
    {
        if (null == line) {
            return;
        }

        if (line.startsWith("H05"))
        {
            String parts[] = line.split(" ", 8);
            String serialNumber = parts[1];
            String name = parts[2];
            String activeOverrideId = parts[4];
            String softwareVersion = parts[5];
            String hardwareVersion = parts[6];
            String productionDate = parts[7];

            updateState(NoboHubBindingConstants.CHANNEL_SERIAL_NUMBER, StringType.valueOf(serialNumber));
            updateState(NoboHubBindingConstants.CHANNEL_NAME, StringType.valueOf(name));
            updateState(NoboHubBindingConstants.CHANNEL_ACTIVE_OVERRIDE_ID, new DecimalType(Integer.parseInt(activeOverrideId)));
            updateState(NoboHubBindingConstants.CHANNEL_SOFTWARE_VERSION, StringType.valueOf(softwareVersion));
            updateState(NoboHubBindingConstants.CHANNEL_HARDWARE_VERSION, StringType.valueOf(hardwareVersion));
            updateState(NoboHubBindingConstants.CHANNEL_PRODUCTION_DATE, StringType.valueOf(productionDate));
        }
    }
}
