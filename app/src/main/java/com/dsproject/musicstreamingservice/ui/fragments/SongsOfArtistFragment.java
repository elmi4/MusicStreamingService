package com.dsproject.musicstreamingservice.ui.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.domain.Consumer;
import com.dsproject.musicstreamingservice.domain.assist.network.ConnectionInfo;
import com.dsproject.musicstreamingservice.domain.media.ArtistName;
import com.dsproject.musicstreamingservice.ui.recyclerViewAdapters.ArtistsAdapter;
import com.dsproject.musicstreamingservice.ui.managers.fragments.MyFragmentManager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// TODO: Test, add download button.
// Optional: implement a search bar.

public class SongsOfArtistFragment extends GenericFragment implements ArtistsAdapter.ItemClickListener
{
    private RecyclerView songsList;
    private String artistSelected;

    public SongsOfArtistFragment()
    {
        super(MyFragmentManager.getLayoutOf(SongsOfArtistFragment.class));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_songs_of_artist, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstance)
    {
        super.onActivityCreated(savedInstance);

        assert getArguments() != null;
        artistSelected = getArguments().getString("selectedArtist");

        TextView label = (TextView) view.findViewById(R.id.songsLabel);
        label.setText("Songs by " + artistSelected + ":");

        songsList = (RecyclerView) view.findViewById(R.id.songsList);

        createSongsList();
    }


    /**
     * Gets the available songs of the artist selected and creates
     * a clickable RecyclerView (list) containing them.
     */
    private void createSongsList()
    {
        AsyncTaskRunner taskRunner = new AsyncTaskRunner();
        ArtistsAdapter myAdapter;

        try {
            List<String> helper = taskRunner.execute().get();
            ArrayList<String> finalSongList = new ArrayList<>();
            finalSongList.addAll(helper);

            songsList.setLayoutManager(new LinearLayoutManager(context));

            myAdapter = new ArtistsAdapter(context, finalSongList);
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
        assert getFragmentManager() != null;
        getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new PlayerFragment()).commit();
    }


    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskRunner extends AsyncTask<Object, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Object... objects) {
            Consumer c1 = new Consumer(loadInitialBrokerCredentials(), context);

            c1.init();

            return c1.requestSongsOfArtist(artistSelected);
        }
    }


    private ConnectionInfo loadInitialBrokerCredentials(){
        ConnectionInfo connectionInfo = null;

        try (FileInputStream fis = context.openFileInput("BrokerCredentials.txt")){
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


//public class SongsOfArtistFragment extends GenericFragment {
//
//    private TextView label;
//
//    public SongsOfArtistFragment() {
//        super(MyFragmentManager.getLayoutOf(SongsOfArtistFragment.class));
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstance) {
//        super.onActivityCreated(savedInstance);
//        String artist = getArguments().getString("selectedArtist");
//
//        label = (TextView) view.findViewById(R.id.songsLabel);
//        label.setText("Songs by " + artist);
//    }
//
//}