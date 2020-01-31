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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nobohub.internal.NoboHubBindingConstants;

/**
 * Nobø serial numbers are 12 digits where 3 and 3 digits form 2 bytes as decimal. In total 32 bits.
 * 
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public final class SerialNumber {

    private final String serialNumber;

    public SerialNumber(String serialNumber) {
        this.serialNumber = serialNumber.trim();
    }

    public boolean isWellFormed() {
        if (serialNumber.length() != 12) {
            return false;
        }

        List<String> parts = new ArrayList<String>(4); 
        for (int i = 0; i < 4; i++) {
            parts.add(serialNumber.substring((i * 3), (i * 3) + 3));
        }    

        if (parts.size() != 4) {
            return false;
        }

        for (String part : parts) {
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException nfe) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the type string.
     */
    public String getTypeIdentifier() {
        if (!isWellFormed()) {
            return "Unknown";
        }

        return serialNumber.substring(0, 3);
    }

    /**
     * Returns the type of this component. 
     */
    public String getComponentType() {
        String id = getTypeIdentifier();
        return NoboHubBindingConstants.SERIALNUMBERS_FOR_TYPES.getOrDefault(id, "Unknown, please contact maintainer to add a new type for " + serialNumber);
    }

    @java.lang.Override
    public String toString() {
        return serialNumber;
    }

    @java.lang.Override
    public boolean equals(@Nullable Object obj) 
    { 
        if (this == obj) {
            return true; 
        }

        if (obj == null || obj.getClass() != this.getClass()) {
            return false; 
        } 
          
        SerialNumber other = (SerialNumber) obj;           
        return this.serialNumber.equals(other.serialNumber); 
    } 
      
    @java.lang.Override
    public int hashCode() 
    { 
        return this.serialNumber.hashCode(); 
    } 
}
