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
package org.openhab.binding.nobohub.model;

public final class ModelHelper {

    /**
     * Converts a String returned form Nobø hub to a normal Java string.
     * 
     * @param noboString String where Char 160 (nobr space is used for space)
     * @return String with normal spaces.
     */
    static String toJavaString(final String noboString)
    {
        return noboString.replace((char) 160, ' ');
    }

}