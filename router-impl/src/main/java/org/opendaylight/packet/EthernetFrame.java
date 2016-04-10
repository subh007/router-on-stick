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

public class EthernetFrame extends Packet{
    private static final String ETH_TYPE = "eth_type";
    private static final String ETH_DMAC = "destination_mac";
    private static final String ETH_SMAC = "source_mac";

    private static LinkedHashMap<String, Pair<Integer, Integer>> ETHERNET_COORDINATE = new LinkedHashMap<String, Pair<Integer, Integer>>(){

        //private static final long serialVersionUID = 1L;

        {
            put(ETH_DMAC, ImmutablePair.of(0, 48));
            put(ETH_SMAC, ImmutablePair.of(48, 48));
            put(ETH_TYPE, ImmutablePair.of(96, 16));
        }
    };

    public EthernetFrame() {
        payload = null;
        hdrFieldsMap = new HashMap<String, byte[]>(4);
        hdrFieldCoordMap = ETHERNET_COORDINATE;
    }

    public EthernetFrame setEthernetDMAC(byte[] value) {
        hdrFieldsMap.put(ETH_DMAC, value);
        return this;
    }

    public EthernetFrame setEthernetSMAC(byte[] value) {
        hdrFieldsMap.put(ETH_SMAC, value);
        return this;
    }

    public EthernetFrame setEtherType(short value) {
        hdrFieldsMap.put(ETH_TYPE, BitBufferHelper.toByteArray(value));
        return this;
    }
}
