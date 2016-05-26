package com.amitai.medcart.medcartclient;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EnrollCartActivity extends AppCompatActivity {

    FloatingActionButton fab;
    TextView viewDeviceName;
    TextView viewDeviceAddress;
    private TextView textview_guide_password;
    private TextView textview_guide_relayNum;
    private TextView textview_guide_description;
    private TextView textview_NFC_UID;
    private Button button_find_ble_lock;
    private EditText mPasswordView;
    private EditText mRelayNumView;
    private EditText mDescriptionView;
    private boolean uploading;
    private String bleAddress;
    private String nfcAddress;

    public static int toInteger(String s) {
        int i;
        try {
            i = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1;
        } catch (NullPointerException e) {
            return -1;
        }
        return i;
    }

    private void uploadFirebase(BluetoothDeviceHolder deviceHolder) {
        Map<String, Object> map = new HashMap<String, Object>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // TODO: 5/26/2016 Check if better to migrate to getRefrence(String path) instead of
        // getReferenceFromUrl.
        DatabaseReference firebaseRelaysRef = database.getReferenceFromUrl(Constants.FIREBASE_URL
                + "relays/" + deviceHolder.getNfcAddress());

        map.put("BLERelayNum", deviceHolder.getRelayNum());
        map.put("BLEuid", deviceHolder.getBluetoothAddreess());
        map.put("Description", deviceHolder.getDescription());
        map.put("NFCuid", deviceHolder.getNfcAddress());
        map.put("password", deviceHolder.getPassword());

        Map<String, String> mapAuthorized = new HashMap<String, String>();
        mapAuthorized.put(LoginHandler.getAuthUid(), "true");
        map.put("authorized", mapAuthorized);

        firebaseRelaysRef.setValue(map);

        Toast.makeText(EnrollCartActivity.this, "Lock added successfully", Toast.LENGTH_SHORT)
                .show();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_cart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload();
            }
        });
        textview_NFC_UID = (TextView) findViewById(R.id.NFC_UID);
        textview_guide_description = (TextView) findViewById(R.id.textViewEnterDescription);
        textview_guide_password = (TextView) findViewById(R.id.textViewEnterPassword);
        textview_guide_relayNum = (TextView) findViewById(R.id.textViewEnterRelayNum);
        textview_guide_description.setVisibility(View.GONE);
        textview_guide_password.setVisibility(View.GONE);
        textview_guide_relayNum.setVisibility(View.GONE);

        button_find_ble_lock = (Button) findViewById(R.id.find_ble_lock);
        button_find_ble_lock.setVisibility(View.GONE);
        button_find_ble_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EnrollCartActivity.this, DeviceScanActivity.class);
                startActivityForResult(intent, Constants.REQUEST_BLUETOOTH_DEVICE_DATA);
            }
        });
        viewDeviceAddress = (TextView) findViewById(R.id.device_address);
        viewDeviceName = (TextView) findViewById(R.id.device_name);
        mDescriptionView = (EditText) findViewById(R.id.editTextDescription);
        mPasswordView = (EditText) findViewById(R.id.editTextPassword);
        mRelayNumView = (EditText) findViewById(R.id.editTextRelayNum);
        viewDeviceAddress.setVisibility(View.GONE);
        viewDeviceName.setVisibility(View.GONE);
        mDescriptionView.setVisibility(View.GONE);
        mPasswordView.setVisibility(View.GONE);
        mRelayNumView.setVisibility(View.GONE);

        uploading = false;

    }

    private void makeViewsVisible() {
        viewDeviceAddress.setVisibility(View.VISIBLE);
        viewDeviceName.setVisibility(View.VISIBLE);
        mDescriptionView.setVisibility(View.VISIBLE);
        mPasswordView.setVisibility(View.VISIBLE);
        mRelayNumView.setVisibility(View.VISIBLE);
        textview_guide_description.setVisibility(View.VISIBLE);
        textview_guide_password.setVisibility(View.VISIBLE);
        textview_guide_relayNum.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_BLUETOOTH_DEVICE_DATA && resultCode == RESULT_OK) {
            bleAddress = data.getStringExtra("device_address");
            String name = data.getStringExtra("device_name");
            setChosenBleView(name, bleAddress);
            makeViewsVisible();
        }
        // TODO: 5/22/2016 add option to return result not ok if bluetooth searched was not
        // successful.
    }

    @Override
    public void onPause() {
        super.onPause();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent
                (this,
                        getClass()
                ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // to catch all NFC discovery events:
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);


    }

    public void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            String NFC_UID = NFC.ByteArrayToStringDisplayFormat(intent
                    .getByteArrayExtra(NfcAdapter.EXTRA_ID));
            newNFCDetected(NFC_UID);
        }
    }

    public void newNFCDetected(final String NFC_UID) {
        // TODO: 5/24/2016 Add dialog to give the user a option to override the nfc relay data on
        // the firebase database.

        DatabaseReference firebaseRelaysRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(Constants.FIREBASE_URL + "relays");
        firebaseRelaysRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(EnrollCartActivity.this, "Cannot contact database", Toast
                            .LENGTH_SHORT).show();
                    return;
                }
                Iterable<DataSnapshot> relays = dataSnapshot.getChildren();
                boolean isNFCUsed = false;
                for (DataSnapshot relayData : relays) {
                    if (relayData.getKey().equals(NFC_UID)) {

                        // 1. Instantiate an AlertDialog.Builder with its constructor
                        AlertDialog.Builder builder = new AlertDialog.Builder(EnrollCartActivity
                                .this);

                        // 2. Chain together various setter methods to set the dialog
                        // characteristics
                        builder.setMessage("NFC tag if being used for a different lock. " +
                                "\n\nDo you want to override?")
                                .setTitle("NFC Tag Used");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                setNewNFC(NFC_UID);
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });

                        // 3. Get the AlertDialog from create()
                        AlertDialog dialog = builder.create();
                        dialog.show();

                        Toast.makeText(EnrollCartActivity.this, "NFC tag if being used for a " +
                                "different lock", Toast.LENGTH_SHORT).show();
                        isNFCUsed = true;
                        break;
                    }
                }
                if (!isNFCUsed) {
                    setNewNFC(NFC_UID);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(EnrollCartActivity.this, "Cannot contact database", Toast
                        .LENGTH_SHORT).show();
            }
        });

    }

    private void setNewNFC(String NFC_UID) {
        textview_NFC_UID.setText("NFC UID:  " + NFC_UID);
        nfcAddress = NFC_UID;
        button_find_ble_lock.setVisibility(View.VISIBLE);
    }

    private void setChosenBleView(String deviceName, String deviceAddress) {
        viewDeviceAddress.setText(deviceAddress);
        viewDeviceName.setText(deviceName);
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    public void upload() {
        if (uploading) {
            return;
        }

        mRelayNumView.setError(null);
        mPasswordView.setError(null);

        String relayNum = mRelayNumView.getText().toString();
        String password = mPasswordView.getText().toString();
        String description = mDescriptionView.getText().toString();

        boolean cancelUpload = false;
        View focusView = null;

        if (!(bleAddress != null && bleAddress.length() == 17 && nfcAddress != null && nfcAddress
                .length() > 0)) {
            cancelUpload = true;
        }

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.invalid_password));
            focusView = mPasswordView;
            cancelUpload = true;
        }

        if (TextUtils.isEmpty(relayNum)) {
            mRelayNumView.setError(getString(R.string.field_required));
            focusView = mRelayNumView;
            cancelUpload = true;
        } else if (toInteger(relayNum) == -1) {
            mRelayNumView.setError(getString(R.string.invalid_relay_num));
            focusView = mRelayNumView;
            cancelUpload = true;
        }

        if (TextUtils.isEmpty(description)) {
            mDescriptionView.setError(getString(R.string.field_required));
            focusView = mDescriptionView;
            cancelUpload = true;
        }

        if (cancelUpload) {
            // error in upload
            if (focusView != null)
                focusView.requestFocus();
        } else {
            uploading = true;
            uploadFirebase(new BluetoothDeviceHolder(bleAddress, toInteger(relayNum), password,
                    nfcAddress, description));
            hideKeyboard();
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context
                .INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mDescriptionView.getWindowToken(), 0);
    }
}
