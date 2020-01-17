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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NoboHubConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class NoboHubConfiguration {

    /**
     * Serial Number of Nobø Hub.
     */
    public String serialNumber;

    /**
     * Host address of Nobø Hub.
     */
    public String hostName;

    /**
     * Polling interval (seconds)
     */
    public int pollingInterval;
}
