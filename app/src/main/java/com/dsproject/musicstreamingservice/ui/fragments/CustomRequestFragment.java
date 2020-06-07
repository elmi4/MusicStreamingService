package com.dsproject.musicstreamingservice.ui.fragments;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.domain.Consumer;
import com.dsproject.musicstreamingservice.ui.managers.fragments.MyFragmentManager;
import com.dsproject.musicstreamingservice.ui.util.AsyncConsumerRequest;
import com.dsproject.musicstreamingservice.ui.util.RequestDetails;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomRequestFragment extends GenericFragment
{
    private static final String PLAY_REQUEST = "play";
    private static final String DOWNLOAD_REQUEST = "download";

    private TextInputLayout artist_input_field;
    private TextInputLayout song_input_field;
    private Button Req;
    private Switch switch1;

    private PlayerFragment playerFragment;
    private List<Byte> dataBuffer;


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

            boolean playNow = !switch1.isChecked();
            Consumer.RequestType request_type = (playNow) ? Consumer.RequestType.PLAY_CHUNKS
                                                          : Consumer.RequestType.DOWNLOAD_FULL_SONG;

            if(playNow){
                dataBuffer = new ArrayList<>(3000000);
            }
            playerFragment = PlayerFragment.getInstance(dataBuffer);

            RequestDetails details = new RequestDetails(artist, song, request_type, dataBuffer, playerFragment);

            new AsyncConsumerRequest(getActivity()).execute(details);

            if(playNow){
                Bundle data = new Bundle();
                ArrayList<String> title = new ArrayList<>();
                title.add(artist);
                title.add(song);
                data.putStringArrayList("songInfo",title);
                goToFragmentWithData(data, playerFragment);
            }

        });
    }

    private void handleMessages()
    {
        Bundle args = getArguments();
        if(args != null){
            song_input_field.getEditText().setText(args.getString("songName"));
        }
    }
}
