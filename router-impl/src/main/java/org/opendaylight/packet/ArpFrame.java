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
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.opendaylight.controller.liblldp.BitBufferHelper;
import org.opendaylight.controller.liblldp.Packet;

public class ArpFrame extends Packet{
    private static final String HTYPE = "htype";
    private static final String PTYPE = "ptype";
    private static final String HLEN = "hlen";
    private static final String PLEN = "plen";
    private static final String OPERATION = "operation";
    private static final String SHA = "sha";
    private static final String SPA = "spa";
    private static final String THA = "tha";
    private static final String TPA = "tpa";

    private static final int ETHERNET_HW_TYPE = 1;

    private final Map<String, Pair<Integer, Integer>> ARP_FIELD_COORDINATES = new LinkedHashMap<String, Pair<Integer, Integer>>() {

        private static final long serialVersionUID = 1L;

        {
            put(HTYPE, ImmutablePair.of(0, 16));
            put(PTYPE, ImmutablePair.of(16, 16));
            put(HLEN, ImmutablePair.of(32, 8));
            put(PLEN, ImmutablePair.of(40, 8));
            put(OPERATION, ImmutablePair.of(48, 16));
            put(SHA, ImmutablePair.of(64, 48));
            put(SPA, ImmutablePair.of(112, 32));
            put(THA, ImmutablePair.of(144, 48));
            put(TPA, ImmutablePair.of(192, 32));
        }
    };

    public ArpFrame() {
        payload = null;
        hdrFieldsMap = new HashMap<String, byte[]>(9);
        setHLen((byte) 6); // MAC address length
        setPLen((byte) 4); // IPv4 address length
        setHType((short)ETHERNET_HW_TYPE);
        setPType((short)0x0800);
        hdrFieldCoordMap = ARP_FIELD_COORDINATES;
    }

    public ArpFrame setHType(short value) { // 16
        hdrFieldsMap.put(HTYPE, BitBufferHelper.toByteArray(value));
        return this;
    }

    public ArpFrame setPType(short value) { //16
        hdrFieldsMap.put(PTYPE, BitBufferHelper.toByteArray(value));
        return this;
    }

    public ArpFrame setHLen(byte value) { //8
        hdrFieldsMap.put(HLEN, BitBufferHelper.toByteArray(value));
        return this;
    }

    public ArpFrame setPLen(byte value) { //8
        hdrFieldsMap.put(PLEN, BitBufferHelper.toByteArray(value));
        return this;
    }

    public ArpFrame setOperation(short value) { //16
        hdrFieldsMap.put(OPERATION, BitBufferHelper.toByteArray(value));
        return this;
    }

    public ArpFrame setSHA(byte[] value) { //48
        hdrFieldsMap.put(SHA, value);
        return this;
    }

    public ArpFrame setSPA(byte[] value) { //32
        hdrFieldsMap.put(SPA, value);
        return this;
    }

    public ArpFrame setTHA(byte[] value) { //48
        hdrFieldsMap.put(THA, value);
        return this;
    }

    public ArpFrame setTPA(byte[] value) { //32
        hdrFieldsMap.put(TPA, value);
        return this;
    }
}
