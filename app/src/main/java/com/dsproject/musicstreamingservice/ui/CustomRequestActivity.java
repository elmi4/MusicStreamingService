package com.dsproject.musicstreamingservice.ui;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.domain.Consumer;
import com.dsproject.musicstreamingservice.domain.assist.Utilities;
import com.dsproject.musicstreamingservice.domain.assist.network.ConnectionInfo;
import com.dsproject.musicstreamingservice.ui.managers.setup.ApplicationSetup;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class CustomRequestActivity extends AppCompatActivity
{
    private static final String PLAY_REQUEST = "play";
    private static final String DOWNLOAD_REQUEST = "download";

    private TextInputLayout artist_input_field;
    private TextInputLayout song_input_field;
    private Button Req;
    private Switch switch1;
    private TextView tag;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_request);

        artist_input_field = (TextInputLayout) findViewById(R.id.artist_input_field);
        song_input_field = (TextInputLayout) findViewById(R.id.song_input_field);

        switch1 = (Switch) findViewById(R.id.switch1);
        switch1.setText("Play Now");

        Req = (Button) findViewById(R.id.Req);
    }


    @Override
    public void onStart()
    {
        super.onStart();

        switch1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            createDownloadProgressNotification();
            if(isChecked){
                switch1.setText("Download");
            }
            else {
                switch1.setText("Play Now");
            }
        });

        Req.setOnClickListener(view -> {
            String artist = Objects.requireNonNull(artist_input_field.getEditText()).getText().toString().trim();
            String song = Objects.requireNonNull(song_input_field.getEditText()).getText().toString().trim();

            boolean playNow = switch1.isChecked();
            String request_type = (playNow) ? DOWNLOAD_REQUEST : PLAY_REQUEST;

            Log.d("DEBUG", artist +" , "+ song);

            AsyncTaskRunner runner = new AsyncTaskRunner();
            runner.execute(artist, song, request_type);
        });
    }

    //notifications test
    private void createDownloadProgressNotification()
    {
        final int progressMax = 100;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(CustomRequestActivity.this);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                CustomRequestActivity.this, ApplicationSetup.CHANNEL_1_ID);

        notificationBuilder.setSmallIcon(R.drawable.ic_file_download_black_24dp)
                .setContentTitle("Download (test)")
                .setContentText("Download in progress")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(progressMax, 0, false);

        new Thread(() -> {
            SystemClock.sleep(2000);
            for (int progress = 0; progress <= progressMax; progress += 20) {
                notificationBuilder.setProgress(progressMax, progress, false);
                notificationManager.notify(1, notificationBuilder.build());
                SystemClock.sleep(1000);
            }
            notificationBuilder.setContentText("Download finished")
                    .setProgress(0, 0, false)
                    .setOngoing(false);
            notificationManager.notify(1, notificationBuilder.build());
        }).start();
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskRunner extends AsyncTask<String, Void, Void>
    {
        @Override
        protected Void doInBackground(String... params) {
            Consumer c1 = new Consumer(ConnectionInfo.of(Utilities.getMachineIP(), 4030), CustomRequestActivity.this);
            Consumer.RequestType type = (params[2].equals(PLAY_REQUEST)) ?
                    Consumer.RequestType.PLAY_CHUNKS : Consumer.RequestType.DOWNLOAD_FULL_SONG;

            c1.init();
            Log.d("DEBUG", "Got: "+c1.artistToBroker.size()+" artists in eventDelivery.");
            c1.requestSongData(params[0], params[1], type);

            Log.d("DEBUG", "Got: "+c1.requestSongsOfArtist("Jason Shaw").size()+" songs for this artist");

            return null;
        }
    }
}
