package com.dsproject.musicstreamingservice.ui;

import androidx.annotation.NonNull;
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

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.ui.managers.notifications.MyNotificationManager;
import com.dsproject.musicstreamingservice.ui.managers.notifications.Notifier;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Button customReq = (Button) findViewById(R.id.customReq);
        customReq.setOnClickListener(view -> {
            //go to Activity2
            Intent myIntent = new Intent(view.getContext(), CustomRequestActivity.class);
            startActivity(myIntent);
        });

        Button btn = (Button)findViewById(R.id.notificationBtn);
        btn.setOnClickListener(v -> sendNotificationOnChannel1());


    }

    public void sendNotificationOnChannel1()
    {
        Notifier myNotificationManager = new MyNotificationManager(MainActivity.this);

        myNotificationManager.makeAndShowPlainNotification(
                "Aggressive Notification",
                "FUCK YOU, I WILL VIBRATE ALL I WANT",
                R.drawable.ic_file_download_black_24dp); //notification icon or null for a default

        //play sound and GET TROLLED BITCH vibrate 15 times
        myNotificationManager.playNotificationSound(R.raw.notification_sound);
        myNotificationManager.vibrateRepeating(500, 500, 15);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_screen_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            //go to Activity Connect
            case R.id.ItemConnect:
                Intent connectionIntent = new Intent(this, ConnectActivity.class);
                this.startActivity(connectionIntent);
                return true;
            case R.id.ItemCredits:
                //go to Activity Credits
                Intent creditsIntent = new Intent(this, CreditsActivity.class);
                this.startActivity(creditsIntent);
                return true;
            default: return super.onOptionsItemSelected(item);
        }

    }
}