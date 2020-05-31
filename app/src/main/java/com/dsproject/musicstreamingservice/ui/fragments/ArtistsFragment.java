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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;


// To add:
// Ripple effect on rows when clicked (https://stackoverflow.com/questions/30931889/adding-ripple-effect-to-recyclerview-item/49704439#49704439),
// Search bar.

public class ArtistsFragment extends GenericFragment implements CustomRVAdapter.ItemClickListener
{
    private Context fragContext;
    private View view;
    private RecyclerView artistsList;


    public ArtistsFragment()
    {
        super(MyFragmentManager.getLayoutOf(ArtistsFragment.class));
    }


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

        createArtistsList();

//        //---------------------------------------Petros' example code-----------------------------------------------------------
//        // (Didn't know whether I could erase this or not)
//
//        Button change = view.findViewById(R.id.test_changeFragBtn);
//        change.setOnClickListener(v -> goToFragment(new CustomRequestFragment()));
//
//        Button edit = view.findViewById(R.id.test_editTextBtn);
//        EditText txtArea = view.findViewById(R.id.test_textField);
//
//        edit.setOnClickListener(v -> {
//            //Create container of data (can send many data with different types too")
//            //Here we are taking the input of the editText and passing it as argument with id songName
//            Bundle bundle = new Bundle();
//            bundle.putString("songName", txtArea.getText().toString().trim());
//
//            goToFragmentWithData(bundle, new CustomRequestFragment());
//        });
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
     * Gets the available artists from a Publisher and creates
     * a clickable RecyclerView (list) containing them.
     */
    private void createArtistsList()
    {
        AsyncTaskRunner taskRunner = new AsyncTaskRunner();
        CustomRVAdapter myAdapter;

        try {
            Map<ArtistName, ConnectionInfo> artists = taskRunner.execute().get();
            ArrayList<String> artistsNames = new ArrayList<>();

            for (ArtistName name : artists.keySet()) {
                artistsNames.add(name.getArtistName());
            }

            artistsList.setLayoutManager(new LinearLayoutManager(fragContext));

            myAdapter = new CustomRVAdapter(fragContext, artistsNames);
            myAdapter.setClickListener(this);
            artistsList.setAdapter(myAdapter);

            //Divider item to set the rows apart
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