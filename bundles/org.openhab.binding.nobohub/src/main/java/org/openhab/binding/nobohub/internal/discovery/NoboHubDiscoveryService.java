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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * This class identifies devices that are available on the Nobø hub and adds discovery results for them.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.nobohub")
public class NoboHubDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(NoboHubDiscoveryService.class);

    public NoboHubDiscoveryService() {
        super(new HashSet<>(Arrays.asList(THING_TYPE_HUB)), 10, true);
    }

    @Override
    protected void startScan() {
        scheduler.execute(scanner);
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

    private final Runnable scanner = new Runnable() {
        @Override
        public void run() {
            boolean found = false;
            logger.info("Detecting Glen Dimplex Nobø Hubs, trying Multicast");
            try {
                MulticastSocket socket = new MulticastSocket(NOBO_HUB_MULTICAST_PORT);
                found = waitOnSocket(socket, "multicast");
            } catch (IOException ioex) {
                logger.error("Failed detecting Nobø Hub multicast", ioex);
            }

            if (!found) {
                logger.info("Detecting Glen Dimplex Nobø Hubs, trying Broadcast");

                try {
                    DatagramSocket socket = new DatagramSocket(NOBO_HUB_BROADCAST_PORT, InetAddress.getByName(NOBO_HUB_BROADCAST_ADDRESS));
                    found = waitOnSocket(socket, "broadcast");
                } catch (IOException ioex) {
                    logger.error("Failed detecting Nobø Hub broadcast", ioex);
                }
            }
        }

        private boolean waitOnSocket(DatagramSocket socket, String type) throws IOException {
            try {
                socket.setBroadcast(true);

                byte[] buffer = new byte[1024];
                DatagramPacket data = new DatagramPacket(buffer, buffer.length);
                String received = "";
                while (!received.startsWith("__NOBOHUB__")) {
                    socket.setSoTimeout((int) Duration.ofSeconds(4).toMillis());
                    socket.receive(data);
                    received = new String(buffer, 0, data.getLength());
                }

                logger.debug("Hub detection {}}: Received: {} from {}", type, received, data.getAddress());    

                String parts[] = received.split("__", 3);
                if (3 != parts.length) {
                    logger.debug("Data error, didn't contain three parts: '{}''", String.join("','", parts));
                    return false;
                }

                String serialNumberStart = parts[parts.length - 1];
                addDevice(serialNumberStart, data.getAddress().getHostName());
                return true;
            } finally {
                socket.close();
            }
        }

        private void addDevice(String serialNumberStart, String hostName) {
            ThingUID bridge = new ThingUID(THING_TYPE_HUB, serialNumberStart);
            String label = "Nobø Hub " + serialNumberStart;

            Map<String, Object> properties = new HashMap<>(1);
            properties.put("serialNumber", serialNumberStart);
            properties.put("name", label);
            properties.put("vendor", VENDOR);
            properties.put("hostName", hostName);

            logger.debug("Adding device {} to inbox: {} {} at {}", bridge, label, serialNumberStart, hostName);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(bridge)
                    .withLabel(label).withProperties(properties).withRepresentationProperty("serialNumber").build();
            thingDiscovered(discoveryResult);
        }
    };
}
