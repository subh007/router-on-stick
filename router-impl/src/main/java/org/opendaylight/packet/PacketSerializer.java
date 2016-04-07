/*
 * Copyright (c) 2015 Yoyodyne, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.packet;

import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.arp.rev140528.arp.packet.received.packet.chain.packet.ArpPacket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ethernet.rev140528.Header8021q;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ethernet.rev140528.ethernet.packet.received.packet.chain.packet.EthernetPacket;

public class PacketSerializer {

    /**
     * This method serializes the ethernet header.
     * @param etherHeader
     * @return
     */
    public static byte[] serializeEthernetHeader(EthernetPacket etherHeader) {
        return null;
    }

    /**
     * This method serializes the ARP header.
     * @param arpHeader
     * @return
     */
    public static byte[] serializeArpHeader(ArpPacket arpHeader) {
        return null;
    }

    /**
     * This method serializes the vlan header.
     * @param vlanHeader
     * @return
     */
    public static byte[] serializeVlanHeader(Header8021q vlanHeader) {
        return null;
    }
    /**
     * This method combines the all the headers and returns as single
     * byte stream.
     * @param headers
     * @return
     */
    public static byte[] joinHeader(byte[]... headers) {
        return null;
    }
}
