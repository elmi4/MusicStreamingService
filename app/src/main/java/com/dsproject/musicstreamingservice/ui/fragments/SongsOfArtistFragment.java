package com.dsproject.musicstreamingservice.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.domain.Consumer;
import com.dsproject.musicstreamingservice.domain.assist.network.ConnectionInfo;
import com.dsproject.musicstreamingservice.domain.media.ArtistName;
import com.dsproject.musicstreamingservice.ui.adapters.CustomRVAdapter;
import com.dsproject.musicstreamingservice.ui.managers.fragments.MyFragmentManager;
import com.dsproject.musicstreamingservice.ui.fragments.GenericFragment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/*To do:
  - Pass an artist from the previous fragment
  - Create RecyclerView of songs
  - When a song is clicked -> Download class for specific song.
*/

public class SongsOfArtistFragment extends GenericFragment implements CustomRVAdapter.ItemClickListener
{
    private Context fragContext;
    private View view;
    private RecyclerView songsList;
    private ArtistName artist;


    public SongsOfArtistFragment()
    {
        super(MyFragmentManager.getLayoutOf(SongsOfArtistFragment.class));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.songs_of_artist_fragment, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstance)
    {
        super.onActivityCreated(savedInstance);

        getActivityElements();

        songsList = (RecyclerView) view.findViewById(R.id.songsList);

        createSongsList();
    }


    private void getActivityElements()
    {
        fragContext = getActivity().getApplicationContext();
        view = getView();
        if(view == null || fragContext == null){
            throw new IllegalStateException("Couldn't get view or context from fragment.");
        }
    }


    /**
     * Gets the available songs of the artist selected and creates
     * a clickable RecyclerView (list) containing them.
     */
    private void createSongsList()
    {
        AsyncTaskRunner taskRunner = new AsyncTaskRunner();
        CustomRVAdapter myAdapter;

        try {
            List<String> songs1 = taskRunner.execute().get();
            ArrayList<String> songs2 = new ArrayList<>();


            //Move every element from songs in songsofartist!!!!!!!!!!!!!!!!!!!


            songsList.setLayoutManager(new LinearLayoutManager(fragContext));

            myAdapter = new CustomRVAdapter(fragContext, songs2);
            myAdapter.setClickListener(this);
            songsList.setAdapter(myAdapter);

            //Divider item to set the rows apart
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(fragContext, LinearLayoutManager.VERTICAL);
            songsList.addItemDecoration(dividerItemDecoration);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void onItemClick(View view, int position) {
        assert getFragmentManager() != null;
        getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new CustomRequestFragment()).commit();
    }


    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskRunner extends AsyncTask<Object, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Object... objects) {
            Consumer c1 = new Consumer(loadInitialBrokerCredentials(), fragContext);

            c1.init();

            return c1.requestSongsOfArtist(artist.getArtistName());
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
