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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nobohub.internal.NoboHubBindingConstants;
import org.openhab.binding.nobohub.internal.NoboHubBridgeHandler;
import org.openhab.binding.nobohub.model.NoboCommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection to the Nobø Hub (Socket wrapper).
 * 
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class HubConnection {

    private final Logger logger = LoggerFactory.getLogger(HubConnection.class);

    private final InetAddress host;
    private final NoboHubBridgeHandler hubHandler;
    private final String serialNumber;

    private @Nullable Socket hubConnection;
    private @Nullable PrintWriter out;
    private @Nullable BufferedReader in;


    public HubConnection(String hostName, String serialNumber, NoboHubBridgeHandler hubHandler) throws NoboCommunicationException {
        try {
            host = InetAddress.getByName(hostName);
        } catch (IOException ioex) {
            throw new NoboCommunicationException(String.format("Failed to resolve IP address of %s", hostName));
        }

        this.hubHandler = hubHandler;
        this.serialNumber = serialNumber;
    }

    public boolean connect() throws NoboCommunicationException {
        connectSocket();

        String hello = String.format("HELLO %s %s %s\r", NoboHubBindingConstants.API_VERSION, serialNumber, getDateString());
        write(hello);
        String helloRes = readLine();
        if (null == helloRes || !helloRes.startsWith("HELLO")) {
            if (helloRes != null && helloRes.startsWith("REJECT")) {
                String reject[] = helloRes.split(" ", 2);
                throw new NoboCommunicationException(String.format("Hub rejects us with reason %s: %s", reject[1], NoboHubBindingConstants.REJECT_REASONS.get(reject[1])));
            } else {
                throw new NoboCommunicationException(String.format("Hub rejects us with unknown reason"));
            }
        }

        write("HANDSHAKE\r");
        String handshakeRes = readLine();
        if (null == handshakeRes || !handshakeRes.startsWith("HANDSHAKE")) {
            throw new NoboCommunicationException(String.format("Hub rejects handshake"));
        }

        refreshAllNoReconnect();
        return true;
    }

    public void handshake() throws NoboCommunicationException {
        if (!isConnected()) {
            connect();
        } else {
            write("HANDSHAKE\r");
        }
    }

    public void refreshAll() throws NoboCommunicationException {
        if (!isConnected()) {
            connect();
        } else {
            refreshAllNoReconnect();
        }
    }

    private void refreshAllNoReconnect() throws NoboCommunicationException {
        if (!isConnected()) {
            connect();
        } else {
            write("G00\r");

            String line = "";
            while (line != null && !line.startsWith("H05")) {
                line = readLine();
                hubHandler.receivedData(line);
            }
        }
    }

    public boolean isConnected() {
        return hubConnection != null && hubConnection.isConnected();
    }

    public void processReads(Duration timeout) throws NoboCommunicationException {
        try {
            if (null == hubConnection) {
                throw new NoboCommunicationException("No connection to Hub");
            }

            logger.debug("Reading from Hub, waiting maximum {}", timeout);
            hubConnection.setSoTimeout((int) timeout.toMillis());

            try {
                String line = readLine();
                if (line != null && line.startsWith("HANDSHAKE")) {
                    line = readLine();
                }
    
                hubHandler.receivedData(line);
            } catch (NoboCommunicationException nce) {
                if (!(nce.getCause() instanceof SocketTimeoutException)) {
                    connectSocket();
                }
            }
        } catch (SocketException se) {
            throw new NoboCommunicationException("Failed setting read timeout", se);
        }
    }

    private @Nullable String readLine() throws NoboCommunicationException {
        try {
            String line = in.readLine();
            logger.debug("Reading '{}'", line);
            return line;    
        } catch (IOException ioex) {
            throw new NoboCommunicationException("Failed reading from Nobø Hub", ioex);
        }
    }

    private void write(String s) {
        logger.debug("Sending '{}'", s);
        out.write(s);
        out.flush();
    }

    private void connectSocket() throws NoboCommunicationException {
        try {
            hubConnection = new Socket(host, NoboHubBindingConstants.NOBO_HUB_TCP_PORT);
            out = new PrintWriter(hubConnection.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(hubConnection.getInputStream()));    
        } catch (IOException ioex) {
            throw new NoboCommunicationException(String.format("Failed connecting to Nobø Hub at %s", host.getHostName()), ioex);
        }
    }

    public void disconnect() throws NoboCommunicationException {
        try {
            if (out != null) {
                out.close();
            }

            if (in != null) {
                in.close();
            }

            if (hubConnection != null) {
                hubConnection.close();
            }
        } catch (IOException ioex) {
            throw new NoboCommunicationException("Error disconnecting from Hub", ioex);
        }
    }

    private String getDateString() {
        return LocalDateTime.now().format(NoboHubBindingConstants.DATE_FORMAT_SECONDS);
    }
}
