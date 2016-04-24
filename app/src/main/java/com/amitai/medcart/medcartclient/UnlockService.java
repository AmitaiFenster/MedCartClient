package com.amitai.medcart.medcartclient;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class UnlockService extends IntentService {
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
        BLEConnection = new BLEScanConnect(bluetoothAddreess, this);
        BLEConnection.scanLeDevice(true);
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
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
}
