package com.dsproject.musicstreamingservice.ui.recyclerViewAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dsproject.musicstreamingservice.R;

import java.util.List;

/**
 * Adapter to populate each row with its respective song name.
 */
public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder>
{
    private List<String> mData;
    private ItemClickListener mClickListener;
    private LayoutInflater mInflater;


    public SongsAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    /**
     * Inflates (builds) the row layout from its corresponding xml file.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_of_songs_list, parent, false);
        return new ViewHolder(view);
    }


    /**
     * Binds the string to its respective TextView in each row.
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        String song = mData.get(position);
        holder.songName.setText(song);
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }


    /**
     * Enables scrolling on the RecyclerView.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private TextView songName;
        private ImageView download;

        ViewHolder(View itemView) {
            super(itemView);

            songName = itemView.findViewById(R.id.songNameTV);
            download = itemView.findViewById(R.id.downloadIcon);

            songName.setOnClickListener(this);
            download.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition(), songName, download);
        }
    }


    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }


    public interface ItemClickListener {
        void onItemClick(View view, int position, TextView textView, ImageView image);
    }

}

