package com.amitai.medcart.medcartclient;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by amita on 5/5/2016.
 */
public class LoginHandler {

    private static final int RC_SIGN_IN = 100;
    MainActivity activity;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public LoginHandler(Activity activity) {
        try {
            this.activity = (MainActivity) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement MainActivity");
        }
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(Constants.TAG_LoginHandler, "onAuthStateChanged:signed_in:" + user
                            .getUid());
                } else {
                    // User is signed out
                    Log.d(Constants.TAG_LoginHandler, "onAuthStateChanged:signed_out");
                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
    }

    public static boolean isLoggedIn() {
        // TODO: 5/25/2016 add expiration!
        return getFirebaseUser() != null /* && !isExpired(auth)*/;
    }

    public static FirebaseUser getFirebaseUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    // TODO: 5/25/2016 fix isExpired
//    private static boolean isExpired(FirebaseAuth authData) {
//        return (System.currentTimeMillis() / 1000) >= authData.getExpires();
//    }

    public static String getAuthUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
            return user.getUid();
        return null;
    }

    public void activityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == this.activity.RESULT_OK) {
                // user is signed in!
                Log.d(Constants.TAG_LoginHandler, "Sign in result: RESULT_OK");
                logUserInfo();
                tryAccessMainFragment();
//                finish();
            } else {
                Log.d(Constants.TAG_LoginHandler, "Sign in result: RESULT_CANCELLED");
                // user is not signed in. Maybe just wait for the user to press
                // "sign in" again, or show a message
            }
        }
    }

    /**
     * This method tries to switch to main fragment if signed in. If not, the LoginFragment will
     * be shown.
     */
    public void tryAccessMainFragment() {
        if (!isLoggedIn()) {
//            this.activity.fab.hide();
            Toast.makeText(this.activity, "Please login!", Toast.LENGTH_LONG).show();
            switchToLogin();
        } else {
//            this.activity.fab.show();
            switchToMainFragment(Constants.FIREBASE_URL + "/users/" + mAuth.getCurrentUser()
                    .getUid());
        }
    }

    public void switchToLogin() {
        this.activity.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setProviders(
                                AuthUI.EMAIL_PROVIDER,
                                AuthUI.GOOGLE_PROVIDER,
                                AuthUI.FACEBOOK_PROVIDER)
                        .build(),
                RC_SIGN_IN);
    }

    private void switchToMainFragment(String repoUrl) {
        this.activity.setTitle(this.activity.getResources().getString(R.string.app_name2));
        this.activity.setNavigationViewChecked(0);
        FragmentTransaction fragmentTransaction = this.activity.getFragmentManager()
                .beginTransaction();
        Fragment mainFragment = MainFragment.newInstance(repoUrl);
        fragmentTransaction.replace(R.id.content_frame, mainFragment, "Main");
        fragmentTransaction.commit();
    }

    public void logout() {
        AuthUI.getInstance()
                .signOut(this.activity)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // user is now signed out
                        switchToLogin();
                    }
                });
    }

    private void logUserInfo() {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReferenceFromUrl
                (Constants.FIREBASE_URL + "/users/" + getAuthUid());
        final FirebaseUser firebaseUser = mAuth.getCurrentUser();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("email", firebaseUser.getEmail());
                    map.put("provider", firebaseUser.getProviderId());
                    map.put("level", "user");
//                        if (firebaseUser.getProviderData().containsKey("displayName")) {
//                            map.put("displayName", firebaseUser.getProviderData().get
//                                    ("displayName")
//                                    .toString());
//                        }
                    map.put("displayName", firebaseUser.getProviderData().get(0)
                            .getDisplayName());
                    ref.setValue(map);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(activity, "cannot log your information", Toast.LENGTH_LONG)
                        .show();
            }
        });
        setupNavProfileInfo(firebaseUser);
    }

    public void setupNavProfileInfo(FirebaseUser firebaseUser) {
        TextView nav_header_emailAddress = (TextView) activity.navigationView.getHeaderView(0)
                .findViewById(R.id.nav_header_emailAddress);
        nav_header_emailAddress.setText((String) firebaseUser.getEmail());
        TextView nav_header_displayName = (TextView) activity.navigationView.getHeaderView(0)
                .findViewById(R.id.nav_header_displayName);
        nav_header_displayName.setText(firebaseUser.getProviderData().get(0).getDisplayName());

        ImageView profileImage = (ImageView) activity.navigationView.getHeaderView(0)
                .findViewById(R.id.nav_header_imageView);
        if (firebaseUser.getProviderData().get(0).getPhotoUrl() != null) {
            Uri uri = firebaseUser.getProviderData().get(0).getPhotoUrl();
            Picasso.with(this.activity).load(uri).into(profileImage);
        } else {
            profileImage.setImageDrawable(this.activity.getResources().getDrawable(android.R
                    .drawable.sym_def_app_icon));
        }

    }

}
