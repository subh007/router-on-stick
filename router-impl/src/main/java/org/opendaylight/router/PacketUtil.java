/*
 * Copyright (c) 2015 Yoyodyne, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.router;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketUtil {
    private static final Logger LOG = LoggerFactory.getLogger(PacketUtil.class);
    public static byte[] getBytes(byte[] data) {
        return null;
    }

    public static byte[] getBytes(byte[] data, int start, int end) {
        return Arrays.copyOfRange(data, start, end);
    }

    public static String bytesToHexString(byte[] bytes) {

        if(bytes == null) {
            return "null";
        }

        String ret = "";
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < bytes.length; i++) {
            if(i > 0) {
                ret += ":";
            }
            short u8byte = (short) (bytes[i] & 0xff);
            String tmp = Integer.toHexString(u8byte);
            if(tmp.length() == 1) {
                buf.append("0");
            }
            buf.append(tmp);
        }
        ret = buf.toString();
        return ret;
    }

    public static byte[] hexStringToByteArray(String data) {
        if(data == null) {
            return null;
        }

        byte[] output = new byte[data.length()/2];
        for(int i=0; i<data.length(); i+=2) {
            output[i/2] = (byte) ((Character.digit(data.charAt(i), 16)) |
                    (Character.digit(data.charAt(i+1), 16)));
        }
        return output;
    }

    public static int byteToInt(byte[] data) {
        if(data.length > 4) {
            LOG.debug("can't decode");
            return 0;
        }
        int val = 0;
        for(int i=data.length-1; i>=0; i--) {

            val += (data[i] & 0xFF) << ((data.length - i -1) * 8);
        }
        return val;
    }
}
