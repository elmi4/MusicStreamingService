package com.dsproject.musicstreamingservice.ui.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.dsproject.musicstreamingservice.ui.util.UtilitiesUI;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

// TODO: download button (see CustomRequest),
public class SongsOfArtistFragment extends GenericFragment implements SongsAdapter.ItemClickListener
{
    private RecyclerView songsList;
    private String artistSelected;
    private Button download;

    private PetrosPlayerFragment playerFragment;
    private List<Byte> dataBuffer;


    public SongsOfArtistFragment()
    {
        super(MyFragmentManager.getLayoutOf(SongsOfArtistFragment.class));
    }


    @Override
    public void onActivityCreated(Bundle savedInstance)
    {
        super.onActivityCreated(savedInstance);

        //download = (Button) view.findViewById(R.id.downloadBtn);

        //Get the selected artist from the Artist fragment
        assert getArguments() != null;
        artistSelected = getArguments().getString("artistSelected");

        TextView label = (TextView) view.findViewById(R.id.songsLabel);
        label.setText("Songs by " + artistSelected);

        songsList = (RecyclerView) view.findViewById(R.id.songsList);
        songsList.setLayoutManager(new LinearLayoutManager(context));

        setListener();

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


    private void setListener()
    {
        download.setOnClickListener(view -> {

    });

    }


    public void onItemClick(View view, int position, TextView songName)
    {
        String songSelected = songName.getText().toString();

        dataBuffer = new ArrayList<>(3000000);
        playerFragment = new PetrosPlayerFragment(dataBuffer);

        Bundle data = new Bundle();

        ArrayList<String> titleOfPlayer = new ArrayList<>();
        titleOfPlayer.add(artistSelected);
        titleOfPlayer.add(songSelected);

        data.putStringArrayList("songInfo", titleOfPlayer);
        goToFragmentWithData(data, playerFragment);
    }


    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskRunner extends AsyncTask<Object, Void, ArrayList<String>>
    {
        @Override
        protected ArrayList<String> doInBackground(Object... objects) {

            Socket brokerConnection = MyConnectionsManager.getConnectionWithABroker(context);

            if(brokerConnection == null){
                UtilitiesUI.showToast(Objects.requireNonNull(getActivity()), MyConnectionsManager.CANNOT_CONNECT_MSG);
                MainActivity.getNotificationManager().makeNoConnectionNotification();
                return null;
            }

            Consumer c1 = new Consumer(brokerConnection, context);

            c1.init();

           return c1.requestSongsOfArtist(artistSelected);
        }
    }
}