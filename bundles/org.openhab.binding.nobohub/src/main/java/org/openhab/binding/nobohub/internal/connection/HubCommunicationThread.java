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

import org.eclipse.jdt.annotation.NonNullByDefault;
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

    private volatile boolean stopped = false;

    public HubCommunicationThread(HubConnection hubConnection, Duration timeout) {
        this.hubConnection = hubConnection;
        this.timeout = timeout;
    }

    public void stopNow() {
        stopped = true;
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                hubConnection.handshake();
                hubConnection.processReads(timeout);
            } catch (NoboCommunicationException nce) {
                logger.error("Communication error with Hub", nce);
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
