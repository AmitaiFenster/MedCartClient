package com.amitai.medcart.medcartclient;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UnlockFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UnlockFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UnlockFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final DateFormat TIME_FORMAT = SimpleDateFormat.getDateTimeInstance();
    private NfcAdapter mAdapter;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private TextView mainText;
    ListView myList;

    View rootView;

    //ListView explanation: http://stackoverflow.com/a/7917516/4038549
    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS.
    List<Map<String, String>> listData;
    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW.
    SimpleAdapter adapter;


    public UnlockFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UnlockFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UnlockFragment newInstance(/*String param1, String param2*/) {
        UnlockFragment fragment = new UnlockFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getActivity().getIntent();
        showTagUID(intent);
    }

    /**
     * Initializing other views and components. this method is usually called by the OnCreate method.
     */
    private void components() {

        //TODO: remove if not needed. If using nfc foreground detection or opening already running activities and adding the nfc intent to a list, than use this:
//        mPendingIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
//
        mainText = (TextView) rootView.findViewById(R.id.unlockTextView);
//        myList = (ListView) getActivity().findViewById(R.id.unlockListView);
//        listData = new ArrayList<Map<String, String>>();
//        adapter = new SimpleAdapter(getActivity(), listData,
//                R.layout.simple_list_item_2,
//                new String[]{"title", "date"},
//                new int[]{R.id.listText1,
//                        R.id.listText2});
//        myList.setAdapter(adapter);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_unlock, container, false);
        components();
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    /**
     * Converts between byte array and hexadecimal.
     *
     * @param inarray array of bytes.
     * @return String that contains a hexadecimal address.
     */
    private String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
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

    /**
     * Adds to the ListView the nfc UID of passed intent.
     *
     * @param intent intent of the current scanned nfc tag.
     */
    private void showTagUID(Intent intent) {
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            byte[] rawId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            String hexStringID = ByteArrayToHexString(rawId);
            String hexStringIDfinal = stringUIDDisplayFormat(hexStringID);
            Map<String, String> datum = new HashMap<String, String>(2);
            datum.put("title", hexStringIDfinal);
            datum.put("date", TIME_FORMAT.format(new Date()));
            mainText.setText(datum.toString());
        }
    }

    /**
     * @param ID String with numbers only that represent the nfc tag UID.
     * @return String with the UID in a display format, with dashes between each byte.
     */
    private String stringUIDDisplayFormat(String ID) {
        char[] IDcharArray = ID.toCharArray();
        String hexStringIDfinal = "" + IDcharArray[0] + IDcharArray[1];
        for (int i = 2; i < IDcharArray.length; i += 2)
            hexStringIDfinal += "-" + IDcharArray[i] + IDcharArray[i + 1];
        return hexStringIDfinal;
    }

}
