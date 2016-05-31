package com.amitai.medcart.medcartclient;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.nfc.NfcAdapter;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

/**
 * class with only static methods for enabling the components needed in the app: Bluetooth, NFC and
 * Location.
 */
public class EnablingComponents {

    /**
     * @param activity
     * @param mBluetoothAdapter
     * @return true if all components are enabled, ond false otherwise. components: NFC,
     * Bluetooth, Location.
     */
    public static boolean isComponentsEnabled(Activity activity, BluetoothAdapter
            mBluetoothAdapter) {
        return isBluetoothEnabled(mBluetoothAdapter, activity) && isLocationEnabled(activity) &&
                isNFCEnabled(activity);
    }

    /**
     * @param activity
     * @return true if NFC in enabled and available and false otherwise.
     */
    public static boolean isNFCEnabled(Activity activity) {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if (nfcAdapter == null || !nfcAdapter.isEnabled())
            return false;
        return true;
    }

    /**
     * Enabling all components, including: NFC, Bluetooth, Location.
     *
     * @param activity Context activity
     * @return true if all components are enabled, and false otherwise.
     */
    public static boolean enableAllComponents(Activity activity, BluetoothAdapter
            mBluetoothAdapter) {
        // TODO: 5/31/2016 Check if can romove the argument of the BluetoothAdapter and use
        // BluetoothAdapter.getDefaultAdapter()
        return enableNFC(activity) && enableBluetooth(activity, mBluetoothAdapter) &&
                enableLocation(activity);
    }

    /**
     * this method checks if nfc is turned on (enabled). if not, the nfc wireless settings dialog
     * will be opened so that the user can manually turn on nfc.
     *
     * @param activity Context activity
     * @return True if nfc is enabled. and false otherwise.
     */
    private static boolean enableNFC(Activity activity) {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if (nfcAdapter == null) {
            Toast.makeText(activity, R.string.no_nfc, Toast.LENGTH_LONG)
                    .show();
        } else if (!nfcAdapter.isEnabled()) {
            showWirelessSettingsDialog(activity);
        } else {
            return true;
        }
        return false;
    }

    /**
     * @param activity
     * @return true is location is enabled and false otherwise.
     */
    public static boolean isLocationEnabled(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            return false;
        LocationManager manager = (LocationManager) activity.getSystemService(Context
                .LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            return false;
        return true;
    }

    /**
     * this method checks if location is turned on (enabled). if not, an activity will be started
     * to prompt the user to turn on location.
     *
     * @param activity Context activity
     * @return true if location is enabled, and false otherwise.
     */
    private static boolean enableLocation(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission
                .ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    Constants.MY_PERMISSIONS_REQUEST_LOCATION);
            Toast.makeText(activity, "Please enable permission", Toast
                    .LENGTH_LONG).show();
            return false;
        } else {
            LocationManager manager = (LocationManager) activity.getSystemService(Context
                    .LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                activity.startActivityForResult(enableLocationIntent,
                        Constants.REQUEST_ENABLE_LOCATION);
                return false;
            }
        }
        return true;
    }

    /**
     * Method to turn on Bluetooth.
     * <p/>
     * Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled, fire
     * an intent to display a dialog asking the user to grant permission to enable it.
     *
     * @param activity          Context activity
     * @param mBluetoothAdapter
     * @return true if Bluetooth is enabled, and false otherwise.
     */
    public static boolean enableBluetooth(Activity activity, BluetoothAdapter mBluetoothAdapter) {
        if (!isBluetoothEnabled(mBluetoothAdapter, activity)) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
            return false;
        }
        return true;
    }

    /**
     * @param mBluetoothAdapter
     * @param activity
     * @return true if bluetooth is enabled and false otherwise.
     */
    public static boolean isBluetoothEnabled(BluetoothAdapter mBluetoothAdapter, Activity
            activity) {
        return (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && activity
                .getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE));
    }

    /**
     * Opens the Wireless Settings Dialog so that the user could turn on the NFC.
     *
     * @param activity Context activity
     */
    private static void showWirelessSettingsDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.nfc_disabled);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                activity.startActivity(intent);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
//                toolbarSwitch.setChecked(false);
            }
        });
        builder.create().show();
    }

}
