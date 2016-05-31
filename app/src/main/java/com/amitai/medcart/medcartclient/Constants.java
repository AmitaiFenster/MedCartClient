package com.amitai.medcart.medcartclient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Constants class. cannot be used as an object, this class only contains static values. The
 * values declared here are values that correspond to the hole application.
 */
public class Constants {
    /**
     * String with the URL pointing to the Firebase database.
     */
    public static final String FIREBASE_URL = "https://medcart-556f3.firebaseio.com/";
    /**
     * Use this String for the name of extra data for an Intent used for a Firebase URL.
     */
    public static final String FIREBASE = "FIREBASE";
    /**
     * Use this Tag as a request code to create an Activity that it's purpose is is to enable
     * Bluetooth.
     */
    public static final int REQUEST_ENABLE_BT = 1;
    /**
     * Use this Tag as a request code to create an Activity that it's purpose is is to enable
     * Location.
     */
    public static final int REQUEST_ENABLE_LOCATION = 2;
    /**
     * Use this constant to ask for permission for the Location.
     */
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 3;
    /**
     * Use this constant as a request code to create an Activity with a result of Bluetooth device
     * information after scanning use user choosing a Bluetooth device. See {@link
     * DeviceScanActivity}
     */
    public static final int REQUEST_BLUETOOTH_DEVICE_DATA = 5;
    /**
     * Use this Tag for all Log prints in the context of MainActivity.
     */
    public static final String TAG_MainActivity = "MedCart.MainActivity";
    /**
     * Use this Tag for all Log prints in the context of EnrollCartActivity.
     */
    public static final String TAG_EnrollCartActivity = "EnrollCartActivity";
    /**
     * Use this Tag for all Log prints in the context of LoginHandler.
     */
    public static final String TAG_LoginHandler = "MedCart.LoginHandler";
    /**
     * Use this Tag for all Log prints in the context of UnlockService.
     */
    public static final String TAG_UnlockService = "UnlockService";
    /**
     * Use this action String id to run unlock service using NFC Tag UID. See {@link UnlockService}.
     */
    public static final String ACTION_UNLOCK_USING_NFC = "com.amitai.medcart.medcartclient" +
            ".action.UNLOCK_USING_NFC";
    /**
     * Extra data name to include when running unlock service using NFC Tag UID.
     */
    public static final String EXTRA_NFC_UID = "com.amitai.medcart.medcartclient.extra.NFC_UID";
    /**
     * int that indicates which fragment is currently opened.
     */
    public static int currentFragment = -1;

    /**
     * @return Current Date and time in a DateFormat Object.
     */
    public static DateFormat getDateTime() {
        return SimpleDateFormat.getDateTimeInstance();
    }

    /**
     * @return Current Date and time in a String format.
     */
    public static String getDateTimeString() {
        return getDateTime().format(new Date());
    }

}
