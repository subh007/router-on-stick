/*
 * Copyright (c) 2015 Yoyodyne, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.router;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.arp.rev140528.KnownHardwareType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.arp.rev140528.KnownOperation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.arp.rev140528.arp.packet.received.packet.chain.packet.ArpPacket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.arp.rev140528.arp.packet.received.packet.chain.packet.ArpPacketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ethernet.rev140528.KnownEtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ethernet.rev140528.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ethernet.rev140528.ethernet.packet.fields.Header8021q;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ethernet.rev140528.ethernet.packet.fields.Header8021qBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ethernet.rev140528.ethernet.packet.received.packet.chain.packet.EthernetPacket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ethernet.rev140528.ethernet.packet.received.packet.chain.packet.EthernetPacketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.router.rev150105.AddressMappingElem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.router.rev150105.AddressMappingElemBuilder;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RouterProvider implements BindingAwareProvider, AutoCloseable, PacketProcessingListener {

    private static final Logger LOG = LoggerFactory.getLogger(RouterProvider.class);
    private static final int ETHER_PACKET_HEADER_SIZE=14;
    private static final int VLAN_PACKET_HEADER_SIZE=4;
    private static final int ARP_PACKET_HEADER_SIZE=28;

    private ListenerRegistration<NotificationListener> listener;
    private ListenerRegistration<DataChangeListener> dataChangeListener;

    private DataBroker dataBroker;
    private ConcurrentHashMap<String, AddressMappingElem> addressTable;

    private static String ROUTER_MAC_ADDRESS="10:20:30:40:50:60";
    private static InstanceIdentifier<Node> NODE_IID = InstanceIdentifier.builder(Nodes.class).child(Node.class).build();

    public RouterProvider(NotificationProviderService notificationProviderService, DataBroker broker) {
        listener = notificationProviderService.registerNotificationListener(this);
        dataBroker = broker;

        dataChangeListener = dataBroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, NODE_IID, new OFSwitchTracker(), DataChangeScope.BASE);
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("HelloProvider Session Initiated");
        addressTable = new ConcurrentHashMap<>();
    }

    @Override
    public void close() throws Exception {
        this.listener.close();
        dataChangeListener.close();

        listener = null;
        dataChangeListener = null;
    }

    @Override
    public void onPacketReceived(PacketReceived packet) {
        LOG.debug("reveived the packet :");
        byte[] rawPacket = packet.getPayload();
        try {
            EthernetPacket etherHeader = etherPacketDecoder(rawPacket);
            if(etherHeader.getEthertype() == KnownEtherType.Arp) {
                LOG.debug("received the ARP packet.");

                // create and populate the arpheader.
                ArpPacket arpHeader = arpDecoder(Arrays.copyOfRange(rawPacket,
                        ETHER_PACKET_HEADER_SIZE,
                        ETHER_PACKET_HEADER_SIZE+ARP_PACKET_HEADER_SIZE));

                if(arpHeader != null) {
                    LOG.info("in_port : {}", packet.getIngress().getValue());
                    LOG.info("smac {} dmac {} sip {} dip {}", arpHeader.getSourceHardwareAddress(),
                            arpHeader.getDestinationHardwareAddress(),
                            arpHeader.getSourceProtocolAddress(),
                            arpHeader.getDestinationProtocolAddress());

                    AddressMappingElem amElem = new AddressMappingElemBuilder()
                            .setIp(arpHeader.getSourceProtocolAddress())
                            .setMac(arpHeader.getSourceHardwareAddress())
                            .setInPort(packet.getIngress()).build();
                    addressTable.put(amElem.getIp(), amElem);
                    LOG.info("address {}", addressTable);
                }
            }
            else if(etherHeader.getEthertype() == KnownEtherType.VlanTagged) {
                LOG.debug("reveived vlan tagged packet.");

                byte[] vlanHeaderData = Arrays.copyOfRange(rawPacket, ETHER_PACKET_HEADER_SIZE,
                        ETHER_PACKET_HEADER_SIZE + VLAN_PACKET_HEADER_SIZE);

                // decode the vlan header.
                Header8021q vlanHeader = vlanDecoder(vlanHeaderData);
                LOG.debug("vlan id : {}", vlanHeader.getVlan().getValue().intValue());
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
        // TODO: Handler for the vlan tagged packet.

        EthernetPacketBuilder ethernetPacketBuilder = new EthernetPacketBuilder();
        if(data.length < ETHER_PACKET_HEADER_SIZE) {
            throw new PacketSizeException("packet is not sufficiently big to extract the header.");
        }

        byte[] ethernetHeaderData = Arrays.copyOfRange(data, 0, ETHER_PACKET_HEADER_SIZE);
        ethernetPacketBuilder.setEthertype(KnownEtherType.forValue(PacketUtil.byteToInt(Arrays.copyOfRange(ethernetHeaderData, 12,14))));
        return ethernetPacketBuilder.build();
    }

    private Header8021q vlanDecoder(byte[] data) throws PacketSizeException {
        if (data.length < VLAN_PACKET_HEADER_SIZE) {
            throw new PacketSizeException("vlan header can't be decoded.");
        }

        Header8021qBuilder vlanHeaderBuilder = new Header8021qBuilder();
        int vlanID = PacketUtil.byteToInt(PacketUtil.getBitsFromBytes(Arrays.copyOfRange(data, 0, 2), 12));
        vlanHeaderBuilder.setVlan(new VlanId(new Integer(vlanID)));
        LOG.debug("Received packet from the vlan {}", vlanID);

        return vlanHeaderBuilder.build();
    }

    /**
     * This method return the ARP header info from the provided data.
     */
    private ArpPacket arpDecoder(byte[] data) throws PacketSizeException{
        ArpPacketBuilder arpBuilder = new ArpPacketBuilder();

        if(data.length < ARP_PACKET_HEADER_SIZE) {
            throw new PacketSizeException("packet is not sufficiently bit to extract the header.");
        }

        arpBuilder.setHardwareType(KnownHardwareType.forValue(PacketUtil.byteToInt(Arrays.copyOfRange(data, 0, 2))));
        arpBuilder.setProtocolType(KnownEtherType.forValue(PacketUtil.byteToInt(Arrays.copyOfRange(data, 2, 4))));

        arpBuilder.setOperation(KnownOperation.forValue(PacketUtil.byteToInt(Arrays.copyOfRange(data, 6, 8)))); //2 byte

        if(arpBuilder.getHardwareType().equals(KnownHardwareType.Ethernet)) {
            // retrieve the hex string for src and dst mac.
            arpBuilder.setSourceHardwareAddress(PacketUtil.bytesToHexString(Arrays.copyOfRange(data, 8, 14))); //6 byte
            arpBuilder.setDestinationHardwareAddress(PacketUtil.bytesToHexString(Arrays.copyOfRange(data, 18, 24))); // 6 byte
        }

        if(arpBuilder.getProtocolType().equals(KnownEtherType.Ipv4)) {
            // decode IPv4 protocol address
            LOG.debug("ready to retrieeve IPv4 address");
            try {
                arpBuilder.setSourceProtocolAddress(InetAddress.getByAddress(Arrays.copyOfRange(data, 14, 18)).getHostAddress());
                arpBuilder.setDestinationProtocolAddress(InetAddress.getByAddress(Arrays.copyOfRange(data, 24, 28)).getHostAddress());
            } catch(UnknownHostException ex) {
                LOG.error("error : {}", ex.getMessage());
            }

        } else {
            // decode IPv6 protocol address
        }
        return arpBuilder.build();
    }

}


class PacketSizeException extends Exception {
    public PacketSizeException(String msg) {
        super(msg);
    }
}