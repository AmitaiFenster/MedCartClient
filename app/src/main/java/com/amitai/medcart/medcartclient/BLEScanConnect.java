package com.amitai.medcart.medcartclient;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Class for scanning and connecting to a Bluetooth LE device.
 */
public class BLEScanConnect implements BluetoothAdapter.LeScanCallback {

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private static final String DEVICE_NAME = "ZL-RC02D";
    private static final String TAG = "BLEScanConnect";
    BluetoothDevice device;
    Context context;
    private String defaultBT = "";
    private SharedPreferences share;
    private TimerTask task2;
    private Timer timer2;
    private TimerTask task1;
    private Timer timer1;
    private TimerTask task3;
    private Timer timer3;
    private int button3flag = 0;
    //    private SparseArray<BluetoothDevice> mDevices;
    private boolean mConnected = false; //Is GATT established.
    private boolean isBTConnected = false;  //Is Bluetooth connected.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.e(TAG, "Only gatt, just wait");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
                    .equals(action)) {
                mConnected = false;

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                mConnected = true;
                isBTConnected = true;

                Log.e(TAG, "In what we need");

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.e(TAG, "RECV DATA");
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                if (data != null) {

                }
            }
        }
    };
    /**
     * Address of device that what specified by the constructor to connect.
     */
    private String deviceAddress;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeService mBluetoothLeService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {

            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            } else {
                Log.e(TAG, "mBluetoothLeService is okay");
//  			mBluetoothLeService.connect(mDeviceAddress);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    private boolean mScanning;
    private Handler mHandler;


    public BLEScanConnect(String address, Context context) {
        this.deviceAddress = address;
        this.context = context;
        mHandler = new Handler();
        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }

    /**
     * Starts and stops Bluetooth LE scanning for BLE devices.
     *
     * @param enable true to start scanning, false to stop scanning.
     */
    public void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mScanning) {
                        BleScan(false);
                    }
                }
            }, SCAN_PERIOD);
            BleScan(true);
        } else {
            BleScan(false);
        }

    }

    /**
     * @param scan true to scan, and false to stop scanning for Bluetooth LE devices.
     */
    private void BleScan(boolean scan) {
        if (scan) {
            mScanning = true;
            boolean temp = mBluetoothAdapter.startLeScan(this);
            Log.i(TAG, "Started LE scan: " + temp);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(this);
        }
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.i(TAG, "New LE Device: " + device.getName() + " @ " + rssi);
        /**
         * We are looking for relay devices only, so validate he name
         * that each device reports before saving the ble device object.
         */
        if (DEVICE_NAME.equals(device.getName()) && this.deviceAddress.equals(device.getAddress()
        )) {
            this.device = device;
            BleScan(false);
            connectBluetoothDevice();
        }
    }

    public void connectBluetoothDevice() {

        if (device == null) return;

        Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);
        Log.d(TAG, "Try to bindService=" + context.bindService(gattServiceIntent,
                mServiceConnection,
                Context.BIND_AUTO_CREATE));
        context.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        final String mDeviceAddress = device.getAddress();
        Log.d(TAG, "service" + mBluetoothLeService);

        mBluetoothLeService.connect(mDeviceAddress);

        if (mScanning) {
            mBluetoothAdapter.stopLeScan(this);
            mScanning = false;
        }

        if (mDeviceAddress == null || mDeviceAddress == "") {
            Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show();
            return;
        }

        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {

                    case 1:
                        String str = device.getName();
                        if (str == null) {
                            str = "Unknown Device";
                        }
                        switchRelay();
//                        startActivity(intent);
                        break;

                    case 2:
                        String str2 = device.getName();
                        if (str2 == null) {
                            str2 = "Unknown Device";
                        }

                        mBluetoothLeService.connect(mDeviceAddress);
//					devicename.setText("连接失败,请等待重新连接");
                        break;

                    case 3:
                        if (isBTConnected) {
                            switchRelay();
//					            devicename.setText("Reconnection");
//                              startActivity(intent);
                        } else {
//						devicename.setText("Reconnection fails");
                        }
                        break;

                }
                super.handleMessage(msg);
            }
        };

        task1 = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 2;
                handler.sendMessage(message);
            }
        };

        task2 = new TimerTask() {
            @Override
            public void run() {
                if (isBTConnected) {
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                } else {
                    timer1 = new Timer();
                    timer1.schedule(task1, 1000);
                    timer3 = new Timer();
                    timer3.schedule(task3, 2000);
                }
            }
        };
        timer2 = new Timer();
        timer2.schedule(task2, 800);

        task3 = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 3;
                handler.sendMessage(message);
            }
        };
    }


    public void switchRelay() {
        if (!isBTConnected) {
            Toast.makeText(context, "蓝牙没有连接，请先检查设备", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        defaultBT = share.getString("defaultBT", "12345678");
        if (button3flag == 0) {
            //1路开
            byte[] data = {(byte) 0xC5, (byte) 0x04, (byte) 0x99, (byte) 0x99, (byte)
                    0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte)
                    0x99, (byte) 0xAA};
            button3flag = 1;
            if (defaultBT.length() < 8) {
                Toast.makeText(context, "Please set the correct password in the settings 8",
                        Toast.LENGTH_SHORT).show();
            } else {
                data[2] = (byte) defaultBT.charAt(0);
                data[3] = (byte) defaultBT.charAt(1);
                data[4] = (byte) defaultBT.charAt(2);
                data[5] = (byte) defaultBT.charAt(3);
                data[6] = (byte) defaultBT.charAt(4);
                data[7] = (byte) defaultBT.charAt(5);
                data[8] = (byte) defaultBT.charAt(6);
                data[9] = (byte) defaultBT.charAt(7);
                mBluetoothLeService.WriteBytes(data);
            }
            //startbutton3.setBackgroundResource(R.drawable.ii);

        } else {
            //1路关
            byte[] data = {(byte) 0xC5, (byte) 0x06, (byte) 0x99, (byte) 0x99, (byte)
                    0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte)
                    0x99, (byte) 0xAA};
            button3flag = 0;
            if (defaultBT.length() < 8) {
                Toast.makeText(context, "Please set the correct password in the settings 8",
                        Toast.LENGTH_SHORT).show();
            } else {

                data[2] = (byte) defaultBT.charAt(0);
                data[3] = (byte) defaultBT.charAt(1);
                data[4] = (byte) defaultBT.charAt(2);
                data[5] = (byte) defaultBT.charAt(3);
                data[6] = (byte) defaultBT.charAt(4);
                data[7] = (byte) defaultBT.charAt(5);
                data[8] = (byte) defaultBT.charAt(6);
                data[9] = (byte) defaultBT.charAt(7);
                mBluetoothLeService.WriteBytes(data);
            }
        }
    }


}


