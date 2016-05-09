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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class UnlockService extends IntentService /*implements BluetoothAdapter.LeScanCallback*/ {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS

    /**
     * Use this action to unlock using NFC Tag UID.
     */
    private static final String ACTION_UNLOCK_USING_NFC = "com.amitai.medcart.medcartclient" +
            ".action.UNLOCK_USING_NFC";
    // TODO: Rename parameters and remove or add extra parameters.
    private static final String EXTRA_NFC_UID = "com.amitai.medcart.medcartclient.extra.NFC_UID";
    //    private static final String ACTION_BAZ = "com.amitai.medcart.medcartclient.action.BAZ";
    private static final String EXTRA_PARAM2 = "com.amitai.medcart.medcartclient.extra.PARAM2";
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private static final String DEVICE_NAME = "ZL-RC02D";
    BluetoothDevice mDevice;
    private Firebase mFirebasePermissions;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private String defaultBT = "";
    // TODO: 5/8/2016  use Firebase instead of SharedPreferences, and handle Bluetooth devices
    // passwords better.
//    private SharedPreferences share;
    private TimerTask task2;
    private Timer timer2;
    private TimerTask task1;
    private Timer timer1;
    private TimerTask task3;
    private Timer timer3;

    //Close the relay after opened
    private TimerTask task4;
    private Timer timer4;

    private int button3flag = 0;
    //    private SparseArray<BluetoothDevice> mDevices;
    private boolean mConnected = false; //Is GATT established.
    private boolean isBTConnected = false;  //Is Bluetooth connected.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.e(Constants.TAG_UnlockService, "Only gatt, just wait");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
                    .equals(action)) {
                mConnected = false;

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                mConnected = true;
                isBTConnected = true;

                Log.e(Constants.TAG_UnlockService, "In what we need");

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.e(Constants.TAG_UnlockService, "RECV DATA");
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
                Log.e(Constants.TAG_UnlockService, "Unable to initialize Bluetooth");
            } else {
                Log.e(Constants.TAG_UnlockService, "mBluetoothLeService is okay");
//  			mBluetoothLeService.connect(mDeviceAddress);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    private boolean mScanning;
    ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
//            Log.i(Constants.TAG_UnlockService, "New LE Device: " + device.getName() + ",
// Address: " + device
//                    .getAddress() +
//                    " @ " +
//                    "" + result.rssi);
            /**
             * We are looking for relay devices only, so validate he name
             * that each device reports before saving the ble device object.
             */
            if (/*DEVICE_NAME.equals(device.getName()) && */ deviceAddress.equals(device
                            .getAddress()
            )) {
                mDevice = device;
                BleScan(false);
                connectBluetoothDevice();
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.i(Constants.TAG_UnlockService, "Scan Faild!!");
        }
    };
    private Handler mHandler = new Handler();

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
    public static void startActionUnlockUsingNFC(Context context, String NFC_UID, String
            firebaseUrl) {
        Intent intent = new Intent(context, UnlockService.class);
        intent.setAction(ACTION_UNLOCK_USING_NFC);
        intent.putExtra(EXTRA_NFC_UID, NFC_UID);
        intent.putExtra(Constants.FIREBASE, firebaseUrl);
        context.startService(intent);
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

    @Override
    public void onDestroy() {
        unregisterReceiver(mGattUpdateReceiver);
//        getApplicationContext().unbindService(mServiceConnection);
//        if (mBluetoothLeService != null) {
//            mBluetoothLeService.close();
//            mBluetoothLeService = null;
//        }
        Log.d(Constants.TAG_UnlockService, "We are in destroy");

        super.onDestroy();
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
    public void onCreate() {
        super.onCreate();

//        Intent gattServiceIntent = new Intent(getApplicationContext(), BluetoothLeService.class);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        // TODO: 5/8/2016 Check why bind service returns false, and why the is a error logged
        // that UnlockService has leaked ServiceConnection.
        Log.d(Constants.TAG_UnlockService, "Try to bindService=" + getApplicationContext()
                .bindService
                        (gattServiceIntent,
                                mServiceConnection,
                                Context.BIND_AUTO_CREATE));
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

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
                final String NFC_UID = intent.getStringExtra(EXTRA_NFC_UID);
                final String firebaseUrl = intent.getStringExtra(Constants.FIREBASE);
                mFirebasePermissions = new Firebase(firebaseUrl);
                handleActionUnlockUsingNFC(NFC_UID, mFirebasePermissions);
            }
//            } else if (ACTION_BAZ.equals(action)) {
//                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
//                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
//                handleActionBaz(param1, param2);
//            }
        }
    }

    private void connectBLE(String bluetoothAddreess) {
        deviceAddress = bluetoothAddreess;
        scanLeDevice(true);
    }

