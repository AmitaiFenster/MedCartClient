/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amitai.medcart.medcartclient;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends ListActivity {
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private String TAG = "DeviceScanActivity";
    private LeDeviceListAdapter mLeDeviceListAdapter;
    ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            mLeDeviceListAdapter.addDevice(device, result.getRssi(), result.getScanRecord()
                    .getBytes());
            mLeDeviceListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.i(Constants.TAG_UnlockService, "Scan Faild!!");
        }
    };
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private boolean mScanning;
    private Handler mHandler;
    private Button buttonScan;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_device_scan_temp);

        buttonScan = (Button) findViewById(R.id.buttonscan);

        buttonScan.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
            }
        });

        // TODO: 5/22/2016 add EnableAllComponents to make sure location permission is allowed,
        // and to make this code more simple (remove what isn't needed here that
        // EnableAllComponents covers...

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == Constants.REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
            }
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;

        if (mScanning) {
            mBluetoothLeScanner.stopScan(mScanCallback);
            mScanning = false;
        }

        final Intent intent = new Intent();
        intent.putExtra("device_address", device.getAddress());
        intent.putExtra("device_name", device.getName());
        setResult(RESULT_OK, intent);
        finish();
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mScanning) {
                        //10 sec
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
        if (scan && !mScanning) {
//            button_find_ble_lock.setText("Scaning");
            mScanning = true;
            mBluetoothLeScanner.startScan(mScanCallback);
            Log.i(Constants.TAG_UnlockService, "Started LE scan");
        } else if (!scan && mScanning) {
//            button_find_ble_lock.setText("Scan for locks");
            mScanning = false;
            mBluetoothLeScanner.stopScan(mScanCallback);
            Log.i(Constants.TAG_UnlockService, "Stopped LE scan");
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private ArrayList<Integer> rssis;
        private ArrayList<byte[]> bRecord;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            rssis = new ArrayList<Integer>();
            bRecord = new ArrayList<byte[]>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device, int rs, byte[] record) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
                rssis.add(rs);
                bRecord.add(record);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
            rssis.clear();
            bRecord.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.deviceName.setTextColor(Color.BLUE);
            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0) {

                viewHolder.deviceName.setText(deviceName);
            } else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress() + "  RSSI:" + String.valueOf
                    (rssis.get(i)));
            //viewHolder.deviceAddress.setText(ByteToString(bRecord.get(i)));

            return view;
        }
    }
}