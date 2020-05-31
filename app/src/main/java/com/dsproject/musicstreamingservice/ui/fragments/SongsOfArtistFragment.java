package com.dsproject.musicstreamingservice.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dsproject.musicstreamingservice.R;

// A simplistic implementation made in order for the Artists' fragment to work. Still under implementation.
public class SongsOfArtistFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.songs_of_artist_fragment, container, false);
    }

}
