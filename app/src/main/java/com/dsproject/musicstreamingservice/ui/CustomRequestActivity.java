package com.dsproject.musicstreamingservice.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.domain.Consumer;
import com.dsproject.musicstreamingservice.domain.assist.Utilities;
import com.dsproject.musicstreamingservice.domain.assist.network.ConnectionInfo;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class CustomRequestActivity extends AppCompatActivity {
    private TextInputLayout artist_input_field;
    private TextInputLayout song_input_field;
    private Button Req;
    private Switch switch1;
    private TextView tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_request);

        artist_input_field = (TextInputLayout) findViewById(R.id.artist_input_field);
        song_input_field = (TextInputLayout) findViewById(R.id.song_input_field);

        switch1 = (Switch) findViewById(R.id.switch1);
        switch1.setText("Play Now");

        Req = (Button) findViewById(R.id.Req);
    }

    public void onStart() {
        super.onStart();

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    switch1.setText("Download");
                }
                else {
                    switch1.setText("Play Now");
                }
            }
        });

        Req.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("DEBUG", "here2");

                String artist = Objects.requireNonNull(artist_input_field.getEditText()).getText().toString();
                String song = Objects.requireNonNull(song_input_field.getEditText()).getText().toString();
                Boolean playNow = switch1.isChecked();

                String request_type = (playNow) ? "play" : "download";

                Log.d("DEBUG", artist);
                Log.d("DEBUG", song);

                AsyncTaskRunner runner = new AsyncTaskRunner();
                runner.execute(artist, song, request_type);


            }
        });
    }
    private class AsyncTaskRunner extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.d("DEBUG", "doInBackground");

            Consumer c1 = new Consumer(ConnectionInfo.of("168.192.2.7", 4030), CustomRequestActivity.this);

            Consumer.RequestType type = (params[2].equals("play")) ? Consumer.RequestType.PLAY_CHUNKS : Consumer.RequestType.DOWNLOAD_FULL_SONG;
            c1.init();
            c1.requestSongData(params[0], params[1], type);

            return null;
        }
    }
}
