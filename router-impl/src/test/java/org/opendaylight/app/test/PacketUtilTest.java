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

    @Test
    public void testReplaceBytes() {
        byte[] expected = new byte[] {0x01, 0x02, 0x03, 0x01, 0x02, 0x06};
        byte[] destination = new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        byte[] source = new byte[] {0x01, 0x02, 0x03};

        byte[] output = PacketUtil.replaceBytes(destination, source, 3, 2);

        int i=0;
        for(byte b: output) {
            Assert.assertEquals(b, expected[i]);
            i++;
        }
    }
}
