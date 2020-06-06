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

import static com.dsproject.musicstreamingservice.ui.util.UtilitiesUI.showToast;

// TODO: image instead of button,
//       layout,
//       make an adapter superclass,
//       check every file for imports/warnings/cleanup,
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


    public void onItemClick(View view, int position, TextView songName, Button download)
    {
        String songSelected = songName.getText().toString();

        if(songName.isPressed()) {
            List<Byte> dataBuffer = new ArrayList<>(3000000);
            PetrosPlayerFragment playerFragment = new PetrosPlayerFragment(dataBuffer);

            Bundle data = new Bundle();

            ArrayList<String> titleOfPlayer = new ArrayList<>();
            titleOfPlayer.add(artistSelected);
            titleOfPlayer.add(songSelected);

            data.putStringArrayList("songInfo", titleOfPlayer);
            goToFragmentWithData(data, playerFragment);
        }

        else if (download.isPressed()) {
            new AsyncTaskRunner2().execute(artistSelected, songSelected, DOWNLOAD_REQUEST);
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


    @SuppressLint("StaticFieldLeak")
    class AsyncTaskRunner2 extends AsyncTask<String, Void, Void>
    {
        @Override
        protected Void doInBackground(final String... params) {
            Socket brokerConnection = MyConnectionsManager.getConnectionWithABroker(context);
            if(brokerConnection == null){
                UtilitiesUI.showToast(Objects.requireNonNull(getActivity()), MyConnectionsManager.CANNOT_CONNECT_MSG);
                MainActivity.getNotificationManager().makeNoConnectionNotification();
                return null;
            }

            Consumer.RequestType type = Consumer.RequestType.DOWNLOAD_FULL_SONG;

            Consumer c1 = new Consumer(brokerConnection, context);
            c1.init();

            c1.requestSongData(params[0], params[1], type);

            return null;
        }
    }
}