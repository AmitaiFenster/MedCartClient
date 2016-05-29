package com.amitai.medcart.medcartclient;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class UnlockActivity extends AppCompatActivity {

    public static final String EXTRA_NFC_ID = "com.amitai.medcart.medcartclient" +
            ".UnlockActivity.extra.nfcID";
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!LoginHandler.isLoggedIn()) {
            Toast.makeText(UnlockActivity.this, "Please Login!", Toast.LENGTH_LONG).show();
            finish();
        }

        EnablingComponents.enableAllComponents(this, mBluetoothAdapter);

        Intent intent = getIntent();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) && EnablingComponents
                .isComponentsEnabled(this, mBluetoothAdapter)) {

            UnlockService.startActionUnlockUsingNFC(this, NFC.ByteArrayToStringDisplayFormat(intent
                    .getByteArrayExtra(NfcAdapter.EXTRA_ID)), Constants.FIREBASE_URL + "/users/"
                    + LoginHandler.getAuthUid());
        }
        if (intent.getAction().equals(Constants.ACTION_UNLOCK_USING_NFC) && EnablingComponents
                .isComponentsEnabled(this, mBluetoothAdapter)) {
            UnlockService.startActionUnlockUsingNFC(this, intent.getStringExtra(this
                    .EXTRA_NFC_ID), Constants.FIREBASE_URL + "/users/" + LoginHandler.getAuthUid());
        }

        finish();
    }

    public static void startUnlockActivity(Activity activity, String nfcUID) {
        Intent intent = new Intent(Constants.ACTION_UNLOCK_USING_NFC);
        intent.putExtra(UnlockActivity.EXTRA_NFC_ID, nfcUID);
        intent.setClass(activity, UnlockActivity.class);
        activity.startActivity(intent);
    }

}
