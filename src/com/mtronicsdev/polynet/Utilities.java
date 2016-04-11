package com.mtronicsdev.polynet;

import java.nio.ByteBuffer;

/**
 * @author Maximilian Schmeller (mtronics_dev)
 */
public class Utilities {
    public static int bytesToInt(byte[] bytes) {
        if (bytes.length != 4) {
            byte[] newBytes = new byte[4];
            System.arraycopy(bytes, 0, newBytes, 0, Math.min(bytes.length, newBytes.length));
            bytes = newBytes;
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return buffer.getInt();
    }

    public static byte[] intToBytes(int integer) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(integer);
        return buffer.array();
    }
}
