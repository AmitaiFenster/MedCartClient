package com.amitai.medcart.medcartclient;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by amita on 5/5/2016.
 */
public class LoginHandler implements LoginFragment.OnLoginListener, GoogleApiClient
        .OnConnectionFailedListener {

    MainActivity activity;
    private GoogleSignInOptions gso;
    private GoogleApiClient mGoogleApiClient;

    public LoginHandler(Activity activity) {
        try {
            this.activity = (MainActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement MainActivity");
        }
        this.mGoogleApiClient = this.activity.mGoogleApiClient;
    }

    @Override
    public void onLogin(String email, String password) {
        Firebase firebase = new Firebase(Constants.FIREBASE_URL);
        firebase.authWithPassword(email, password, new MyAuthResultHandler());
    }

    @Override
    public void onGoogleLogin() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        activity.startActivityForResult(intent, Constants.REQUEST_CODE_GOOGLE_LOGIN);
    }

    public void activityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_GOOGLE_LOGIN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                String emailAddress = account.getEmail();
                getGoogleOAuthToken(emailAddress);

            }
        }
    }

    private void getGoogleOAuthToken(final String emailAddress) {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            String errorMessage = null;

            @Override
            protected String doInBackground(Void... params) {
                String token = null;
                try {
                    String scope = "oauth2:profile email";
                    token = GoogleAuthUtil.getToken(activity, emailAddress, scope);
                } catch (IOException transientEx) {
                /* Network or server error */
                    errorMessage = "Network error: " + transientEx.getMessage();
                } catch (UserRecoverableAuthException e) {
                /* We probably need to ask for permissions, so start the intent if there is none
                pending */
                    Intent recover = e.getIntent();
                    activity.startActivityForResult(recover, Constants.REQUEST_CODE_GOOGLE_LOGIN);
                } catch (GoogleAuthException authEx) {
                    errorMessage = "Error authenticating with Google: " + authEx.getMessage();
                }
                return token;
            }

            @Override
            protected void onPostExecute(String token) {
                Log.d(Constants.TAG_LoginHandler, "onPostExecute, token: " + token);
                if (token != null) {
                    onGoogleLoginWithToken(token);
                } else {
                    showLoginError(errorMessage);
                }
            }
        };
        task.execute();
    }

    private void showLoginError(String errorMessage) {

    }

    private void onGoogleLoginWithToken(String oAuthToken) {
        Firebase firebase = new Firebase(Constants.FIREBASE_URL);
        firebase.authWithOAuthToken("google", oAuthToken, new MyAuthResultHandler());
    }


    /**
     * This method tries to switch to main fragment if signed in. If not, the LoginFragment will
     * be shown.
     */
    public void tryAccessMainFragment() {
        Firebase firebase = new Firebase(Constants.FIREBASE_URL);
        AuthData auth = firebase.getAuth();
        if (auth == null || isExpired(auth)) {
            this.activity.fab.hide();
            Toast.makeText(this.activity, "Please login!", Toast.LENGTH_LONG).show();
            switchToLoginFragment();
        } else {
            this.activity.fab.show();
            switchToMainFragment(Constants.FIREBASE_URL + "/users/" + auth.getUid());
        }
    }


    public boolean isExpired(AuthData authData) {
        return (System.currentTimeMillis() / 1000) >= authData.getExpires();
    }

    public void switchToLoginFragment() {
        FragmentTransaction fragmentTransaction = this.activity.getFragmentManager()
                .beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new LoginFragment(), "Login");
        fragmentTransaction.commit();
        this.activity.navigationView.getMenu().getItem(1).setChecked(true);
    }

    private void switchToMainFragment(String repoUrl) {
        this.activity.setTitle(this.activity.getResources().getString(R.string.app_name2));
        this.activity.navigationView.getMenu().getItem(0).setChecked(true);
        FragmentTransaction fragmentTransaction = this.activity.getFragmentManager()
                .beginTransaction();
        Fragment unlockFragment = UnlockFragment.newInstance(repoUrl);
        fragmentTransaction.replace(R.id.content_frame, unlockFragment, "Main");
        fragmentTransaction.commit();
    }

    public void logout() {
        Firebase firebase = new Firebase(Constants.FIREBASE_URL);
        firebase.unauth();
        switchToLoginFragment();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(Constants.TAG_MainActivity, "onConnectionFaild: " + connectionResult
                .getErrorMessage());
    }


    class MyAuthResultHandler implements Firebase.AuthResultHandler {

        @Override
        public void onAuthenticated(AuthData authData) {

            final Firebase ref = new Firebase(Constants.FIREBASE_URL + "/users/");
            final AuthData authDataFinal = authData;

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.hasChild(authDataFinal.getUid())) {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("email", (String) authDataFinal.getProviderData().get("email"));
                        map.put("provider", authDataFinal.getProvider());
                        map.put("level", "user");
                        if (authDataFinal.getProviderData().containsKey("displayName")) {
                            map.put("displayName", authDataFinal.getProviderData().get
                                    ("displayName")
                                    .toString());
                        }
                        ref.child(authDataFinal.getUid()).setValue(map);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });

            switchToMainFragment(Constants.FIREBASE_URL + "/users/" + authData.getUid());
//            tryAccessMainFragment();
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            Log.i(Constants.TAG_LoginHandler, "onAuthenticationError: " + firebaseError
                    .getMessage());
            switchToLoginFragment();
            Toast.makeText(activity, "Email or password incorrect", Toast.LENGTH_LONG).show();

        }
    }


}
