/*
 * Copyright (c) 2015 Yoyodyne, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.proxyarp;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import org.opendaylight.controller.liblldp.Packet;
import org.opendaylight.packet.ArpFrame;
import org.opendaylight.packet.EthernetFrame;
import org.opendaylight.packet.VlanFrame;
import org.opendaylight.router.PacketUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.router.rev150105.AddressMappingElem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.router.rev150105.AddressMappingElemBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyArp implements PacketProcessingListener{

    private static final Logger LOG = LoggerFactory.getLogger(ProxyArp.class);
    private static final int ETHER_PACKET_HEADER_SIZE=14;
    private static final int VLAN_PACKET_HEADER_SIZE=4;
    private static final int ARP_PACKET_HEADER_SIZE=28;

    private ConcurrentHashMap<String, AddressMappingElem> addressTable;
    private PacketProcessingService packetProcessingService;

    public ProxyArp() {
        addressTable = new ConcurrentHashMap<>();
    }

    public void setPacketProcessingService(PacketProcessingService packetProcessingSercice) {
        this.packetProcessingService = packetProcessingSercice;
    }

    @Override
    public void onPacketReceived(PacketReceived packet) {
        LOG.debug("reveived the packet :");
        byte[] rawPacket = packet.getPayload();
        try {
            EthernetPacket etherHeader = etherPacketDecoder(rawPacket);

            if(etherHeader.getEthertype() == KnownEtherType.Arp) {
                LOG.debug("received the ARP packet but we don't handle the untagged packet.");

            }else if(etherHeader.getEthertype() == KnownEtherType.VlanTagged) {
                LOG.debug("reveived vlan tagged packet.");

                byte[] vlanHeaderData = Arrays.copyOfRange(rawPacket, ETHER_PACKET_HEADER_SIZE,
                        ETHER_PACKET_HEADER_SIZE + VLAN_PACKET_HEADER_SIZE);
                byte[] arpheaderData = Arrays.copyOfRange(rawPacket,
                        ETHER_PACKET_HEADER_SIZE,
                        ETHER_PACKET_HEADER_SIZE+ARP_PACKET_HEADER_SIZE);

                // decode the vlan header and arp header.
                Header8021q vlanHeader = vlanDecoder(vlanHeaderData);

                if(KnownEtherType.forValue(PacketUtil.byteToInt(Arrays.copyOfRange(vlanHeaderData, 2, 4)))
                        == KnownEtherType.Arp) {

                    LOG.debug("received packet is arp packet");
                    ArpPacket arpHeader = arpDecoder(arpheaderData);
                    if(arpHeader != null) {
                        processArpRequestPacket(arpHeader, packet.getIngress());
                        createAndSendArpResponse(etherHeader, vlanHeader, arpHeader);
                    }
                }

                // Test code to send the packet on output port
                NodeConnectorRef outputport  = packet.getIngress();
                InstanceIdentifier<Node> nodeIID = outputport.getValue().firstIdentifierOf(Node.class);
                InstanceIdentifier<NodeConnector> outportIID = outputport.getValue().firstIdentifierOf(NodeConnector.class);

                // create ARP response packet = ethernet + vlan + arp

                // ARP header
                ArpFrame arp = new ArpFrame();
                arp.setOperation((short)2);
                arp.setSHA(new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06});
                arp.setTHA(new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06});
                arp.setSPA(new byte[] {0x01, 0x00, 0x00, 0x02});
                arp.setTPA(new byte[] {0x01, 0x00, 0x00, 0x01});

                // vlan header
                VlanFrame vlan = new VlanFrame();
                vlan.setPRI((short)0);
                vlan.setCFI((short) 0);
                vlan.setVlanID((short) 100);
                vlan.setEtherType((short) 0x0806);

                // ethernet header
                EthernetFrame ether = new EthernetFrame();
                ether.setEthernetDMAC(new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06});
                ether.setEthernetSMAC(new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06});
                ether.setEtherType((short) 0x8100);

                byte[] data = concateHeaders(ether, vlan, arp);

                sendPacket(nodeIID,
                        outportIID,
                        data);
            }
        } catch(PacketSizeException ex) {
            LOG.debug("packet can't be decode.");
        }
    }

    // This method will populate the address mapping with the data
    private void processArpRequestPacket(ArpPacket arpHeader, NodeConnectorRef portref) {
        LOG.info("smac {} dmac {} sip {} dip {}", arpHeader.getSourceHardwareAddress(),
                arpHeader.getDestinationHardwareAddress(),
                arpHeader.getSourceProtocolAddress(),
                arpHeader.getDestinationProtocolAddress());

        AddressMappingElem amElem = new AddressMappingElemBuilder()
                .setIp(arpHeader.getSourceProtocolAddress())
                .setMac(arpHeader.getSourceHardwareAddress())
                .setInPort(portref).build();
        addressTable.put(amElem.getIp(), amElem);
        LOG.info("added entry to address table {}", addressTable);
    }

    private ArpFrame getArpFrame(ArpPacket arpHeader) {
        ArpFrame arpFrame = new ArpFrame();
        arpFrame.setOperation((short)arpHeader.getOperation().getIntValue());
        arpFrame.setSHA(PacketUtil.hexStringToByteArray(arpHeader.getSourceHardwareAddress()));
        arpFrame.setSPA(PacketUtil.hexStringToByteArray(arpHeader.getSourceProtocolAddress()));
        arpFrame.setTHA(PacketUtil.hexStringToByteArray(arpHeader.getDestinationHardwareAddress()));
        arpFrame.setTPA(PacketUtil.hexStringToByteArray(arpHeader.getDestinationProtocolAddress()));

        return arpFrame;
    }

    private VlanFrame getVlanFrame(Header8021q vlanHeader) {
        VlanFrame vlanFrame = new VlanFrame();
        vlanFrame.setCFI((short)0);
        vlanFrame.setPRI((short)0);
        vlanFrame.setEtherType((short) 0x0806); // Hardcoded for the ARP
        vlanFrame.setVlanID((short)vlanHeader.getVlan().getValue().intValue());

        return vlanFrame;
    }

    private EthernetFrame getEtherFrame(EthernetPacket etherHeader) {
        EthernetFrame etherFrame = new EthernetFrame();
        etherFrame.setEthernetDMAC(PacketUtil.hexStringToByteArray(etherHeader.getDestinationMac().getValue()));
        etherFrame.setEthernetSMAC(PacketUtil.hexStringToByteArray(etherHeader.getSourceMac().getValue()));
        etherFrame.setEtherType((short)0x8100); // hardcoded for vlan

        return etherFrame;
    }
    /**
     * Get the ARP response for the ARP request packet. Arp Response will be
     * build using self mac address (sub-interface mac address).
     * @param arpPacket
     * @return
     */
    private void createAndSendArpResponse(EthernetPacket receivedEthernet,
            Header8021q reveivedVlan,
            ArpPacket receivedArp) {

        // create Headers
    }

    /**
     * This method concate the header byte steam.
     * @param packets
     * @return
     */
    private byte[] concateHeaders(Packet ...packets) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            for(Packet packet : packets) {
                outputStream.write(packet.serialize());
            }
        } catch (Exception e) {
            LOG.error("exception in serializing the packet : {}", e.getMessage());
        }

        return outputStream.toByteArray();
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

    private Header8021q vlanDecoder(byte[] data) throws PacketSizeException {
        if (data.length < VLAN_PACKET_HEADER_SIZE) {
            throw new PacketSizeException("vlan header can't be decoded.");
        }

        Header8021qBuilder vlanHeaderBuilder = new Header8021qBuilder();
        int vlanID = PacketUtil.byteToInt(PacketUtil.getBitsFromBytes(Arrays.copyOfRange(data, 0, 2), 12));
        int
        vlanHeaderBuilder.setVlan(new VlanId(new Integer(vlanID)));
        LOG.debug("Received packet from the vlan {}", vlanID);

        return vlanHeaderBuilder.build();
    }

    /**
     * This method sends the packet on given output port
     * @param nodeIID SwithID
     * @param ncIID Node connector (ouput port)
     * @param data packet to transmit
     */
    private void sendPacket(InstanceIdentifier<Node> nodeIID,
            InstanceIdentifier<NodeConnector> ncIID,
            byte[] data) {

        TransmitPacketInputBuilder txBuilder = new TransmitPacketInputBuilder();
        txBuilder.setPayload(data)
        .setNode(new NodeRef(nodeIID))
        .setEgress(new NodeConnectorRef(ncIID));

        packetProcessingService.transmitPacket(txBuilder.build());
    }
}



class PacketSizeException extends Exception {
    public PacketSizeException(String msg) {
        super(msg);
    }
}
