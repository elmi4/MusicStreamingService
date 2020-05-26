package com.dsproject.musicstreamingservice.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.ui.fragments.ArtistsFragment;
import com.dsproject.musicstreamingservice.ui.fragments.CustomRequestFragment;
import com.dsproject.musicstreamingservice.ui.fragments.SettingsFragment;
import com.dsproject.musicstreamingservice.ui.managers.notifications.MyNotificationManager;
import com.dsproject.musicstreamingservice.ui.managers.notifications.Notifier;
import com.google.android.material.navigation.NavigationView;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
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
        Button artists = (Button) findViewById(R.id.artistsBtn);
        artists.setOnClickListener(view -> {
            Intent goToArtists = new Intent(view.getContext(), Artists.class);
            startActivity(goToArtists);
        });

    }

    public void sendNotificationOnChannel1()
    {
        Notifier myNotificationManager = new MyNotificationManager(MainActivity.this);

        myNotificationManager.makeAndShowPlainNotification(
                "Aggressive Notification",
                "FUCK YOU, I WILL VIBRATE ALL I WANT",
                R.drawable.ic_file_download_black_24dp, //notification icon or null for a default
                null);

        //play sound and GET TROLLED BITCH vibrate 15 times
        myNotificationManager.playNotificationSound(R.raw.notification_sound);
        myNotificationManager.vibrateRepeating(500, 500, 15);
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
}

//public class MainActivity extends AppCompatActivity
//{
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//
//        Button customReq = (Button) findViewById(R.id.customReq);
//        customReq.setOnClickListener(view -> {
//            //go to Activity2
//            Intent myIntent = new Intent(view.getContext(), CustomRequestActivity.class);
//            startActivity(myIntent);
//        });
//
//        Button btn = (Button)findViewById(R.id.notificationBtn);
//        btn.setOnClickListener(v -> sendNotificationOnChannel1());
//
//    }
//
//    public void sendNotificationOnChannel1()
//    {
//        Notifier myNotificationManager = new MyNotificationManager(MainActivity.this);
//
//        myNotificationManager.makeAndShowPlainNotification(
//                "Aggressive Notification",
//                "FUCK YOU, I WILL VIBRATE ALL I WANT",
//                R.drawable.ic_file_download_black_24dp, //notification icon or null for a default
//                null);
//
//        //play sound and GET TROLLED BITCH vibrate 15 times
//        myNotificationManager.playNotificationSound(R.raw.notification_sound);
//        myNotificationManager.vibrateRepeating(500, 500, 15);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_screen_menu,menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        switch (item.getItemId()){
//            //go to Activity Connect
//            case R.id.ItemConnect:
//                Intent connectionIntent = new Intent(this, ConnectActivity.class);
//                this.startActivity(connectionIntent);
//                return true;
//            case R.id.ItemCredits:
//                //go to Activity Credits
//                Intent creditsIntent = new Intent(this, CreditsActivity.class);
//                this.startActivity(creditsIntent);
//                return true;
//            default: return super.onOptionsItemSelected(item);
//        }
//
//    }
//}