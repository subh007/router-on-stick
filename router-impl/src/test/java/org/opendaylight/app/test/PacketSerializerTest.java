package org.opendaylight.app.test;

import org.junit.Test;
import org.opendaylight.packet.VlanFrame;
import org.opendaylight.router.PacketUtil;

import junit.framework.Assert;

public class PacketSerializerTest {
    @Test
    public void testVlanPacket() {
        try{
            VlanFrame vlanFrame = new VlanFrame();
            vlanFrame.setPRI((short)3).setCFI((short)2).setVlanID((short)100).setEtherType((short) 100);
            String hexVlanHeader = PacketUtil.bytesToHexString(vlanFrame.serialize());
            Assert.assertEquals(hexVlanHeader, "00640064");

        } catch(Exception e) {
            System.out.println("received exception " + e.getMessage());
            Assert.fail("Received exception");
        }
    }
}
