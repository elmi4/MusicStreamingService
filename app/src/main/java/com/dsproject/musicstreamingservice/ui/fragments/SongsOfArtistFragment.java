package com.dsproject.musicstreamingservice.ui.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.domain.Consumer;
import com.dsproject.musicstreamingservice.ui.MainActivity;
import com.dsproject.musicstreamingservice.ui.managers.connections.MyConnectionsManager;
import com.dsproject.musicstreamingservice.ui.managers.fragments.MyFragmentManager;
import com.dsproject.musicstreamingservice.ui.recyclerViewAdapters.SongsAdapter;
import com.dsproject.musicstreamingservice.ui.util.AsyncConsumerRequest;
import com.dsproject.musicstreamingservice.ui.util.RequestDetails;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.dsproject.musicstreamingservice.ui.util.UtilitiesUI.showToast;

public class SongsOfArtistFragment extends GenericFragment implements SongsAdapter.ItemClickListener
{
    private RecyclerView songsList;
    private String artistSelected;
    private static final String DOWNLOAD_REQUEST = "download";

    public SongsOfArtistFragment()
    {
        super(MyFragmentManager.getLayoutOf(SongsOfArtistFragment.class));
    }

    @Override
    public void onActivityCreated(Bundle savedInstance)
    {
        super.onActivityCreated(savedInstance);

        //Get the selected artist from the Artist fragment
        assert getArguments() != null;
        artistSelected = getArguments().getString("artistSelected");

        TextView label = view.findViewById(R.id.songsLabel);
        label.setText("Songs by " + artistSelected);

        songsList = view.findViewById(R.id.songsList);
        songsList.setLayoutManager(new LinearLayoutManager(context));

        createSongsList();
    }


    /**
     * Gets the available songs of the selected artist and creates
     * a clickable RecyclerView (list) containing them.
     */
    private void createSongsList()
    {
        AsyncTaskRunner taskRunner = new AsyncTaskRunner();
        SongsAdapter myAdapter;

        try {
            ArrayList<String> songsNames = taskRunner.execute().get();

            Collections.sort(songsNames);

            myAdapter = new SongsAdapter(context, songsNames);
            myAdapter.setClickListener(this);
            songsList.setAdapter(myAdapter);

            //Divider item to set the rows apart
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, LinearLayoutManager.VERTICAL);
            songsList.addItemDecoration(dividerItemDecoration);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void onItemClick(View view, int position, TextView songName, ImageView download)
    {
        String songSelected = songName.getText().toString();
        List<Byte> dataBuffer = null;
        Consumer.RequestType request_type = Consumer.RequestType.DOWNLOAD_FULL_SONG;

        if(songName.isPressed()){
            dataBuffer = new ArrayList<>(3000000);
            request_type = Consumer.RequestType.PLAY_CHUNKS;
        }
        PetrosPlayerFragment playerFragment = PetrosPlayerFragment.getInstance(dataBuffer);

        RequestDetails details = new RequestDetails(artistSelected, songSelected, request_type,
                dataBuffer, playerFragment);

        new AsyncConsumerRequest(getActivity()).execute(details);

        if(songName.isPressed()){
            Bundle data = new Bundle();
            ArrayList<String> title = new ArrayList<>();
            title.add(artistSelected);
            title.add(songSelected);
            data.putStringArrayList("songInfo",title);
            goToFragmentWithData(data, playerFragment);
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskRunner extends AsyncTask<Object, Void, ArrayList<String>>
    {
        @Override
        protected ArrayList<String> doInBackground(Object... objects) {

            Socket brokerConnection = MyConnectionsManager.getConnectionWithABroker(context);

            if(brokerConnection == null){
                showToast(Objects.requireNonNull(getActivity()), MyConnectionsManager.CANNOT_CONNECT_MSG);
                MainActivity.getNotificationManager().makeNoConnectionNotification();
                return null;
            }

            Consumer c1 = new Consumer(brokerConnection, context);

            c1.init();

           return c1.requestSongsOfArtist(artistSelected);
        }
    }

}