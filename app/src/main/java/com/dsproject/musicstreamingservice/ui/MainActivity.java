package com.dsproject.musicstreamingservice.ui;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.ui.fragments.ArtistsFragment;
import com.dsproject.musicstreamingservice.ui.fragments.CustomRequestFragment;
import com.dsproject.musicstreamingservice.ui.fragments.GenericFragment;
import com.dsproject.musicstreamingservice.ui.fragments.SettingsFragment;
import com.google.android.material.navigation.NavigationView;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GenericFragment.DataPassListener
{
    private DrawerLayout drawer;
    private NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
            //this is the way to change fragments
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ArtistsFragment()).commit();
            navView.setCheckedItem(R.id.nav_artists);
        }
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
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public <F extends Fragment> void changeMenuCheckedItem(final F frag)
    {
        Class fragClass = frag.getClass();
        if(fragClass == ArtistsFragment.class){
            navView.setCheckedItem(R.id.nav_artists);
        }else if(fragClass == CustomRequestFragment.class){
            navView.setCheckedItem(R.id.nav_custom_request);
        }else if(fragClass == SettingsFragment.class){
            navView.setCheckedItem(R.id.nav_settings);
        }else{
            MenuItem menuItem = navView.getCheckedItem();
            if(menuItem != null){
                menuItem.setChecked(false);
            }
        }
    }

    @Override
    public void passData(final Bundle data, final GenericFragment frag)
    {
        System.out.println(frag.getClass());
        frag.setArguments(data);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frag).commit();
    }
}