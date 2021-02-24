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
package org.openhab.binding.nobohub.internal.model;

import static org.junit.Assert.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;

/**
 * Unit tests for Override model object.
 * 
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class OverrideRegisterTest {

    @Test
    public void testPutGet() throws NoboDataException {
        Override o = Override.fromH04("H04 4 0 0 -1 -1 0 -1");
        OverrideRegister sut = new OverrideRegister();
        sut.put(o);
        assertEquals(o, sut.get(o.getId()));
    }

    @Test
    public void testPutOverwrite() throws NoboDataException {
        Override o1 = Override.fromH04("H04 4 0 0 -1 -1 0 -1");
        Override o2 = Override.fromH04("H04 4 3 0 -1 -1 0 -1");
        OverrideRegister sut = new OverrideRegister();
        sut.put(o1);
        sut.put(o2);
        assertEquals(o2, sut.get(o2.getId()));
    }

    @Test
    public void testRemove() throws NoboDataException {
        Override o = Override.fromH04("H04 4 0 0 -1 -1 0 -1");
        OverrideRegister sut = new OverrideRegister();
        sut.put(o);
        Override res = sut.remove(o.getId());
        assertEquals(o, res);
    }

    @Test
    public void testRemoveUnknown() throws NoboDataException {
        OverrideRegister sut = new OverrideRegister();
        Override res = sut.remove(666);
        assertEquals(null, res);
    }

    @Test
    public void testGetUnknown() throws NoboDataException {
        OverrideRegister sut = new OverrideRegister();
        Override o = sut.get(666);
        assertEquals(null, o);
    }
}
