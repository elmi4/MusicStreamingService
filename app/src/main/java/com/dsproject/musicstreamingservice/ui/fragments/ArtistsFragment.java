package com.dsproject.musicstreamingservice.ui.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.domain.Consumer;
import com.dsproject.musicstreamingservice.domain.assist.network.ConnectionInfo;
import com.dsproject.musicstreamingservice.domain.media.ArtistName;
import com.dsproject.musicstreamingservice.ui.MainActivity;
import com.dsproject.musicstreamingservice.ui.managers.connections.MyConnectionsManager;
import com.dsproject.musicstreamingservice.ui.managers.fragments.MyFragmentManager;
import com.dsproject.musicstreamingservice.ui.recyclerViewAdapters.ArtistsAdapter;
import com.dsproject.musicstreamingservice.ui.util.UtilitiesUI;

import java.net.Socket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ArtistsFragment extends GenericFragment implements ArtistsAdapter.ItemClickListener
{
    private RecyclerView artistsList;

    public ArtistsFragment()
    {
        super(MyFragmentManager.getLayoutOf(ArtistsFragment.class));
    }

    @Override
    public void onActivityCreated(Bundle savedInstance)
    {
        super.onActivityCreated(savedInstance);

        artistsList = view.findViewById(R.id.artistsList);
        artistsList.setLayoutManager(new LinearLayoutManager(context));
        createArtistsList();
    }


    /**
     * Gets the available artists from a Publisher and creates
     * a clickable RecyclerView (list) containing them.
     */
    private void createArtistsList()
    {
        AsyncTaskRunner taskRunner = new AsyncTaskRunner();
        ArtistsAdapter myAdapter;

        try {
            Map<ArtistName, ConnectionInfo> artists = taskRunner.execute().get();
            if(artists == null){
                return;
            }

            ArrayList<String> artistsNames = new ArrayList<>();

            for (ArtistName name : artists.keySet()) {
                artistsNames.add(name.getArtistName());
            }

            Collections.sort(artistsNames);

            myAdapter = new ArtistsAdapter(context, artistsNames);
            myAdapter.setClickListener(this);
            artistsList.setAdapter(myAdapter);

            //Divider item to set the rows apart
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, LinearLayoutManager.VERTICAL);
            artistsList.addItemDecoration(dividerItemDecoration);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void onItemClick(View view, int position, TextView artistName)
    {
       String artistSelected = artistName.getText().toString();

        Bundle dataToSend = new Bundle();
        dataToSend.putString("artistSelected", artistSelected);

        SongsOfArtistFragment songs = new SongsOfArtistFragment();
        goToFragmentWithData(dataToSend, songs);
    }


    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskRunner extends AsyncTask<Object, Void, Map<ArtistName, ConnectionInfo>>
    {
        @Override
        protected Map<ArtistName, ConnectionInfo> doInBackground(Object... objects) {
            Socket brokerConnection = MyConnectionsManager.getConnectionWithABroker(context);
            if(brokerConnection == null){
                UtilitiesUI.showToast(Objects.requireNonNull(getActivity()), MyConnectionsManager.CANNOT_CONNECT_MSG);
                MainActivity.getNotificationManager().makeNoConnectionNotification();
                return null;
            }

            Consumer c1 = new Consumer(brokerConnection, context);
            c1.init();

            return c1.getArtistToBroker();
        }
    }
}