//    /**
//     * Handle action Baz in the provided background thread with the provided
//     * parameters.
//     */
//    private void handleActionBaz(String param1, String param2) {
//        // TODO: Handle action Baz
//        throw new UnsupportedOperationException("Not yet implemented");
//    }

    /**
     * Handle action UnlockUsingNFC in the provided background thread with the provided
     * parameters.
     *
     * @param NFC_UID  of the NFC tag.
     * @param firebase pointing to the user.
     */
    // TODO: 4/22/2016 Edit function to recive the BLE address from the server database.
    private void handleActionUnlockUsingNFC(final String NFC_UID, Firebase firebase) {
        Firebase firebaseMain = new Firebase(Constants.FIREBASE_URL);

        firebase = firebase.child("authorization");
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Iterable<DataSnapshot> authorizationSnapshots = dataSnapshot.getChildren();
                Firebase firebaseRelays = new Firebase(Constants.FIREBASE_URL + "relays");

                firebaseRelays.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> relays = dataSnapshot.getChildren();
                        boolean isAuthorized = false;
                        DataSnapshot correspondingRelay = null;

                        for (DataSnapshot relay : relays) {
                            String a = relay.child("NFCuid").getValue(String.class);
                            if (relay.child("NFCuid").getValue(String.class).equals(NFC_UID)) {
                                String relayName = relay.getKey();

                                for (DataSnapshot authorizationSnap : authorizationSnapshots) {
                                    if (authorizationSnap.getValue(Boolean.class) &&
                                            authorizationSnap.getKey().equals(relayName)) {
                                        isAuthorized = true;
                                        correspondingRelay = relay;
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                        Toast.makeText(UnlockService.this, "Permission: " + isAuthorized, Toast
                                .LENGTH_LONG).show();
                        if (isAuthorized)
                            connectBLE(correspondingRelay.child("BLEuid").getValue(String.class));
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
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
            mBluetoothLeScanner.startScan(mScanCallback);
            Log.i(Constants.TAG_UnlockService, "Started LE scan");
        } else {
            mScanning = false;
            mBluetoothLeScanner.stopScan(mScanCallback);
            Log.i(Constants.TAG_UnlockService, "Stopped LE scan");
        }
    }

    public void connectBluetoothDevice() {

        if (mDevice == null) return;

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        Log.d(Constants.TAG_UnlockService, "Try to bindService=" + this.bindService
                (gattServiceIntent,
                        mServiceConnection,
                        Context.BIND_AUTO_CREATE));
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        final String mDeviceAddress = mDevice.getAddress();
        Log.d(Constants.TAG_UnlockService, "service " + mBluetoothLeService);

        boolean isConnectionSuccess = mBluetoothLeService.connect(mDeviceAddress);
        Log.d(Constants.TAG_UnlockService, "Connection success: " + isConnectionSuccess);

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
//					devicename.setText("Connection fails, wait reconnect");
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

        task3 = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 3;
                handler.sendMessage(message);
            }
        };

        //Close the relay after opened
        task4 = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
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
                    timer4 = new Timer();
                    timer4.schedule(task4, 2000);
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

//        unregisterReceiver(mGattUpdateReceiver);
//        getApplicationContext().unbindService(mServiceConnection);
//        if (mBluetoothLeService != null) {
//            mBluetoothLeService.close();
//            mBluetoothLeService = null;
//        }
    }


    public void switchRelay() {
        if (!isBTConnected) {
            Toast.makeText(this, "Bluetooth is not connected, first check equipment", Toast
                    .LENGTH_SHORT)
                    .show();
            return;
        }
//        defaultBT = share.getString("defaultBT", "12345678");
        defaultBT = "12345678";
        if (button3flag == 0) {
            //1 open road
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
            //1 off road
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

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
