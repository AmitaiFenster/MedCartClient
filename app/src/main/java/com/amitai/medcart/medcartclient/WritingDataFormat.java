package com.amitai.medcart.medcartclient;

/**
 * Use the static methods in this class to get the data for sending to the Bluetooth LE Relay
 * device, corresponding to the action that is wished to be triggered on the Bluetooth LE Relay
 * device.
 */
public class WritingDataFormat {

    /**
     * @param btPassword <code>String</code> with the password of the Bluetooth LE Relay device.
     * @return <code>byte array</code> with the data for the action: open relay number 1.
     */
    public static byte[] getDataRelay1Open(String btPassword) {
        byte[] data = {(byte) 0xC5, (byte) 0x04, (byte) 0x99, (byte) 0x99, (byte)
                0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte)
                0x99, (byte) 0xAA};
        data = getWritingDataPassword(btPassword, data);
        return data;
    }

    /**
     *
     * @param btPassword <code>String</code> with the password of the Bluetooth LE Relay device.
     * @return <code>byte array</code> with the data for the action: close relay number 1.
     */
    public static byte[] getDataRelay1Close(String btPassword) {
        byte[] data = {(byte) 0xC5, (byte) 0x06, (byte) 0x99, (byte) 0x99, (byte)
                0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte)
                0x99, (byte) 0xAA};
        data = getWritingDataPassword(btPassword, data);
        return data;
    }

    /**
     *
     * @param btPassword <code>String</code> with the password of the Bluetooth LE Relay device.
     * @return <code>byte array</code> with the data for the action: open relay number 2.
     */
    public static byte[] getDataRelay2Open(String btPassword) {
        byte[] data = {(byte) 0xC5, (byte) 0x05, (byte) 0x99, (byte) 0x99, (byte)
                0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte)
                0x99, (byte) 0xAA};
        data = getWritingDataPassword(btPassword, data);
        return data;
    }

    /**
     *
     * @param btPassword <code>String</code> with the password of the Bluetooth LE Relay device.
     * @return <code>byte array</code> with the data for the action: close relay number 2.
     */
    public static byte[] getDataRelay2Close(String btPassword) {
        byte[] data = {(byte) 0xC5, (byte) 0x07, (byte) 0x99, (byte) 0x99, (byte)
                0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte)
                0x99, (byte) 0xAA};
        data = getWritingDataPassword(btPassword, data);
        return data;
    }

    /**
     *
     * @param btPassword <code>String</code> with the password of the Bluetooth LE Relay device.
     * @param data <code>byte array</code> with the data.
     * @return <code>byte array</code> with the data and the password.
     */
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

    /**
     * @param oldPassword <code>String</code> with the old password (current password).
     * @param newPassword <code>String</code> with the new password.
     * @return <code>byte array</code> with the data for the action: set new password.
     */
    public static byte[] getDataSetPassword(String oldPassword, String newPassword) {
        byte[] data = {(byte) 0xC5, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte)
                0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte)
                0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte)
                0x36, (byte) 0x37, (byte) 0x38, (byte) 0xAA};
        data[1] = (byte) oldPassword.charAt(0);
        data[2] = (byte) oldPassword.charAt(1);
        data[3] = (byte) oldPassword.charAt(2);
        data[4] = (byte) oldPassword.charAt(3);
        data[5] = (byte) oldPassword.charAt(4);
        data[6] = (byte) oldPassword.charAt(5);
        data[7] = (byte) oldPassword.charAt(6);
        data[8] = (byte) oldPassword.charAt(7);

        data[9] = (byte) newPassword.charAt(0);
        data[10] = (byte) newPassword.charAt(1);
        data[11] = (byte) newPassword.charAt(2);
        data[12] = (byte) newPassword.charAt(3);
        data[13] = (byte) newPassword.charAt(4);
        data[14] = (byte) newPassword.charAt(5);
        data[15] = (byte) newPassword.charAt(6);
        data[16] = (byte) newPassword.charAt(7);

        return data;
    }
}
