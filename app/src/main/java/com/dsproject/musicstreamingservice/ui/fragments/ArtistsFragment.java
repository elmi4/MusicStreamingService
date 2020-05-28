package com.dsproject.musicstreamingservice.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.domain.Consumer;
import com.dsproject.musicstreamingservice.domain.assist.network.ConnectionInfo;
import com.dsproject.musicstreamingservice.domain.media.ArtistName;
import com.dsproject.musicstreamingservice.ui.extras.MyRecyclerViewAdapter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/* To fix:
* Clickable rows -> to corresponding songs.
* Searchbar implementation
* Position of TextView artistsLabel
 */

public class ArtistsFragment extends Fragment implements MyRecyclerViewAdapter.ItemClickListener
{
    private Context fragContext;
    private View view;
    private RecyclerView artistsList;
    private SearchView searchBar;               //Make it usable


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.artists_fragment, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstance)
    {
        super.onActivityCreated(savedInstance);

        getActivityElements();

        artistsList = (RecyclerView) view.findViewById(R.id.artistsList);
        searchBar = (SearchView) view.findViewById(R.id.searchBar);               //Make it usable

        createArtistsList();
    }


    private void getActivityElements()
    {
        fragContext = getActivity().getApplicationContext();
        view = getView();
        if(view == null || fragContext == null){
            throw new IllegalStateException("Couldn't get view or context from fragment.");
        }
    }


    private void createArtistsList()
    {
        AsyncTaskRunner taskRunner = new AsyncTaskRunner();
        MyRecyclerViewAdapter myAdapter;

        try {
            Map<ArtistName, ConnectionInfo> artists = taskRunner.execute().get();
            ArrayList<String> artistsNames = new ArrayList<>();

            for (ArtistName name : artists.keySet()) {
                artistsNames.add(name.getArtistName());
            }

            artistsList.setLayoutManager(new LinearLayoutManager(fragContext));

            myAdapter = new MyRecyclerViewAdapter(fragContext, artistsNames);
            myAdapter.setClickListener(this);
            artistsList.setAdapter(myAdapter);

            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(fragContext, LinearLayoutManager.VERTICAL);
            artistsList.addItemDecoration(dividerItemDecoration);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void onItemClick(View view, int position) {
        assert getFragmentManager() != null;
        getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new SongsOfArtistFragment()).commit();
    }


    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskRunner extends AsyncTask<Object, Void, Map<ArtistName, ConnectionInfo>> {

        @Override
        protected Map<ArtistName, ConnectionInfo> doInBackground(Object... objects) {
            Consumer c1 = new Consumer(loadInitialBrokerCredentials(), fragContext);

            c1.init();

            return c1.requestState();
        }
    }

    private ConnectionInfo loadInitialBrokerCredentials(){
        ConnectionInfo connectionInfo = null;

        try (FileInputStream fis = fragContext.openFileInput("BrokerCredentials.txt")){
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
