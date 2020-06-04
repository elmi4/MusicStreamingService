package com.dsproject.musicstreamingservice.ui.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.domain.Consumer;
import com.dsproject.musicstreamingservice.domain.assist.network.ConnectionInfo;
import com.dsproject.musicstreamingservice.domain.media.ArtistName;
import com.dsproject.musicstreamingservice.ui.recyclerViewAdapters.ArtistsAdapter;
import com.dsproject.musicstreamingservice.ui.MainActivity;
import com.dsproject.musicstreamingservice.ui.UtilitiesUI;
import com.dsproject.musicstreamingservice.ui.adapters.CustomRVAdapter;
import com.dsproject.musicstreamingservice.ui.managers.connections.MyConnectionsManager;
import com.dsproject.musicstreamingservice.ui.managers.fragments.MyFragmentManager;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

// TODO: pass the artist onto the next fragment.
// Optional: Search bar, ripple effect on rows when clicked
// (https://stackoverflow.com/questions/30931889/adding-ripple-effect-to-recyclerview-item/49704439#49704439).

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

        artistsList = (RecyclerView) view.findViewById(R.id.artistsList);
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

           // artistsList.setLayoutManager(new LinearLayoutManager(context));

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


    public void onItemClick(View view, int position)
    {
        Bundle bundle = new Bundle();
        bundle.putString("selectedArtist", "Kalomira");
        //bundle.putString("selectedArtist", selectedArtist);

        SongsOfArtistFragment soaf = new SongsOfArtistFragment();
        soaf.setArguments(bundle);

        assert getFragmentManager() != null;
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, soaf).commit();
    }


    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskRunner extends AsyncTask<Object, Void, Map<ArtistName, ConnectionInfo>>
    {
        @Override
        protected Map<ArtistName, ConnectionInfo> doInBackground(Object... objects) {
            Socket brokerConnection = MyConnectionsManager.getConnectionWithABroker(context);
            if(brokerConnection == null){
                UtilitiesUI.showToast(getActivity(), MyConnectionsManager.CANNOT_CONNECT_MSG);
                MainActivity.getNotificationManager().makeNoConnectionNotification();
                return null;
            }

            Consumer c1 = new Consumer(brokerConnection, context);
            c1.init();

            return c1.getArtistToBroker();
        }
    }

}

