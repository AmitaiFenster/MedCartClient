package com.amitai.medcart.medcartclient;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";


    ListView myList;
    View rootView;
    //ListView explanation: http://stackoverflow.com/a/7917516/4038549
    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS.
    List<Map<String, String>> listData;
    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW.
    String repoUrl;
    // TODO: Rename and change types of parameters
//    private String mParam1;
    private OnFragmentInteractionListener mListener;
    private TextView mainText;


    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param repoUrl this specific user UID Firebase URL.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String repoUrl) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(Constants.FIREBASE, repoUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            repoUrl = getArguments().getString(Constants.FIREBASE);
        }
        getArguments();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    /**
     * Initializing other views and components. this method is usually called by the OnCreate
     * method.
     */
    private void components() {

        //TODO: remove if not needed. If using nfc foreground detection or opening already
        // running activities and adding the nfc intent to a list, than use this:
//        mPendingIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
//
//        mainText = (TextView) rootView.findViewById(R.id.unlockTextView);

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
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
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

}
