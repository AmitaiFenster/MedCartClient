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

import java.util.ArrayList;


/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends ListActivity {
    public static final String REQUEST_START_SCAN_IMMEDIATELY = "DeviceScanActivity" +
            ".RequestStartScanImmediately";
    /**
     * SCAN_PERIOD to specify how many milliseconds to scan. for example, =5000 is to Stops
     * scanning after 5 seconds.
     */
    private static final long SCAN_PERIOD = 5000;
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
            Log.i(Constants.TAG_UnlockService, "Scan Faild!");
        }
    };
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    /**
     * boolean indicating weather the {@link BluetoothLeScanner} is scanning or not. scanning =
     * true, not scanning = false.
     */
    private boolean mScanning;
    private Handler mHandler;
    /**
     * Button that is used to control the start or stop scanning.
     */
    private Button buttonScan;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();
        setContentView(R.layout.activity_scan);

        buttonScan = (Button) findViewById(R.id.buttonscan);

        buttonScan.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                if (mScanning)
                    scanLeDevice(false);
                else {
                    mLeDeviceListAdapter.clear();
                    scanLeDevice(true);
                }
            }
        });

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        EnablingComponents.enableAllComponents(this, mBluetoothAdapter);

        // TODO: 5/27/2016 Test this statement when bluetooth was turned off.
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

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);

        if (getIntent().getBooleanExtra(REQUEST_START_SCAN_IMMEDIATELY, false)) {
            mLeDeviceListAdapter.clear();
            scanLeDevice(true);
        }
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
            scanLeDevice(false);
        }

        final Intent intent = new Intent();
        intent.putExtra("device_address", device.getAddress());
        intent.putExtra("device_name", device.getName());
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * @param enable true to start scan and stop after scan period,
     */
    private void scanLeDevice(final boolean enable) {
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
        if (scan && !mScanning) {
            buttonScan.setText("Scanning");
            mScanning = true;
            mBluetoothLeScanner.startScan(mScanCallback);
            Log.i(Constants.TAG_UnlockService, "Started LE scan");
        } else if (!scan && mScanning) {
            buttonScan.setText("Scan");
            mScanning = false;
            mBluetoothLeScanner.stopScan(mScanCallback);
            Log.i(Constants.TAG_UnlockService, "Stopped LE scan");
        }
    }

    /**
     * this ViewHolder class holds two TextViews.
     */
    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    /**
     * Adapter for holding Bluetooth devices found through scanning.
     */
    private class LeDeviceListAdapter extends BaseAdapter {
        /**
         * {@link ArrayList} of the Bluetooth devices (each item is by the type {@link
         * BluetoothDevice}
         */
        private ArrayList<BluetoothDevice> mLeDevices;
        /**
         * {@link ArrayList} of the received signal strength indicator of the Bluetooth devices.
         */
        private ArrayList<Integer> rssis;
        /**
         * The records of each Bluetooth device. each record is a <code>array of bytes</code>.
         */
        private ArrayList<byte[]> bRecord;
        /**
         * {@link LayoutInflater} that is used to inflate the Views needed by this adapter.
         */
        private LayoutInflater mInflator;

        /**
         * Constructor that initializes the adapters arrayLists and initializes the {@link
         * LayoutInflater}
         */
        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            rssis = new ArrayList<Integer>();
            bRecord = new ArrayList<byte[]>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        /**
         * Adding a new device to the devices list in the adapter.
         *
         * @param device the new bluetooth device to be added.
         * @param rs     the received signal strength indicator on the new Bluetooth device.
         * @param record
         */
        public void addDevice(BluetoothDevice device, int rs, byte[] record) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
                rssis.add(rs);
                bRecord.add(record);
            }
        }

        /**
         * @param position position in which the user clicked.
         * @return {@link BluetoothDevice} - the bluetooth device corresponding to the item the
         * user clicked.
         */
        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        /**
         * Clear all the data in the adapter.
         */
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