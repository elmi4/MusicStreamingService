package com.dsproject.musicstreamingservice.ui;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.domain.Consumer;
import com.dsproject.musicstreamingservice.domain.assist.network.ConnectionInfo;
import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

        artist_input_field.getEditText().setText("Jason Shaw");
        song_input_field.getEditText().setText("Landra's Dream");

        switch1.setOnCheckedChangeListener((buttonView, isChecked) -> {
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

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskRunner extends AsyncTask<String, Void, Void>
    {
        @Override
        protected Void doInBackground(String... params) {
            ConnectionInfo brokerInfo = loadInitialBrokerCredentials();
            if(brokerInfo == null){
                runOnUiThread(() -> {
                    final Toast toast = Toast.makeText(CustomRequestActivity.this,
                            "Please fill in the broker connection info in the settings.", Toast.LENGTH_LONG);
                    toast.show();
                });
                return null;
            }

            Consumer c1 = new Consumer(brokerInfo, CustomRequestActivity.this);

            Consumer.RequestType type = (params[2].equals(PLAY_REQUEST)) ?
                    Consumer.RequestType.PLAY_CHUNKS : Consumer.RequestType.DOWNLOAD_FULL_SONG;

            c1.init();
            Log.d("DEBUG", "Got: "+c1.artistToBroker.size()+" artists in eventDelivery.");
            c1.requestSongData(params[0], params[1], type);

            Log.d("DEBUG", "Got: "+c1.requestSongsOfArtist("Jason Shaw").size()+" songs for this artist");

            return null;
        }
    }

    public ConnectionInfo loadInitialBrokerCredentials(){
        ConnectionInfo connectionInfo = null;

        try (FileInputStream fis = openFileInput("BrokerCredentials.txt")){
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String credentials;
            if((credentials = br.readLine())!=null){
                sb.append(credentials);
                credentials = sb.toString();
            }
            String ip = credentials.substring(0,credentials.indexOf('@'));
            int port = Integer.parseInt(credentials.substring(credentials.indexOf('@')+1));
            connectionInfo = new ConnectionInfo(ip,port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return connectionInfo;
    }

}
