package com.amitai.medcart.medcartclient;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// TODO: 5/5/2016 If possible, have a different class implement OnLoginListener.
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MainFragment
        .OnFragmentInteractionListener {

    //    public FloatingActionButton fab;
    View viewSwitchLayout;
    NavigationView navigationView;
    LoginHandler login;
    private BluetoothAdapter mBluetoothAdapter;
    private Switch toolbarSwitch;
    private MenuItem mPreviousMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.mainactivity_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string
                .navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
//        navigationView.getMenu().setGroupCheckable();

        components();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Initializing other views and components. this method is usually called by the OnCreate
     * method.
     */
    private void components() {

        login = new LoginHandler(this);
        login.tryAccessMainFragment();

        if (LoginHandler.isLoggedIn()) {
            login.setupNavProfileInfo(LoginHandler.getFirebaseUser());
        }
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
        if (EnablingComponents.isComponentsEnabled(this, mBluetoothAdapter))
            toolbarSwitch.setChecked(true);
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
        boolean bool = EnablingComponents.enableAllComponents(MainActivity.this, mBluetoothAdapter);
        if (!bool) {
            toolbarSwitch.setChecked(false);
        }
        return bool;
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
            login.signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main) {
            login.tryAccessMainFragment();
        } else if (id == R.id.login) {
            login.switchToSignIn();
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_enroll_cart) {
            // TODO: 5/22/2016  fix setNavigationViewChecked, when back button is pressed Enroll
            // Cart item is still selected on the menu.

            DatabaseReference fb = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants
                    .FIREBASE_URL + "users/" + LoginHandler.getAuthUid() + "/level");
            fb.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue(String.class).equals("admin")) {

                        //setTitle and setNavigationViewChecked in the case of using a fragment
                        // for enroll cart, but I decided to use a new activity, so the user doesn't
                        // have access to the navigationView while in the EnrollCartActivity.
//                        setTitle("Enroll Cart");
//                        setNavigationViewChecked(item);

                        Intent myIntent = new Intent(MainActivity.this, EnrollCartActivity.class);
                        startActivity(myIntent);
                    } else {
                        Toast.makeText(MainActivity.this, "Please login as admin", Toast
                                .LENGTH_SHORT).show();
//                        setNavigationViewChecked(Constants.currentFragment);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this, "Please login as admin", Toast
                            .LENGTH_SHORT).show();
                    setNavigationViewChecked(mPreviousMenuItem);
                }
            });

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        login.activityResult(requestCode, resultCode, data);
    }

    // TODO: 5/5/2016 Remove if not needed.

    /**
     * Replacing current fragment with newFragment. Used for example when navigation drawer item
     * is selected.
     *
     * @param newFragment
     */
    public void replaceFragment(Fragment newFragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, newFragment);
        fragmentTransaction.commit();
    }

    /**
     * @param item item number of the navigation item wished to be checked.
     */
    public void setNavigationViewChecked(int item) {
        MenuItem menuItem = navigationView.getMenu().getItem(item);
        setNavigationViewChecked(menuItem);
    }


    /**
     * @param menuItem the navigation item wished to be checked.
     */
    public void setNavigationViewChecked(MenuItem menuItem) {
        menuItem.setCheckable(true);
        menuItem.setChecked(true);
        if (mPreviousMenuItem != null && mPreviousMenuItem != menuItem) {
            mPreviousMenuItem.setChecked(false);
        }
        mPreviousMenuItem = menuItem;
        Constants.currentFragment = menuItem.getItemId();
    }
}
