/*
 * Copyright (c) 2015 Yoyodyne, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.router;

import java.util.Arrays;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.arp.rev140528.arp.packet.received.packet.chain.packet.ArpPacket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ethernet.rev140528.KnownEtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ethernet.rev140528.ethernet.packet.received.packet.chain.packet.EthernetPacket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ethernet.rev140528.ethernet.packet.received.packet.chain.packet.EthernetPacketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouterProvider implements BindingAwareProvider, AutoCloseable, PacketProcessingListener {

    private static final Logger LOG = LoggerFactory.getLogger(RouterProvider.class);
    private static final int ETHER_PACKET_HEADER_SIZE=14;
    private static final int ARP_PACKET_HEADER_SIZE=28;
    private ListenerRegistration<NotificationListener> listener;

    public RouterProvider(NotificationProviderService notificationProviderService) {
        listener = notificationProviderService.registerNotificationListener(this);
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("HelloProvider Session Initiated");
    }

    @Override
    public void close() throws Exception {
        this.listener.close();
    }

    @Override
    public void onPacketReceived(PacketReceived packet) {
        LOG.debug("reveived the packet :" + packet.toString());
        byte[] rawPacket = packet.getPayload();
        try {
            EthernetPacket etherHeader = etherPacketDecoder(rawPacket);
            if(etherHeader.getEthertype() == KnownEtherType.Arp) {
                LOG.debug("received the ARP packet.");
            }
        } catch(PacketSizeException ex) {
            LOG.debug("packet can't be decode.");
        }
    }

    /**
     * this method return the ethernet from provided data.
     * @param data
     * @return
     */
    private EthernetPacket etherPacketDecoder(byte[] data) throws PacketSizeException {
        EthernetPacketBuilder ethernetPacketBuilder = new EthernetPacketBuilder();
        if(data.length < ETHER_PACKET_HEADER_SIZE) {
            throw new PacketSizeException("packet is not sufficiently big to extract the header.");
        }

        byte[] ethernetHeaderData = Arrays.copyOfRange(data, 0, ETHER_PACKET_HEADER_SIZE);
        ethernetPacketBuilder.setEthertype(KnownEtherType.forValue(PacketUtil.byteToInt(Arrays.copyOfRange(ethernetHeaderData, 12,14))));
        return ethernetPacketBuilder.build();
    }

    /**
     * This method return the ARP header info from the provided data.
     */
    private ArpPacket arpDecoder(byte[] data) {
        return null;
    }

}


class PacketSizeException extends Exception {
    public PacketSizeException(String msg) {
        super(msg);
    }
}