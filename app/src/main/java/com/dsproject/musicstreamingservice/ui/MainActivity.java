package com.dsproject.musicstreamingservice.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.ui.fragments.ArtistsFragment;
import com.dsproject.musicstreamingservice.ui.fragments.CreditsFragment;
import com.dsproject.musicstreamingservice.ui.fragments.CustomRequestFragment;
import com.dsproject.musicstreamingservice.ui.fragments.GenericFragment;
import com.dsproject.musicstreamingservice.ui.fragments.InstructionsFragment;
import com.dsproject.musicstreamingservice.ui.fragments.SettingsFragment;
import com.dsproject.musicstreamingservice.ui.managers.fragments.MyFragmentManager;
import com.dsproject.musicstreamingservice.ui.managers.notifications.MyNotificationManager;
import com.dsproject.musicstreamingservice.ui.managers.notifications.Notifier;
import com.google.android.material.navigation.NavigationView;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GenericFragment.DataPassListener
{
    public static final String REDIRECT_TAG = "destinationFragment";

    private DrawerLayout drawer;
    private NavigationView navView;
    private static Notifier notifManager;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        notifManager = new MyNotificationManager(this);

        drawer = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //savedInstanceState isn't null when an activity gets destroyed and recreated.
        //This could happen when you go from portrait to landscape mode,
        //so we don't want this code to run and replace our current fragment.
        if(savedInstanceState == null) {
            getDestinationFragment(getIntent()).commit();
        }
    }


    /**
     * Called instead of onCreate only when the activity is explicitly referenced from another fragment
     * or activity with an Intent.
     * @param intent The intent created in the calling class.
     */
    @Override
    protected void onNewIntent(final Intent intent)
    {
        super.onNewIntent(intent);
        getDestinationFragment(intent).commit();
    }


    /**
     * Called when an item of the sidebar is selected. Goes to the selected fragment and sets the
     * pressed item as selected.
     * @param item Selected sidebar item
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.nav_artists:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ArtistsFragment()).commit();
                break;

            case R.id.nav_custom_request:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new CustomRequestFragment()).commit();
                break;

            case R.id.nav_settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new SettingsFragment()).commit();
                break;

            case R.id.nav_credits:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new CreditsFragment()).commit();
                break;

            case R.id.nav_instructions:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new InstructionsFragment()).commit();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onBackPressed()
    {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    /**
     * Implementation method of GenericFragment.DataPassListener used from a fragment to communicate
     * with an activity in order to switch to another fragment and pass data to it.
     * @param data Bundle passed from the calling fragment.
     * @param frag Fragment to go to.
     */
    @Override
    public void passData(final Bundle data, final GenericFragment frag)
    {
        frag.setArguments(data);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frag).commit();
    }


    /**
     * Method to be used by any fragment of the activity that wants to create notifications.
     * Use this method to get an instance of a Notifier, don't create one inside fragment.
     * @return The instance of the class' Notifier, like MyNotificationManager.
     */
    public static Notifier getNotificationManager()
    {
        return notifManager;
    }


    /**
     * Changes the selected sidebar item.
     * @param frag The fragment that the sidebar item refers to.
     */
    public <F extends GenericFragment> void changeMenuCheckedItem(final F frag)
    {
        Class fragClass = frag.getClass();
        if(fragClass == ArtistsFragment.class){
            navView.setCheckedItem(R.id.nav_artists);
        }else if(fragClass == CustomRequestFragment.class){
            navView.setCheckedItem(R.id.nav_custom_request);
        }else if(fragClass == SettingsFragment.class){
            navView.setCheckedItem(R.id.nav_settings);
        }else if(fragClass == CreditsFragment.class){
            navView.setCheckedItem(R.id.nav_credits);
        }else if(fragClass == InstructionsFragment.class){
            navView.setCheckedItem(R.id.nav_instructions);
        }else{
            MenuItem menuItem = navView.getCheckedItem();
            if(menuItem != null){
                menuItem.setChecked(false);
            }
        }
    }


    /**
     * Checks whether the Intent of the activity contains a message that specifies what fragment
     * should be opened when the activity gets created, and if not, open the default one.
     * @return A FragmentTransaction object containing the correct destination Fragment.
     */
    private FragmentTransaction getDestinationFragment(final Intent intent)
    {
        //check for msg from another fragment/activity
        String fragName = intent.getStringExtra(REDIRECT_TAG);

        if(fragName == null){
            navView.setCheckedItem(R.id.nav_artists);
            return getSupportFragmentManager().beginTransaction().
                    replace(R.id.fragment_container, new ArtistsFragment()); //default starting fragment
        }else{
            GenericFragment frag = MyFragmentManager.getFragmentByName(fragName);
            changeMenuCheckedItem(frag);
            return getSupportFragmentManager().beginTransaction().
                    replace(R.id.fragment_container, frag); //custom fragment to open
        }
    }
}