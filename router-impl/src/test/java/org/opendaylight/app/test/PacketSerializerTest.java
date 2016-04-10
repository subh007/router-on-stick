package org.opendaylight.app.test;

import org.junit.Test;
import org.opendaylight.packet.ArpFrame;
import org.opendaylight.packet.VlanFrame;
import org.opendaylight.router.PacketUtil;

import junit.framework.Assert;

public class PacketSerializerTest {
    @Test
    public void testVlanPacket() {
        try{
            VlanFrame vlanFrame1 = new VlanFrame();
            vlanFrame1.setPRI((short)3).setCFI((short)2).setVlanID((short)100).setEtherType((short) 100);
            String hexVlanHeader1 = PacketUtil.bytesToHexString(vlanFrame1.serialize());
            Assert.assertEquals(hexVlanHeader1, "00640064");

            VlanFrame vlanFrame2 = new VlanFrame();
            vlanFrame2.setPRI((short)3).setCFI((short)2).setEtherType((short) 100).setVlanID((short)100);
            String hexVlanHeader2 = PacketUtil.bytesToHexString(vlanFrame2.serialize());
            Assert.assertEquals(hexVlanHeader2, "00640064");

        } catch(Exception e) {
            System.out.println("received exception " + e.getMessage());
            Assert.fail("Received exception");
        }
    }

    @Test
    public void testARPPacket() {
        try{
            ArpFrame arpFrame = new ArpFrame();
            System.out.println(PacketUtil.bytesToHexString(arpFrame.serialize()));

        } catch(Exception e) {
            System.out.println("received exception " + e.getMessage());
            Assert.fail("Received exception in ARP");
        }
    }
}
