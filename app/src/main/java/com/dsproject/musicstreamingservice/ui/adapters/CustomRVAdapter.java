package com.dsproject.musicstreamingservice.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.dsproject.musicstreamingservice.R;

import java.util.List;

/**
 * Adapter needed to populate the views in each row of the RecyclerView.
 */
public class CustomRVAdapter extends RecyclerView.Adapter<CustomRVAdapter.ViewHolder>
{
    private List<String> mData;
    private ItemClickListener mClickListener;
    private LayoutInflater mInflater;


    public CustomRVAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }


    /**
     * Inflates (builds) the row layout from its corresponding xml file.
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_of_artists_list, parent, false);
        return new ViewHolder(view);
    }


    /**
     * Binds the strings to their respective TextViews in each row.
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String artist = mData.get(position);
        holder.myTextView.setText(artist);
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


    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }


    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
