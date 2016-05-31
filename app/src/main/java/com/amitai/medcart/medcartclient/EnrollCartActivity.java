package com.amitai.medcart.medcartclient;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

// TODO: 5/31/2016 add option to check if user has permission in this activity, instead of
// requiring a check for permission before activity is launched.

/**
 * This activity is used to Enroll a new cart. before launching this EnrollCartActivity make sure
 * that the user has permission enrollCart (admin user).
 */
public class EnrollCartActivity extends AppCompatActivity {

    /**
     * This {@link FloatingActionButton} will trigger the upload to Firebase (EnrollCart).
     */
    FloatingActionButton fab;
    /**
     * TextView for the device name that was found after scanning.
     */
    TextView viewDeviceName;
    /**
     * TextView for the device address that was found after scanning.
     */
    TextView viewDeviceAddress;
    /**
     * TextView for the guide of the EditText field {@link #mPasswordView} (enter password field).
     */
    private TextView textview_guide_password;
    /**
     * TextView for the guide of the EditText field {@link #mRelayNumView} (enter relay number
     * field).
     */
    private TextView textview_guide_relayNum;
    /**
     * TextView for the guide of the EditText field {@link #mDescriptionView} (enter
     * description field).
     */
    private TextView textview_guide_description;
    /**
     * TextView to show the uid of the NFC sticker that was scanned.
     */
    private TextView textview_NFC_UID;
    /**
     * Button that when pressed will open the {@link DeviceScanActivity} in order to scan for
     * Bluetooth LE devices.
     */
    private Button button_find_ble_lock;
    /**
     * EditText in which the user enters the password of the Bluetooth LE device.
     */
    private EditText mPasswordView;
    /**
     * EditText in which the user enters the relay number of the lock.
     */
    private EditText mRelayNumView;
    /**
     * EditText in which the user enters the description of the lock.
     */
    private EditText mDescriptionView;
    /**
     * Boolean indicating the uploading status. true if the information is currently being
     * uploaded to the Firebase database, and false otherwise.
     */
    private boolean uploading;
    // TODO: 5/31/2016 switch to use the BluetoothDeviceHolder instead of seprate String fields,
    // like bleAddress and nfcAddress.
    /**
     * String for saving the Bluetooth LE device address that was found and chosen.
     */
    private String bleAddress;
    /**
     * String for saving the scanned NFC Tags address.
     */
    private String nfcAddress;

    /**
     * @param string String to be converted to integer.
     * @return the integer that was converted from the String. if String cannot be converted to
     * integer, -1 will be returned.
     */
    public static int toInteger(String string) {
        int i;
        try {
            i = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return -1;
        } catch (NullPointerException e) {
            return -1;
        }
        return i;
    }

    /**
     * Uploads to Firebase a new Bluetooth LE relay device.
     *
     * @param deviceHolder {@link BluetoothDeviceHolder} with the components of the device wished
     *                     to upload to Firebase.
     */
    private void uploadFirebase(BluetoothDeviceHolder deviceHolder) {
        Map<String, Object> map = new HashMap<String, Object>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // TODO: 5/26/2016 Check if better to migrate to getRefrence(String path) instead of
        // getReferenceFromUrl.
        DatabaseReference firebaseRelaysRef = database.getReferenceFromUrl(Constants.FIREBASE_URL
                + "relays/" + deviceHolder.getNfcAddress());

        map.put("BLERelayNum", deviceHolder.getRelayNum());
        map.put("BLEuid", deviceHolder.getBluetoothAddreess());
        map.put("description", deviceHolder.getDescription());
        map.put("NFCuid", deviceHolder.getNfcAddress());
        map.put("password", deviceHolder.getPassword());

        Map<String, String> mapAuthorized = new HashMap<String, String>();
        mapAuthorized.put(LoginHandler.getAuthUid(), "true");
        map.put("authorized", mapAuthorized);

        firebaseRelaysRef.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                Log.d(Constants.TAG_EnrollCartActivity, "onComplete");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d(Constants.TAG_EnrollCartActivity, "onFailure");
                Toast.makeText(EnrollCartActivity.this, "Operation fail", Toast
                        .LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(EnrollCartActivity.this, "Lock added successfully", Toast
                        .LENGTH_SHORT).show();
                Log.d(Constants.TAG_EnrollCartActivity, "onSuccess");
            }
        });

        DatabaseReference firebaseUserRef = database.getReferenceFromUrl(Constants.FIREBASE_URL
                + "users/" + LoginHandler.getAuthUid() + "/authorized/" + deviceHolder
                .getNfcAddress());
        firebaseUserRef.setValue("true");

        // TODO: 5/30/2016 Add a check if lock was really added successfully by checking firebase.
