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
 * Handles all log in / sign in / sign up operations. Use this class to get info and handle
 * operations on the authentication.
 */
public class LoginHandler {

    // TODO: 5/31/2016 Check possibility for LoginHandler to be a Static class.
    /**
     * Use this Tag as a request code to create a sign in activity provided by FirebaseUI.
     */
    private static final int RC_SIGN_IN = 100;
    // TODO: 5/31/2016 check if the Activity reference needs to be the MainActivity or it could be
    // any Activity.
    /**
     * Reference to the {@link MainActivity} which is calling the LoginHandler.
     */
    MainActivity activity;
    private FirebaseAuth mAuth;
    /**
     * Listener that gets notified when an authentication state change occurred.
     */
    private FirebaseAuth.AuthStateListener mAuthListener;

    /**
     * Constructor that throws an exception when a non MainActivity is trying to create an
     * instance of the LoginHandler. this constructor also starts the {@link FirebaseAuth
     * .AuthStateListener}
     *
     * @param activity
     */
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

    /**
     * @return true if the user is logged in and false otherwise.
     */
    public static boolean isLoggedIn() {
        // TODO: 5/25/2016 add expiration!
        return getFirebaseUser() != null /* && !isExpired(auth)*/;
    }

    /**
     * @return FirebaseUser, an instance of the current logged in user.
     */
    public static FirebaseUser getFirebaseUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    // TODO: 5/25/2016 fix isExpired
//    private static boolean isExpired(FirebaseAuth authData) {
//        return (System.currentTimeMillis() / 1000) >= authData.getExpires();
//    }

    /**
     * @return String containing the UID of the current authenticated user. if there is no user
     * logged in null will be returned.
     */
    public static String getAuthUid() {
        FirebaseUser user = getFirebaseUser();
        if (user != null)
            return user.getUid();
        return null;
    }

    /**
     * Call this method from the {@link Activity#onActivityResult(int, int, Intent)} method
     * in the activity that is using this {@link LoginHandler}, in order to get the user info
     * from the newly signed in user and to finish the sign in process.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
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
            switchToSignIn();
        } else {
//            this.activity.fab.show();
            switchToMainFragment(Constants.FIREBASE_URL + "/users/" + mAuth.getCurrentUser()
                    .getUid());
        }
    }

    /**
     * Use this method to start the FirebaseUI sign in activity.
     */
    public void switchToSignIn() {
        // TODO: 5/27/2016 solve Facebook authentication problem.
        this.activity.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setProviders(
                                AuthUI.EMAIL_PROVIDER,
                                AuthUI.GOOGLE_PROVIDER,
                                AuthUI.FACEBOOK_PROVIDER)
                        .build(), RC_SIGN_IN);
    }

    /**
     * Switch to the main fragment.
     *
     * @param repoUrl this specific user UID Firebase URL.
     */
    private void switchToMainFragment(String repoUrl) {
        if (isLoggedIn()) {
            this.activity.setTitle(this.activity.getResources().getString(R.string.app_name2));
            this.activity.setNavigationViewChecked(0);
            FragmentTransaction fragmentTransaction = this.activity.getFragmentManager()
                    .beginTransaction();
            Fragment mainFragment = MainFragment.newInstance(repoUrl);
            fragmentTransaction.replace(R.id.content_frame, mainFragment, "Main");
            fragmentTransaction.commit();
        }
    }

    /**
     * Use this method to sign out the user. When sign out is complete, the sign in activity will
     * launch.
     */
    public void signOut() {
        AuthUI.getInstance()
                .signOut(this.activity)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // user is now signed out
                        switchToSignIn();
                    }
                });
    }

    /**
     * This method adds the currently signed in users information to the Firebase database. The
     * user information will be added to the Firebase database only if the database is missing
     * this user information. If this users information is already on the database nothing will
     * be changed.
     * <p/>
     * Call this method when a new user signs in.
     */
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

    /**
     * This method sets the TextViews and the image in the header of the navigationView to adapt
     * to the {@link FirebaseUser firebaseUser}. Call this method after the user signs in and
     * when the app launches.
     *
     * @param firebaseUser
     */
    public void setupNavProfileInfo(FirebaseUser firebaseUser) {
        TextView nav_header_emailAddress = (TextView) activity.navigationView.getHeaderView(0)
                .findViewById(R.id.nav_header_emailAddress);
        nav_header_emailAddress.setText(firebaseUser.getEmail());
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
