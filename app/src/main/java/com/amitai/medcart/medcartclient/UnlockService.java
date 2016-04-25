package com.amitai.medcart.medcartclient;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
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
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class UnlockService extends IntentService /*implements BluetoothAdapter.LeScanCallback*/{
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    /**
     * Use this action to unlock using NFC Tag UID.
     */
    private static final String ACTION_UNLOCK_USING_NFC = "com.amitai.medcart.medcartclient" +
            ".action.UNLOCK_USING_NFC";
//    private static final String ACTION_BAZ = "com.amitai.medcart.medcartclient.action.BAZ";

    // TODO: Rename parameters and remove or add extra parameters.
    private static final String EXTRA_NFC_UID = "com.amitai.medcart.medcartclient.extra.NFC_UID";
    private static final String EXTRA_PARAM2 = "com.amitai.medcart.medcartclient.extra.PARAM2";

    BLEScanConnect BLEConnection;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;

    public UnlockService() {
        super("UnlockService");
    }

    /**
     * Starts this service to perform action UnlockUsingNFC with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @param NFC_UID NFC tag UID.
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionUnlockUsingNFC(Context context, String NFC_UID, String param2) {
        Intent intent = new Intent(context, UnlockService.class);
        intent.setAction(ACTION_UNLOCK_USING_NFC);
        intent.putExtra(EXTRA_NFC_UID, NFC_UID);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }


//    /**
//     * Starts this service to perform action Baz with the given parameters. If
//     * the service is already performing a task this action will be queued.
//     *
//     * @see IntentService
//     */
//    // TODO: Customize helper method
//    public static void startActionBaz(Context context, String param1, String param2) {
//        Intent intent = new Intent(context, UnlockService.class);
//        intent.setAction(ACTION_BAZ);
//        intent.putExtra(EXTRA_PARAM1, param1);
//        intent.putExtra(EXTRA_PARAM2, param2);
//        context.startService(intent);
//    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            // TODO: 4/25/2016 Check if this IF statement is needed.
            if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            }

            final String action = intent.getAction();
            if (ACTION_UNLOCK_USING_NFC.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_NFC_UID);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionUnlockUsingNFC(param1, param2);
            }
//            } else if (ACTION_BAZ.equals(action)) {
//                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
//                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
//                handleActionBaz(param1, param2);
//            }
        }
    }

    /**
     * Handle action UnlockUsingNFC in the provided background thread with the provided
     * parameters.
     *
     * @param NFC_UID UID of the NFC tag.
     */
    private void handleActionUnlockUsingNFC(String NFC_UID, String param2) {
        if (permission(NFC_UID)) {
            connectBLE(getBluetoothAddreess(NFC_UID));
        } else {
            Toast.makeText(UnlockService.this, "No premission ", Toast.LENGTH_LONG).show();
        }
    }

    private void connectBLE(String bluetoothAddreess) {
//        BLEConnection = new BLEScanConnect(bluetoothAddreess, this);
//        BLEConnection.scanLeDevice(true);
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        deviceAddress = bluetoothAddreess;
        scanLeDevice(true);
    }

    /**
     * @param NFC_UID NFC Tag UID
     * @return true if permission is granted and false otherwise;
     */
    // TODO: 4/22/2016 edit function to recive permission from the sever database.
    private boolean permission(String NFC_UID) {
        return true;
    }

    /**
     * @param NFC_UID
     * @return the address of the Bluetooth relay corresponding to the NFC Tag UID.
     */
    // TODO: 4/22/2016 Edit function to recive the BLE address from the server database.
    private String getBluetoothAddreess(String NFC_UID) {
        return "BB:A0:51:00:00:8F";
    }

//    /**
//     * Handle action Baz in the provided background thread with the provided
//     * parameters.
//     */
//    private void handleActionBaz(String param1, String param2) {
//        // TODO: Handle action Baz
//        throw new UnsupportedOperationException("Not yet implemented");
//    }











    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private static final String DEVICE_NAME = "ZL-RC02D";
    private static final String TAG = "BLEScanConnect";
    BluetoothDevice mDevice;
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
    private Handler mHandler = new Handler();

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    Log.i(TAG, "New LE Device: " + device.getName() + " @ " + rssi);
                    /**
                    * We are looking for relay devices only, so validate he name
                    * that each device reports before saving the ble device object.
                    */
                    if (DEVICE_NAME.equals(device.getName()) && deviceAddress.equals(device.getAddress()
                    )) {
                        mDevice = device;
                        BleScan(false);
                        connectBluetoothDevice();
                    }
                }
            };

    // TODO: 4/25/2016 decide which scan callback to use. Check if location on manifest is needed.

    ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i(TAG, "New LE Device: " + result.getDevice().getName() + " @ " + result.getRssi());
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.i(TAG, "Scan Faild!!");
        }
    };

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
            boolean temp = mBluetoothAdapter.startLeScan(mLeScanCallback);
//            mBluetoothLeScanner.startScan(mScanCallback);
            Log.i(TAG, "Started LE scan");
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
//            mBluetoothLeScanner.stopScan(mScanCallback);
        }
    }

//    @Override
//    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//        Log.i(TAG, "New LE Device: " + device.getName() + " @ " + rssi);
//        /**
//         * We are looking for relay devices only, so validate he name
//         * that each device reports before saving the ble device object.
//         */
//        if (DEVICE_NAME.equals(device.getName()) && this.deviceAddress.equals(device.getAddress()
//        )) {
//            this.device = device;
//            BleScan(false);
//            connectBluetoothDevice();
//        }
//    }

    public void connectBluetoothDevice() {

        if (mDevice == null) return;

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        Log.d(TAG, "Try to bindService=" + this.bindService(gattServiceIntent,
                mServiceConnection,
                Context.BIND_AUTO_CREATE));
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        final String mDeviceAddress = mDevice.getAddress();
        Log.d(TAG, "service" + mBluetoothLeService);

        mBluetoothLeService.connect(mDeviceAddress);

        if (mScanning) {
            BleScan(false);
        }

        if (mDeviceAddress == null || mDeviceAddress == "") {
            Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
            return;
        }

        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {

                    case 1:
                        String str = mDevice.getName();
                        if (str == null) {
                            str = "Unknown Device";
                        }
                        switchRelay();
//                        startActivity(intent);
                        break;

                    case 2:
                        String str2 = mDevice.getName();
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
            Toast.makeText(this, "蓝牙没有连接，请先检查设备", Toast.LENGTH_SHORT)
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
                Toast.makeText(this, "Please set the correct password in the settings 8",
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
                Toast.makeText(this, "Please set the correct password in the settings 8",
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
