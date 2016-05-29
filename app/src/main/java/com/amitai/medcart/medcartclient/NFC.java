package com.amitai.medcart.medcartclient;

import android.content.Intent;
import android.nfc.NfcAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * The NFC class handles all of the operations on NFC intent data.
 */
public class NFC {

    /**
     * @param intent {@link Intent} of the current scanned nfc tag.
     * @return TagUID <code>String</code> with the current time and the NFC UID of the passed
     * intent.
     */
    public static String getTagUID(Intent intent) {
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            byte[] rawId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            String hexStringID = ByteArrayToHexString(rawId);
            String hexStringIDfinal = stringUIDDisplayFormat(hexStringID);
            Map<String, String> datum = new HashMap<String, String>(2);
            datum.put("title", hexStringIDfinal);
            datum.put("date", Constants.getDateTimeString());
            return datum.toString();
        }
        return "";
    }

    /**
     * @param inarray array of bytes containing the UID (in the format that is received from the
     *                NFC detected intent). The UID is received from the {@link
     *                Intent#getByteArrayExtra(String)} (the intent of the detected NFC)
     *                and providing as a parameter {@link NfcAdapter#EXTRA_ID}.
     * @return <code>String</code> with the NFC UID in a display format, with a ':' dividing the
     * hexadecimal numbers.
     */
    public static String ByteArrayToStringDisplayFormat(byte[] inarray) {
        return stringUIDDisplayFormat(ByteArrayToHexString(inarray));
    }

    /**
     * @param ID <code>String</code> with numbers only that represent the nfc tag UID.
     * @return <code>String</code> with the UID in a display format, with dashes between each byte.
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
     * @return <code>String</code> that contains a hexadecimal address.
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
