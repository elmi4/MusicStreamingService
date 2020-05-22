package com.dsproject.musicstreamingservice.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

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

}