//        Toast.makeText(EnrollCartActivity.this, "Lock added successfully", Toast.LENGTH_SHORT)
//                .show();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_cart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        EnablingComponents.enableAllComponents(this, BluetoothAdapter.getDefaultAdapter());
        components();
        uploading = false;
    }

    /**
     * Initializing other views and components. this method is usually called by the OnCreate
     * method.
     */
    private void components() {
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
                intent.putExtra(DeviceScanActivity.REQUEST_START_SCAN_IMMEDIATELY, true);
                startActivityForResult(intent, Constants.REQUEST_BLUETOOTH_DEVICE_DATA);
            }
        });
        viewDeviceAddress = (TextView) findViewById(R.id.device_address);
        viewDeviceName = (TextView) findViewById(R.id.device_name);
        mDescriptionView = (EditText) findViewById(R.id.editTextDescription);
        mPasswordView = (EditText) findViewById(R.id.editTextPassword);
        mRelayNumView = (EditText) findViewById(R.id.editTextRelayNum);
        fab.setVisibility(View.GONE);
        viewDeviceAddress.setVisibility(View.GONE);
        viewDeviceName.setVisibility(View.GONE);
        mDescriptionView.setVisibility(View.GONE);
        mPasswordView.setVisibility(View.GONE);
        mRelayNumView.setVisibility(View.GONE);
    }

    /**
     * Set the visibility of the hidden views to visible. use this when ready for the user to
     * enter the device information (usually after the NFC and Bluetooth device were found).
     */
    private void makeViewsVisible() {
        fab.setVisibility(View.VISIBLE);
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
                (this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // to catch all NFC discovery events:
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);


    }

    @Override
    public void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            String NFC_UID = NFC.ByteArrayToStringDisplayFormat(intent
                    .getByteArrayExtra(NfcAdapter.EXTRA_ID));
            newNFCDetected(NFC_UID);
        }
    }

    /**
     * Use this method to handle a new NFC Tag that was detected. this method checks if the NFC
     * is being used (exists in the firebase database). If it being used a dialog will launch to
     * ask the user whether he wants to override the NFC Tag data on the database for the new tag
     * corresponding to the NFC_UID.
     * <p/>
     * If the NFC tag is not being used, or if the user chose to overwrite, the NFC_UID will be
     * saved as the NFC address and the NFC address TextView will be set to the NFC_UID.
     *
     * @param NFC_UID UID of the detected NFC Tag.
     */
    public void newNFCDetected(final String NFC_UID) {
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

//                        Toast.makeText(EnrollCartActivity.this, "NFC tag if being used for a " +
//                                "different lock", Toast.LENGTH_SHORT).show();
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

    /**
     * setting the NFC address TextView to the new NFC and setting the nfcAddress to the new
     * NFC_UID.
     *
     * @param NFC_UID
     */
    private void setNewNFC(String NFC_UID) {
        textview_NFC_UID.setText("NFC UID:  " + NFC_UID);
        nfcAddress = NFC_UID;
        button_find_ble_lock.setVisibility(View.VISIBLE);
    }

    /**
     * Setting the TextView for the new bluetooth information.
     *
     * @param deviceName    The newly detected Bluetooth device name.
     * @param deviceAddress The newly detected Bluetooth device name.
     */
    private void setChosenBleView(String deviceName, String deviceAddress) {
        viewDeviceAddress.setText(deviceAddress);
        viewDeviceName.setText(deviceName);
    }

    /**
     * Is the password is valid.
     * <p/>
     * A valid password is a password that is longer than 4 characters.
     *
     * @param password password to be checked.
     * @return true if the password is valid and false otherwise.
     */
    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Upload all the device properties that was entered in this EnrollCartActivity to Firebase.
     * This method checks if all af the information (the components) is valid. If not, an error
     * will be displayed to the user and the upload will cancel.
     * <p/>
     * The components: relayNumber, password, description, bleAddress, nfcAddress.
     */
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

    /**
     * Hide the keyboard. Use when uploading process begins and the is no need for a keyboard.
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context
                .INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mDescriptionView.getWindowToken(), 0);
    }
}
