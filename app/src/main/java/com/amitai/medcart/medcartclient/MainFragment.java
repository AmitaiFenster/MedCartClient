package com.amitai.medcart.medcartclient;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 * This Fragment {@link Fragment} shows the list of devices the user is authorized to control,
 * and if the user clicks on a device from the list the device lock will open.
 */
public class MainFragment extends Fragment {

    /**
     * ListView to show the list of devices the user is authorized to control.
     */
    ListView myList;
    /**
     * ArrayList for holding the list of the descriptions (names) of devices the user is authorized
     * to control.
     */
    ArrayList<String> authArrayList = new ArrayList<>();
    /**
     * ArrayList for holding the list of the NFC UID of devices the user is authorized
     * to control.
     */
    ArrayList<String> nfcArrayList = new ArrayList<>();
    /**
     * ArrayAdapter for handling the interaction between the ListView and the ArrayList of
     * devices the user is authorized to control.
     */
    ArrayAdapter<String> adapter;
    /**
     * The root {@link View} of this fragment.
     */
    View rootView;
    // TODO: 5/31/2016 Check if repoUrl can be removed.
    String repoUrl;
    /**
     * Listener for listening for interaction with the {@link android.app.Activity Activity}.
     */
    private OnFragmentInteractionListener mListener;

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
     * Initializing the list data of the authorized relays. Data received from Firebase and
     * inserted to the authArrayList. adapter.notifyDataSetChanged() is called.
     */
    private void listDataSetup() {
        adapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_list_item_1,
                authArrayList);

        final DatabaseReference authRef = FirebaseDatabase.getInstance().getReferenceFromUrl
                (Constants.FIREBASE_URL + "users/" + LoginHandler.getAuthUid() + "/authorized");

        authRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final String key = dataSnapshot.getKey();

                final DatabaseReference relayRef = FirebaseDatabase.getInstance()
                        .getReferenceFromUrl(Constants.FIREBASE_URL + "relays/" + key +
                                "/description");
                relayRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            authArrayList.add(dataSnapshot.getValue(String.class));
                            nfcArrayList.add(key);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        listSetup();
    }

    /**
     * Setting up the ListView of the authorized relays.
     */
    private void listSetup() {
        myList = (ListView) getView().findViewById(R.id.authDevicesList);
        myList.setAdapter(adapter);
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String nfcUID = nfcArrayList.get(position);
                UnlockActivity.startUnlockActivity(getActivity(), nfcUID);
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        listDataSetup();
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
