package com.amitai.medcart.medcartclient;

/**
 * Created by amita on 5/9/2016.
 */
public class WritingDataFormat {

    public static byte[] getDataRelay1Open(String btPassword) {
        byte[] data = {(byte) 0xC5, (byte) 0x04, (byte) 0x99, (byte) 0x99, (byte)
                0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte)
                0x99, (byte) 0xAA};
        data = getWritingDataPassword(btPassword, data);
        return data;
    }

    public static byte[] getDataRelay1Close(String btPassword) {
        byte[] data = {(byte) 0xC5, (byte) 0x06, (byte) 0x99, (byte) 0x99, (byte)
                0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte)
                0x99, (byte) 0xAA};
        data = getWritingDataPassword(btPassword, data);
        return data;
    }

    public static byte[] getDataRelay2Open(String btPassword) {
        byte[] data = {(byte) 0xC5, (byte) 0x05, (byte) 0x99, (byte) 0x99, (byte)
                0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte)
                0x99, (byte) 0xAA};
        data = getWritingDataPassword(btPassword, data);
        return data;
    }

    public static byte[] getDataRelay2Close(String btPassword) {
        byte[] data = {(byte) 0xC5, (byte) 0x07, (byte) 0x99, (byte) 0x99, (byte)
                0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte)
                0x99, (byte) 0xAA};
        data = getWritingDataPassword(btPassword, data);
        return data;
    }

    public static byte[] getWritingDataPassword(String btPassword, byte[] data) {
        data[2] = (byte) btPassword.charAt(0);
        data[3] = (byte) btPassword.charAt(1);
        data[4] = (byte) btPassword.charAt(2);
        data[5] = (byte) btPassword.charAt(3);
        data[6] = (byte) btPassword.charAt(4);
        data[7] = (byte) btPassword.charAt(5);
        data[8] = (byte) btPassword.charAt(6);
        data[9] = (byte) btPassword.charAt(7);
        return data;
    }
}
