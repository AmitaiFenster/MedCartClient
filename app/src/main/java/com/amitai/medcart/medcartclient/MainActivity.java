package com.amitai.medcart.medcartclient;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, UnlockFragment.OnFragmentInteractionListener {


    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    View viewSwitchLayout;

    //TODO: remove if not needed. If using nfc foreground detection or opening already running activities and adding the nfc intent to a list, than use this:
//    private PendingIntent mPendingIntent;
    private Switch toolbarSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainactivity_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        components();

        //        //Setting up default fragment.
        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        Fragment unlockFragment = UnlockFragment.newInstance();
        fragmentTransaction.add(R.id.content_frame, unlockFragment);
        fragmentTransaction.commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
//        Intent intent = getIntent();

        //TODO: remove if not needed. If using nfc foreground detection or opening already running activities and adding the nfc intent to a list, than use this:
//        if (mAdapter != null && mAdapter.isEnabled()) {
//            mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
//        }

//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

    }

    /**
     * Initializing other views and components. this method is usually called by the OnCreate method.
     */
    private void components() {

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
                if (isChecked) {
                    NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(MainActivity.this);
                    if (nfcAdapter == null) {
                        Toast.makeText(MainActivity.this, R.string.no_nfc, Toast.LENGTH_LONG).show();
                        toolbarSwitch.setChecked(false);
                    } else if (!nfcAdapter.isEnabled()) {
                        showWirelessSettingsDialog();
                    } else {
                        Toast.makeText(MainActivity.this, "NFC available", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        return true;        //Indicating that we want to display this menu item now.
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
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_unlock) {
            replaceFragment(UnlockFragment.newInstance());
        } else if (id == R.id.nav_sign_in_eiris) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_test_connection) {

        } else if (id == R.id.nav_enroll_cart) {
            replaceFragment(EnrollCartFragment.newInstance());
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

    public void replaceFragment(Fragment newFragment) {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, newFragment);
        fragmentTransaction.commit();
    }
}
