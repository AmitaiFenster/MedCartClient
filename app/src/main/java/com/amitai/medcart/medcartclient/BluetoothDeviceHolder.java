package com.amitai.medcart.medcartclient;

/**
 * Created by amita on 5/19/2016.
 */
public class BluetoothDeviceHolder {

    private String bluetoothAddreess;
    private int relayNum;
    private String password;
    private String nfcAddress;
    private String description;

    public BluetoothDeviceHolder() {

    }

    public BluetoothDeviceHolder(String bluetoothAddreess, int relayNum, String password) {
        this.bluetoothAddreess = bluetoothAddreess;
        this.relayNum = relayNum;
        this.password = password;
    }

    public BluetoothDeviceHolder(String bluetoothAddreess, int relayNum, String password,
                                 String nfcAddress, String description) {
        this.bluetoothAddreess = bluetoothAddreess;
        this.relayNum = relayNum;
        this.password = password;
        this.nfcAddress = nfcAddress;
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBluetoothAddreess() {
        return this.bluetoothAddreess;
    }

    public void setBluetoothAddreess(String bluetoothAddreess) {
        this.bluetoothAddreess = bluetoothAddreess;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRelayNum() {
        return this.relayNum;
    }

    public void setRelayNum(int relayNum) {
        this.relayNum = relayNum;
    }

    public String getNfcAddress() {
        return this.nfcAddress;
    }

    public void setNfcAddress(String nfcAddress) {
        this.nfcAddress = nfcAddress;
    }
}
