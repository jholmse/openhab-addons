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
import org.eclipse.jdt.annotation.Nullable;

/**
 * Shows information about a Component in the Nobø Hub.
 * 
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class Helpers {

    public static <T> T castToNonNull(@Nullable T value, @Nullable String msg) {
        if (null == value) {
            throw new IllegalArgumentException(msg);
        }

        return value;
    }
}
