/*
 * Copyright (c) 2015 Yoyodyne, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.packet;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.opendaylight.controller.liblldp.BitBufferHelper;
import org.opendaylight.controller.liblldp.Packet;

public class VlanFrame extends Packet{
    private static final String ETH_TYPE = "eth_type";
    private static final String PRI = "priority";
    private static final String CFI = "cfi";
    private static final String ID = "vlanid";

    private static LinkedHashMap<String, Pair<Integer, Integer>> VLAN_COORDINATE = new LinkedHashMap<String, Pair<Integer, Integer>>(){

        //private static final long serialVersionUID = 1L;

        {
            put(PRI, ImmutablePair.of(0, 3));
            put(CFI, ImmutablePair.of(3, 1));
            put(ID, ImmutablePair.of(4, 12));
            put(ETH_TYPE, ImmutablePair.of(16, 16));
        }
    };

    public VlanFrame() {
        payload = null;
        hdrFieldsMap = new HashMap<String, byte[]>(4);
        hdrFieldCoordMap = VLAN_COORDINATE;
    }

    public VlanFrame setPRI(short value) {
        byte[] pri = BitBufferHelper.toByteArray(value);
        hdrFieldsMap.put(PRI, pri);
        return this;
    }

    public VlanFrame setCFI(short value) {
        byte[] cfi = BitBufferHelper.toByteArray(value);
        hdrFieldsMap.put(CFI, cfi);
        return this;
    }

    public VlanFrame setVlanID(short value) {
        byte[] vlanArray = BitBufferHelper.toByteArray(value);
        hdrFieldsMap.put(ID, vlanArray);
        return this;
    }

    public VlanFrame setEtherType(short value) {
        byte[] ethType = BitBufferHelper.toByteArray(value);
        hdrFieldsMap.put(ETH_TYPE, ethType);
        return this;
    }
}
