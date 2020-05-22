package com.dsproject.musicstreamingservice.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.ui.managers.setup.ApplicationSetup;

public class MainActivity extends AppCompatActivity
{
    private NotificationManagerCompat notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationManager = NotificationManagerCompat.from(this);

        Button customReq = (Button) findViewById(R.id.customReq);
        customReq.setOnClickListener(view -> {
            //go to Activity2
            Log.i("DEBUG", "here");
            Intent myIntent = new Intent(view.getContext(), CustomRequestActivity.class);
            startActivity(myIntent);
        });

        Button btn = (Button)findViewById(R.id.notificationBtn);
        btn.setOnClickListener(v -> sendNotificationOnChannel1());

    }

    public void sendNotificationOnChannel1()
    {
         NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(MainActivity.this, ApplicationSetup.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_file_download_black_24dp)
                .setContentTitle("Aggressive notification")
                .setContentText("FUCK YOU")
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setPriority(ApplicationSetup.CHANNEL_1_PRIORITY)
                ;

        notificationManager.notify(1, notificationBuilder.build());
    }

}