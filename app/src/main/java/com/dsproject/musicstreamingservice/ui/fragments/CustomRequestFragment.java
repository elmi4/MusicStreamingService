package com.dsproject.musicstreamingservice.ui.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.domain.Consumer;
import com.dsproject.musicstreamingservice.domain.assist.network.ConnectionInfo;
import com.dsproject.musicstreamingservice.ui.managers.fragments.MyFragmentManager;
import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class CustomRequestFragment extends GenericFragment
{
    private static final String PLAY_REQUEST = "play";
    private static final String DOWNLOAD_REQUEST = "download";

    private TextInputLayout artist_input_field;
    private TextInputLayout song_input_field;
    private Button Req;
    private Switch switch1;


    public CustomRequestFragment()
    {
        super(MyFragmentManager.getLayoutOf(CustomRequestFragment.class));
    }

    //Do initializations here, because if the activity view hasn't been created yet, they will fail.
    @Override
    public void onActivityCreated(Bundle savedInstance)
    {
        super.onActivityCreated(savedInstance);

        artist_input_field = (TextInputLayout) view.findViewById(R.id.artist_input_field);
        song_input_field = (TextInputLayout) view.findViewById(R.id.song_input_field);

        switch1 = (Switch) view.findViewById(R.id.switch1);
        switch1.setText("Play Now");

        Req = (Button) view.findViewById(R.id.Req);

        setListeners();

        //for easy testing
        artist_input_field.getEditText().setText("Jason Shaw");
        song_input_field.getEditText().setText("Landra's Dream");

        handleMessages();
    }

    private void setListeners()
    {
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

    private void handleMessages()
    {
        Bundle args = getArguments();
        if(args != null){
            song_input_field.getEditText().setText(args.getString("songName"));
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskRunner extends AsyncTask<String, Void, Void>
    {
        @Override
        protected Void doInBackground(String... params) {
            ConnectionInfo brokerInfo = loadInitialBrokerCredentials();
            if(brokerInfo == null){
                getActivity().runOnUiThread(() -> {
                    final Toast toast = Toast.makeText(context,
                            "Please fill in the broker connection info in the settings.", Toast.LENGTH_LONG);
                    toast.show();
                });
                return null;
            }

            Consumer c1 = new Consumer(brokerInfo, context);

            Consumer.RequestType type = (params[2].equals(PLAY_REQUEST)) ?
                    Consumer.RequestType.PLAY_CHUNKS : Consumer.RequestType.DOWNLOAD_FULL_SONG;

            c1.init();
            Log.d("DEBUG", "Got: "+c1.artistToBroker.size()+" artists in eventDelivery.");
            c1.requestSongData(params[0], params[1], type);

            Log.d("DEBUG", "Got: "+c1.requestSongsOfArtist("Jason Shaw").size()+" songs for this artist");

            return null;
        }
    }

    public ConnectionInfo loadInitialBrokerCredentials()
    {
        ConnectionInfo connectionInfo = null;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                context.openFileInput("BrokerCredentials.txt"))))
        {
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
