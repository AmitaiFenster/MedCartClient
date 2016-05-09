package com.amitai.medcart.medcartclient;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.client.Firebase;

public class UnlockActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_unlock);
        // TODO: 5/9/2016 Delete layout activity_unlock if not used.

        Firebase.setAndroidContext(this);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!LoginHandler.isLoggedIn()) {
            Toast.makeText(UnlockActivity.this, "Please login!", Toast.LENGTH_LONG).show();
            finish();
        }

        Intent intent = getIntent();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) && EnablingComponents
                .enableAllComponents(this, mBluetoothAdapter)) {

            UnlockService.startActionUnlockUsingNFC(this, NFC.ByteArrayToStringDisplayFormat(intent
                    .getByteArrayExtra(NfcAdapter.EXTRA_ID)), Constants.FIREBASE_URL + "/users/"
                    + LoginHandler.getAuthUid());
        }

        finish();
    }

}
