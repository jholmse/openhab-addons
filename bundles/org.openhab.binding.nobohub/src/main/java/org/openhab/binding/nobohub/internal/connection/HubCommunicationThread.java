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
package org.openhab.binding.nobohub.internal.connection;

import java.time.Duration;
import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nobohub.internal.NoboHubBindingConstants;
import org.openhab.binding.nobohub.internal.model.NoboCommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread that reads from the Nobø Hub and sends HANDSHAKEs to keep the connection open.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class HubCommunicationThread extends Thread {

    private final Logger logger = LoggerFactory.getLogger(HubCommunicationThread.class);

    private final HubConnection hubConnection;
    private final Duration timeout;
    private LocalDateTime lastTimeFullScan;
    private LocalDateTime lastTimeReadStart;

    private volatile boolean stopped = false;

    public HubCommunicationThread(HubConnection hubConnection, Duration timeout) {
        this.hubConnection = hubConnection;
        this.timeout = timeout;
        this.lastTimeFullScan = LocalDateTime.now();
        this.lastTimeReadStart = LocalDateTime.now();
    }

    public void stopNow() {
        stopped = true;
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                if (hubConnection.hasData()) {
                    hubConnection.processReads(timeout);
                }

                if (LocalDateTime.now().isAfter(lastTimeFullScan.plus(NoboHubBindingConstants.TIME_BETWEEN_FULL_SCANS))) {
                    hubConnection.refreshAll();
                    lastTimeFullScan = LocalDateTime.now();
                }
                else {
                    hubConnection.handshake();
                }

                lastTimeReadStart = LocalDateTime.now();
                hubConnection.processReads(timeout);
            } catch (NoboCommunicationException nce) {
                logger.error("Communication error with Hub", nce);
                try {
                    Duration readTime = Duration.between(LocalDateTime.now(), lastTimeReadStart);
                    Thread.sleep(NoboHubBindingConstants.TIME_BETWEEN_RETRIES_ON_ERROR.minus(readTime).toMillis());
                    try {
                        logger.debug("Trying to do a hard reconnect");
                        hubConnection.hardReconnect();
                    } catch (NoboCommunicationException nce2) {
                        logger.error("Failed to reconnect connection", nce2);
                    }
                } catch (InterruptedException ie) {
                    logger.debug("Interrupted from sleep after error");
                }
            }
        }

        try {
            if (stopped) {
                hubConnection.disconnect();
            }
        } catch (NoboCommunicationException nce) {
            logger.error("Error disconnecting from Hub", nce);
        }
    }

    public HubConnection getConnection() {
        return hubConnection;
    }
}
