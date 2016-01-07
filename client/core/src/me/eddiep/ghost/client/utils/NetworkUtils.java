package me.eddiep.ghost.client.utils;

import java.nio.ByteBuffer;

public class NetworkUtils {
    public static byte[] float2ByteArray (float value) {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    public static int byteArray2Int(byte[] array) {
        return ByteBuffer.allocate(array.length).getInt();
    }

    public static byte[] int2ByteArray(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public static float byteArray2Float(byte[] array) {
        return ByteBuffer.allocate(array.length).getFloat();
    }

    public static byte[]  double2ByteArray(double dob) {
        return ByteBuffer.allocate(8).putDouble(dob).array();
    }
}