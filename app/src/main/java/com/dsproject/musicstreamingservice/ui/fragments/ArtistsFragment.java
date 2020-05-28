package com.dsproject.musicstreamingservice.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.domain.Consumer;
import com.dsproject.musicstreamingservice.domain.assist.network.ConnectionInfo;
import com.dsproject.musicstreamingservice.domain.media.ArtistName;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.zip.Inflater;

public class ArtistsFragment extends Fragment
{
    private Context fragContext;
    private View view;
    private MyRecyclerViewAdapter.ItemClickListener clickListener;

    private RecyclerView recyclerView;
    private SearchView searchBar;               //Make it usable
    private ConstraintLayout artistsLayout;     //To delete if not needed


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

        recyclerView = (RecyclerView) view.findViewById(R.id.artistsList);
        searchBar = (SearchView) view.findViewById(R.id.searchBar);               //Make it usable
        artistsLayout = (ConstraintLayout) view.findViewById(R.id.Artists);       //To delete if not needed

        setListeners();
    }


    private void getActivityElements()
    {
        fragContext = getActivity().getApplicationContext();
        view = getView();
        if(view == null || fragContext == null){
            throw new IllegalStateException("Couldn't get view or context from fragment.");
        }
    }


    private void setListeners()
    {
        AsyncTaskRunner taskRunner = new AsyncTaskRunner();
        MyRecyclerViewAdapter myAdapter;

        try {
            Map<ArtistName, ConnectionInfo> artists = taskRunner.execute().get();
            ArrayList<String> artistsNames = new ArrayList<>();

            for (ArtistName name : artists.keySet()) {
                artistsNames.add(name.getArtistName());
            }

            recyclerView.setLayoutManager(new LinearLayoutManager(fragContext));

            myAdapter = new MyRecyclerViewAdapter(fragContext, artistsNames);
            myAdapter.setClickListener(clickListener);
            recyclerView.setAdapter(myAdapter);

            recyclerView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(v.getContext(), SongsOfArtistFragment.class);
                    startActivity(myIntent);
                }
            });

            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(fragContext, LinearLayoutManager.VERTICAL);
            recyclerView.addItemDecoration(dividerItemDecoration);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
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


    public static class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        private List<String> mData;
        private ItemClickListener mClickListener;
        private LayoutInflater mInflater;

        // data is passed into the constructor
        MyRecyclerViewAdapter(Context context, List<String> data) {
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
        }

        // inflates the row layout from xml when needed
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.row_of_artists_list, parent, false);
            return new ViewHolder(view);
        }

        // binds the data to the TextView in each row
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String artist = mData.get(position);
            holder.myTextView.setText(artist);
        }

        // total number of rows
        @Override
        public int getItemCount() {
            return mData.size();
        }

        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView myTextView;

            ViewHolder(View itemView) {
                super(itemView);
                myTextView = itemView.findViewById(R.id.ArtistName);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
            }
        }

        // allows clicks events to be caught
        void setClickListener(ItemClickListener itemClickListener) {
            this.mClickListener = itemClickListener;
        }

        // parent activity will implement this method to respond to click events
        public interface ItemClickListener {
            void onItemClick(View view, int position);
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
