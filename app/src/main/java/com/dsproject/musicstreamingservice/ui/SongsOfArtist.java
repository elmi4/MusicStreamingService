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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.domain.Consumer;
import com.dsproject.musicstreamingservice.domain.assist.network.ConnectionInfo;
import com.dsproject.musicstreamingservice.domain.media.ArtistName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class SongsOfArtist extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RecyclerView recyclerView = findViewById(R.id.artistsList);
        SearchView searchBar = findViewById(R.id.searchBar);            //!!!

        SongsOfArtist.AsyncTaskRunner taskRunner = new SongsOfArtist.AsyncTaskRunner();

        try {
            Map<ArtistName, ConnectionInfo> artists = taskRunner.execute().get();
            ArrayList<String> artistsNames = new ArrayList<>();

            for (ArtistName name : artists.keySet()) {
                artistsNames.add(name.getArtistName());
            }

            SongsOfArtist.MyRecyclerViewAdapter myAdapter;
            recyclerView.setLayoutManager(new LinearLayoutManager(this));           // test if actually needed
            myAdapter = new SongsOfArtist.MyRecyclerViewAdapter(this, artistsNames);
            myAdapter.setClickListener((MyRecyclerViewAdapter.ItemClickListener) SongsOfArtist.this);
            recyclerView.setAdapter(myAdapter);

            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
            recyclerView.addItemDecoration(dividerItemDecoration);

            recyclerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(v.getContext(), SongsOfArtist.class);
                    startActivity(myIntent);
                }
            });
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        private List<String> mData;
        private LayoutInflater mInflater;
        private SongsOfArtist.MyRecyclerViewAdapter.ItemClickListener mClickListener;

        // data is passed into the constructor
        MyRecyclerViewAdapter(Context context, List<String> data) {
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
        }

        //!!!!!!!!!!!!!!!!!!!!!
        // inflates the row layout from xml when needed
        @Override
        public SongsOfArtist.MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null; //mInflater.inflate(R.layout.recyclerview_row, parent, false);
            return new SongsOfArtist.MyRecyclerViewAdapter.ViewHolder(view);
        }

        // binds the data to the TextView in each row
        @Override
        public void onBindViewHolder(MyRecyclerViewAdapter.ViewHolder holder, int position) {
            String animal = mData.get(position);
            holder.myTextView.setText(animal);
        }

        // total number of rows
        @Override
        public int getItemCount() {
            return mData.size();
        }

        //!!!!!!!!!!!!!!!!!!!!!!!
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
        void setClickListener(MyRecyclerViewAdapter.ItemClickListener itemClickListener) {
            this.mClickListener = itemClickListener;
        }

        // parent activity will implement this method to respond to click events
        public interface ItemClickListener {
            void onItemClick(View view, int position);
        }
    }


    private class AsyncTaskRunner extends AsyncTask<Object, Void, Map<ArtistName, ConnectionInfo>> {

        LinearLayout artistsLayout = findViewById(R.id.Artists);

        @Override
        protected Map<ArtistName, ConnectionInfo> doInBackground(Object... objects) {
            Consumer c1 = new Consumer (ConnectionInfo.of("192.168.1.92",  430), SongsOfArtist.this);

            c1.init();
            Map<ArtistName, ConnectionInfo> artists = c1.requestState();

            return artists;
        }
    }
}

/*To do:
  - Find all songs of specific artist
  - System.out.println for each song
  - When one is clicked -> Download class.
*/