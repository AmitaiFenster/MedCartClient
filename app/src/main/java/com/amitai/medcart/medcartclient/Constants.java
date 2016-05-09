package com.amitai.medcart.medcartclient;

/**
 * Created by amita on 5/4/2016.
 */
public class Constants {
    public static final String FIREBASE_URL = "https://medcart.firebaseio.com/";
    public static final String FIREBASE = "FIREBASE";
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_ENABLE_LOCATION = 2;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 3;
    public static final int REQUEST_CODE_GOOGLE_LOGIN = 4;
    public static final String TAG_MainActivity = "MedCart.MainActivity";
    public static final String TAG_LoginHandler = "MedCart.LoginHandler";
    public static final String TAG_UnlockService = "UnlockService";
    /**
     * Use this action to run unlock service using NFC Tag UID.
     */
    public static final String ACTION_UNLOCK_USING_NFC = "com.amitai.medcart.medcartclient" +
            ".action.UNLOCK_USING_NFC";
    /**
     * Extra data to include when running unlock service using NFC Tag UID.
     */
    public static final String EXTRA_NFC_UID = "com.amitai.medcart.medcartclient.extra.NFC_UID";

}
