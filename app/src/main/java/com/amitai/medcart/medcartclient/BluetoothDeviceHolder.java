package com.amitai.medcart.medcartclient;

/**
 * Use a BluetoothDeviceHolder to hold the properties and components of a bluetooth lock.
 */
public class BluetoothDeviceHolder {
    /**
     * Bluetooth address - UID
     */
    private String bluetoothAddreess;
    /**
     * Relay number. One Bluetooth device can have more than on relay, but specify a lock for
     * each relay.
     */
    private int relayNum;
    /**
     * Password of the bluetooth device. When setting new password make sure to change the
     * password for all locks using the same Bluetooth device.
     */
    private String password;
    /**
     * NFC Address (of the NFC sticker corresponding to this lock) - UID
     */
    private String nfcAddress;
    /**
     * Description of this lock.
     */
    private String description;

    /**
     * Empty constructor.
     */
    public BluetoothDeviceHolder() {

    }

    /**
     * Constructor for setting the Bluetooth address, the relay number and the Bluetooth password.
     *
     * @param bluetoothAddreess
     * @param relayNum
     * @param password          password for the Bluetooth device.
     */
    public BluetoothDeviceHolder(String bluetoothAddreess, int relayNum, String password) {
        this.bluetoothAddreess = bluetoothAddreess;
        this.relayNum = relayNum;
        this.password = password;
    }

    /**
     * constructor with all components, including - Bluetooth address, relay number, bluetooth
     * device password, NFC address (UID), description of the lock.
     * @param bluetoothAddreess
     * @param relayNum
     * @param password password for the Bluetooth device.
     * @param nfcAddress
     * @param description Description of the lock.
     */
    public BluetoothDeviceHolder(String bluetoothAddreess, int relayNum, String password,
                                 String nfcAddress, String description) {
        this.bluetoothAddreess = bluetoothAddreess;
        this.relayNum = relayNum;
        this.password = password;
        this.nfcAddress = nfcAddress;
        this.description = description;
    }

    /**
     * description getter.
     * @return Description of the lock
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * description setter
     * @param description Description of the lock
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Bluetooth address getter
     * @return Bluetooth address.
     */
    public String getBluetoothAddreess() {
        return this.bluetoothAddreess;
    }

    /**
     * Bluetooth address setter.
     * @param bluetoothAddreess
     */
    public void setBluetoothAddreess(String bluetoothAddreess) {
        this.bluetoothAddreess = bluetoothAddreess;
    }

    /**
     * Bluetooth device password getter.
     * @return Bluetooth device password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Bluetooth device password setter.
     * @param password Bluetooth device password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Relay number getter.
     * @return Relay number
     */
    public int getRelayNum() {
        return this.relayNum;
    }

    /**
     * Relay number setter
     * @param relayNum Relay number
     */
    public void setRelayNum(int relayNum) {
        this.relayNum = relayNum;
    }

    /**
     * Relay number getter.
     * @return NFC address
     */
    public String getNfcAddress() {
        return this.nfcAddress;
    }

    /**
     * Relay number setter.
     * @param nfcAddress Relay number
     */
    public void setNfcAddress(String nfcAddress) {
        this.nfcAddress = nfcAddress;
    }
}
