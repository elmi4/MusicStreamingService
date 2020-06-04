package com.dsproject.musicstreamingservice.ui.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.domain.Consumer;
import com.dsproject.musicstreamingservice.domain.assist.network.ConnectionInfo;
import com.dsproject.musicstreamingservice.ui.MainActivity;
import com.dsproject.musicstreamingservice.ui.UtilitiesUI;
import com.dsproject.musicstreamingservice.ui.managers.connections.MyConnectionsManager;
import com.dsproject.musicstreamingservice.ui.recyclerViewAdapters.ArtistsAdapter;
import com.dsproject.musicstreamingservice.ui.managers.fragments.MyFragmentManager;
import com.dsproject.musicstreamingservice.ui.recyclerViewAdapters.SongsAdapter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// TODO: display songs in alphabetical order,
//       add download button.
public class SongsOfArtistFragment extends GenericFragment implements SongsAdapter.ItemClickListener
{
    private RecyclerView songsList;
    private String artistSelected;

    public SongsOfArtistFragment()
    {
        super(MyFragmentManager.getLayoutOf(SongsOfArtistFragment.class));
    }


    @Override
    public void onActivityCreated(Bundle savedInstance)
    {
        super.onActivityCreated(savedInstance);

        assert getArguments() != null;
        artistSelected = getArguments().getString("artistSelected");

        TextView label = (TextView) view.findViewById(R.id.songsLabel);
        label.setText("Songs by " + artistSelected);

        songsList = (RecyclerView) view.findViewById(R.id.songsList);
        songsList.setLayoutManager(new LinearLayoutManager(context));

        createSongsList();
    }


    /**
     * Gets the available songs of the artist selected and creates
     * a clickable RecyclerView (list) containing them.
     */
    private void createSongsList()
    {
        AsyncTaskRunner taskRunner = new AsyncTaskRunner();
        SongsAdapter myAdapter;

        try {
            ArrayList<String> songsNames = taskRunner.execute().get();

            //sort songsNames

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


    public void onItemClick(View view, int position) {
//        assert getFragmentManager() != null;
//        getFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                new PlayerFragment()).commit();
    }


    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskRunner extends AsyncTask<Object, Void, ArrayList<String>>
    {
        @Override
        protected ArrayList<String> doInBackground(Object... objects) {

            Socket brokerConnection = MyConnectionsManager.getConnectionWithABroker(context);

            if(brokerConnection == null){
                UtilitiesUI.showToast(getActivity(), MyConnectionsManager.CANNOT_CONNECT_MSG);
                MainActivity.getNotificationManager().makeNoConnectionNotification();
                return null;
            }

            Consumer c1 = new Consumer(brokerConnection, context);

            c1.init();

           return c1.requestSongsOfArtist(artistSelected);
        }
    }
}