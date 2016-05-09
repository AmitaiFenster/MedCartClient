package com.amitai.medcart.medcartclient;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;

// TODO: 5/5/2016 If possible, have a diffrent class implement OnLoginListener.
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, UnlockFragment
        .OnFragmentInteractionListener /*, LoginFragment.OnLoginListener */ {

    public FloatingActionButton fab;
    public GoogleApiClient mGoogleApiClient;
    View viewSwitchLayout;
    NavigationView navigationView;
    LoginHandler login;
    private BluetoothAdapter mBluetoothAdapter;
    //TODO: remove if not needed. If using nfc foreground detection or opening already running
    // activities and adding the nfc intent to a list, than use this:
//    private PendingIntent mPendingIntent;
    private Switch toolbarSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null)
            Firebase.setAndroidContext(this);

        setContentView(R.layout.mainactivity_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string
                .navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        components();

    }

    @Override
    protected void onResume() {
        super.onResume();
//        Intent intent = getIntent();

        //TODO: remove if not needed. If using nfc foreground detection or opening already
        // running activities and adding the nfc intent to a list, than use this:
//        if (mAdapter != null && mAdapter.isEnabled()) {
//            mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
//        }

//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

    }

    /**
     * Initializing other views and components. this method is usually called by the OnCreate
     * method.
     */
    private void components() {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        login = new LoginHandler(this);
        login.tryAccessMainFragment();

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .enableAutoManage(this, login)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem menuSwitchItem = menu.findItem(R.id.toolbarSwitch);
        viewSwitchLayout = MenuItemCompat.getActionView(menuSwitchItem);
        toolbarSwitch = (Switch) viewSwitchLayout.findViewById(R.id.switchForActionBar);
        toolbarSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !enableAllComponents()) {
                    toolbarSwitch.setChecked(false);
                }
            }
        });

        return true;        //Indicating that we want to display this menu item now.
    }

    /**
     * Enabling all components, including: NFC, Bluetooth, Location.
     *
     * @return true if all components are enabled, and false otherwise.
     */
    public boolean enableAllComponents() {
        return enableNFC() && enableBluetooth() && enableLocation();
    }

    /**
     * this method checks if nfc is turned on (enabled). if not, the nfc wireless settings dialog
     * will be opened so that the user can manually turn on nfc.
     *
     * @return True if nfc is Enabled. and false otherwise.
     */
    private boolean enableNFC() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(MainActivity.this);
        if (nfcAdapter == null) {
            Toast.makeText(MainActivity.this, R.string.no_nfc, Toast.LENGTH_LONG)
                    .show();
            toolbarSwitch.setChecked(false);
        } else if (!nfcAdapter.isEnabled()) {
            showWirelessSettingsDialog();
        } else {
//            Toast.makeText(MainActivity.this, "NFC available", Toast.LENGTH_LONG)
//                    .show();
            return true;
        }
        return false;
    }

    /**
     * this method checks if location is turned on (enabled). if not, an activity will be started
     * to prompt the user to turn on location.
     *
     * @return true if location is enabled, and false otherwise.
     */
    private boolean enableLocation() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                .ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    Constants.MY_PERMISSIONS_REQUEST_LOCATION);
            Toast.makeText(MainActivity.this, "Please enable permission", Toast
                    .LENGTH_LONG).show();
            return false;
        } else {
            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                MainActivity.this.startActivityForResult(enableLocationIntent,
                        Constants.REQUEST_ENABLE_LOCATION);
                return false;
            }
        }
        return true;
    }

    /**
     * Method to turn on Bluetooth.
     *
     * @return true if Bluetooth is enabled, and false otherwise.
     */
    public boolean enableBluetooth() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.toolbarSwitch) {
            return true;
        } else if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_logout) {
            login.logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_unlock) {
            login.tryAccessMainFragment();
        } else if (id == R.id.login) {
            login.switchToLoginFragment();
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_test_connection) {

        } else if (id == R.id.nav_enroll_cart) {
//            replaceFragment(EnrollCartFragment.newInstance());
//            setTitle("Enroll Cart");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Opens the Wireless Settings Dialog so that the user could turn on the NFC.
     */
    private void showWirelessSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.nfc_disabled);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                toolbarSwitch.setChecked(false);
            }
        });
        builder.create().show();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        login.activityResult(requestCode, resultCode, data);
    }

    // TODO: 5/5/2016 Remove if not needed.
//    /**
//     * Replacing current fragment with newFragment. Used for example when navigation drawer item
//     * is selected.
//     *
//     * @param newFragment
//     */
//    public void replaceFragment(Fragment newFragment) {
//        fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.replace(R.id.content_frame, newFragment);
//        fragmentTransaction.commit();
//    }
}
