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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nobohub.internal.NoboHubBindingConstants;
import org.openhab.binding.nobohub.internal.NoboHubHandler;
import org.openhab.binding.nobohub.model.NoboCommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HubConnection {

    private final Logger logger = LoggerFactory.getLogger(HubConnection.class);

    private @NonNull final InetAddress host;
    private @NonNull final NoboHubHandler hubHandler;
    private @NonNull final String serialNumber;

    private @Nullable Socket hubConnection;
    private @Nullable PrintWriter out;
    private @Nullable BufferedReader in;


    public HubConnection(String hostName, String serialNumber, NoboHubHandler hubHandler) throws NoboCommunicationException
    {
        try {
            host = InetAddress.getByName(hostName);
        } catch (IOException ioex) {
            throw new NoboCommunicationException(String.format("Failed to resolve IP address of %s", hostName));
        }

        this.hubHandler = hubHandler;
        this.serialNumber = serialNumber;
    }

    public boolean connect() throws NoboCommunicationException
    {
        connectSocket();

        String hello = String.format("HELLO %s %s %s\r", NoboHubBindingConstants.API_VERSION, serialNumber, getDateString());
        write(hello);
        String helloRes = readLine();
        if (null == helloRes || !helloRes.startsWith("HELLO"))
        {
            if (helloRes.startsWith("REJECT"))
            {
                String reject[] = helloRes.split(" ", 2);
                throw new NoboCommunicationException(String.format("Hub rejects us with reason %s", reject[1]));
            } else {
                throw new NoboCommunicationException(String.format("Hub rejects us with unknown reason"));
            }
        }

        write("HANDSHAKE\r");
        String handshakeRes = readLine();
        if (null == handshakeRes || !helloRes.startsWith("HANDSHAKE"))
        {
            throw new NoboCommunicationException(String.format("Hub rejects handshake"));
        }

        refreshAllNoReconnect();
        return true;
    }

    public void handshake() throws NoboCommunicationException
    {
        if (!isConnected())
        {
            connect();
        } else {
            write("HANDSHAKE\r");
        }
    }

    public void refreshAll() throws NoboCommunicationException
    {
        if (!isConnected()) {
            connect();
        }
        else
        {
            refreshAllNoReconnect();
        }
    }

    private void refreshAllNoReconnect() throws NoboCommunicationException
    {
        if (!isConnected()) {
            connect();
        }
        else
        {
            write("G00\r");

            String line = "";
            while (!line.startsWith("H05"))
            {
                line = readLine();
                hubHandler.receivedData(line);
            }
        }
    }

    public boolean isConnected()
    {
        return hubConnection != null && hubConnection.isConnected();
    }

    private String readLine() throws NoboCommunicationException {
        try {
            String line = in.readLine();
            logger.info("NOBØ HUB: Read {}", line);
            return line;    
        } catch (IOException ioex) {
            throw new NoboCommunicationException("Failed reading from Nobø Hub", ioex);
        }
    }

    private void write(String s)
    {
        logger.info("NOBØ HUB: Sending {}", s);
        out.write(s);
        out.flush();
    }

    private void connectSocket() throws NoboCommunicationException
    {
        try {
            hubConnection = new Socket(host, NoboHubBindingConstants.NOBO_HUB_TCP_PORT);
            out = new PrintWriter(hubConnection.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(hubConnection.getInputStream()));    
        } catch (IOException ioex) {
            throw new NoboCommunicationException(String.format("Failed connecting to Nobø Hub at %s", host.getHostName()), ioex);
        }
    }

    private String getDateString()
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        return format.format(new Date());
    }

}