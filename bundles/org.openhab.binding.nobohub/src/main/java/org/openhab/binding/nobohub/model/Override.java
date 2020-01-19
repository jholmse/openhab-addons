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

public final class Override {

    private final int id;
    private final OverrideMode mode;
    private final OverrideType type;
    private final int startTime;
    private final int endTime;
    private final OverrideTarget target;
    private final int targetId;

    public Override(int id, OverrideMode mode, OverrideType type, int startTime, int endTime, OverrideTarget target, int targetId)
    {
        this.id = id;
        this.mode = mode;
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.target = target;
        this.targetId = targetId;
    }

    public static Override fromH04(String h04) throws NoboDataException
    {
        String parts[] = h04.split(" ", 8);

        if (parts.length != 8) {
            throw new NoboDataException(String.format("Unexpected number of parts from hub on H4 call: %d", parts.length));
        }

        return new Override(Integer.parseInt(parts[1]),
                            OverrideMode.getByNumber(Integer.parseInt(parts[2])),
                            OverrideType.getByNumber(Integer.parseInt(parts[3])),
                            Integer.parseInt(parts[4]),
                            Integer.parseInt(parts[5]),
                            OverrideTarget.getByNumber(Integer.parseInt(parts[6])),
                            Integer.parseInt(parts[7]));
    }

    public int getId() {
        return id;
    }

    public OverrideMode getMode() {
        return mode;
    }

    public OverrideType getType() {
        return type;
    }

    public int startTime() {
        return startTime;
    }

    public int endTime() {
        return endTime;
    }

    public OverrideTarget getTarget() {
        return target;
    }

    public int getTargetId() {
        return targetId;
    }

}