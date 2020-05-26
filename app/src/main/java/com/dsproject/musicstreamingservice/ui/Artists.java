package com.dsproject.musicstreamingservice.ui;

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
import androidx.appcompat.app.AppCompatActivity;
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

public class Artists extends AppCompatActivity {

    listFragment myFragment = new listFragment();
    myFragment.

    class listFragment extends Fragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            RecyclerView recyclerView = findViewById(R.id.artistsList);
            SearchView searchBar = findViewById(R.id.searchBar);            //!!!
            LinearLayout artistsLayout = findViewById(R.id.Artists);

            AsyncTaskRunner taskRunner = new AsyncTaskRunner();
            MyRecyclerViewAdapter myAdapter;

            try {
                Map<ArtistName, ConnectionInfo> artists = taskRunner.execute().get();
                ArrayList<String> artistsNames = new ArrayList<>();

                for (ArtistName name : artists.keySet()) {
                    artistsNames.add(name.getArtistName());
                }

                recyclerView.setLayoutManager(new LinearLayoutManager(Artists.this));           // test if actually needed
                myAdapter = new MyRecyclerViewAdapter(Artists.this, artistsNames);
                myAdapter.setClickListener((MyRecyclerViewAdapter.ItemClickListener) Artists.this);
                recyclerView.setAdapter(myAdapter);

                recyclerView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent myIntent = new Intent(v.getContext(), SongsOfArtist.class);
                        startActivity(myIntent);
                    }
                });

              //To incorporate if needed.
//            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(Artists.this, LinearLayoutManager.VERTICAL);
//            recyclerView.addItemDecoration(dividerItemDecoration);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.activity_artists, container, false);
        }

    }


    private class AsyncTaskRunner extends AsyncTask<Object, Void, Map<ArtistName, ConnectionInfo>> {

        @Override
        protected Map<ArtistName, ConnectionInfo> doInBackground(Object... objects) {
            Consumer c1 = new Consumer(loadInitialBrokerCredentials(), Artists.this);

            c1.init();
            Map<ArtistName, ConnectionInfo> artists = c1.requestState();

            return artists;
        }
    }


    public static class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        private List<String> mData;
        private LayoutInflater mInflater;
        private ItemClickListener mClickListener;

        // data is passed into the constructor
        MyRecyclerViewAdapter(Context context, List<String> data) {
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
        }

        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
        // inflates the row layout from xml when needed
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null; //mInflater.inflate(R.layout.recyclerview_row, parent, false);
            return new ViewHolder(view);
        }

        // binds the data to the TextView in each row
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String animal = mData.get(position);
            holder.myTextView.setText(animal);
        }

        // total number of rows
        @Override
        public int getItemCount() {
            return mData.size();
        }

        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView myTextView;

            ViewHolder(View itemView) {
                super(itemView);
                //myTextView = itemView.findViewById(R.id.tvAnimalName);
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


    public ConnectionInfo loadInitialBrokerCredentials() {
        ConnectionInfo connectionInfo = null;

        FileInputStream fis = null;

        try {
            fis = openFileInput("BrokerCredentials.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String credentials;
            if ((credentials = br.readLine()) != null) {
                sb.append(credentials);
                credentials = sb.toString();
            }
            String ip = credentials.substring(0, credentials.indexOf('@'));
            int port = Integer.parseInt(credentials.substring(credentials.indexOf('@') + 1));
            connectionInfo = new ConnectionInfo(ip, port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return connectionInfo;
    }

}
