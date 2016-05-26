package com.amitai.medcart.medcartclient;

import android.content.Intent;
import android.nfc.NfcAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The NFC class handles all of the operations on NFC intent data.
 */
public class NFC {

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

    /**
     * @param intent intent of the current scanned nfc tag.
     * @return TagUID  String with the current time and the NFC UID of the passed intent.
     */
    public static String getTagUID(Intent intent) {
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            byte[] rawId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            String hexStringID = ByteArrayToHexString(rawId);
            String hexStringIDfinal = stringUIDDisplayFormat(hexStringID);
            Map<String, String> datum = new HashMap<String, String>(2);
            datum.put("title", hexStringIDfinal);
            datum.put("date", getDateTimeString());
            return datum.toString();
        }
        return "";
    }

    public static String ByteArrayToStringDisplayFormat(byte[] inarray) {
        return stringUIDDisplayFormat(ByteArrayToHexString(inarray));
    }

    /**
     * @param ID String with numbers only that represent the nfc tag UID.
     * @return String with the UID in a display format, with dashes between each byte.
     */
    public static String stringUIDDisplayFormat(String ID) {
        char[] IDcharArray = ID.toCharArray();
        String hexStringIDfinal = "" + IDcharArray[0] + IDcharArray[1];
        for (int i = 2; i < IDcharArray.length; i += 2)
            hexStringIDfinal += ":" + IDcharArray[i] + IDcharArray[i + 1];
        return hexStringIDfinal;
    }

    /**
     * Converts between byte array and hexadecimal.
     *
     * @param inarray array of bytes.
     * @return String that contains a hexadecimal address.
     */
    public static String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D",
                "E", "F"};
        String out = "";

        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

}
