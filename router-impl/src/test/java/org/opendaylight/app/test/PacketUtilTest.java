package org.opendaylight.app.test;

import org.junit.Test;
import org.opendaylight.router.PacketUtil;

import junit.framework.Assert;

public class PacketUtilTest {
    @Test
    public void testBytesToBit() {
        byte[] expected = new byte[] {0x01, 0x0f};
        byte[] output = PacketUtil.getBitsFromBytes(new byte[] {0x0f, 0x0f}, 9);

        int i=0;
        for(byte b : output) {
            Assert.assertTrue(b == expected[i]);
            i++;
        }
    }

}
