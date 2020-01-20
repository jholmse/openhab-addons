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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The normal week profile (used when no {@link Override}s exist).
 * 
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public final class WeekProfile {

    private final int id;
    private final String name;
    private final String profile;

    public WeekProfile(int id, String name, String profile) {
        this.id = id;
        this.name = name;
        this.profile = profile;
    }

    public static WeekProfile fromH03(String h03) throws NoboDataException
    {
        String parts[] = h03.split(" ", 4);

        if (parts.length != 4) {
            throw new NoboDataException(String.format("Unexpected number of parts from hub on H3 call: %d", parts.length));
        }

        return new WeekProfile(Integer.parseInt(parts[1]),
                            ModelHelper.toJavaString(parts[2]),
                            ModelHelper.toJavaString(parts[3]));        
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProfile() {
        return profile;
    }
}
