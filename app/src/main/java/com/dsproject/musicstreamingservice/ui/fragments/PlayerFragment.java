package com.dsproject.musicstreamingservice.ui.fragments;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.domain.assist.io.IOHandler;
import com.dsproject.musicstreamingservice.ui.managers.fragments.MyFragmentManager;

import java.io.File;
import java.util.ArrayList;

public class PlayerFragment extends GenericFragment
{
    private TextView title;
    private ImageView songImage;
    private ImageButton pausePlayButton;
    private ProgressBar songProgressBar;
    private String artist, song;
    private int progress = 0;

    private static MediaPlayer musicPlayer;


    public PlayerFragment() {
        super(MyFragmentManager.getLayoutOf(PlayerFragment.class));
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_player,container,false);

        title = v.findViewById(R.id.playerSongTitle);
        songImage = v.findViewById(R.id.playerImageSong);
        pausePlayButton = v.findViewById(R.id.playerPlayPauseButton);
        songProgressBar = v.findViewById(R.id.playerProgressBar);

        Bundle bundle = getArguments();
        if(bundle!= null){
            ArrayList<String> songInfo = bundle.getStringArrayList("songInfo");
            artist = songInfo.get(0);
            song = songInfo.get(1);

            title.setText(song + " by " + artist);

            String dataFolder = artist + "___" + song + "/";
            String fileName = song + "__" + artist + ".mp3";
            File songFile = new File(getActivity().getExternalFilesDir(null),dataFolder+fileName);

            musicPlayer = MediaPlayer.create(getActivity(), Uri.fromFile(songFile));
            if(musicPlayer == null){
                title.setText("Error playing file, please try again.");
                return v;
            }
            musicPlayer.setOnCompletionListener(MediaPlayer::release);
            musicPlayer.start();

            pausePlayButton.setOnClickListener(v1 -> {
                if (musicPlayer.isPlaying()) {
                    musicPlayer.pause();
                    pausePlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_circle_outline_black_24dp));
                } else {
                    musicPlayer.start();
                    pausePlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_circle));
                }
            });

        }
        return v;
    }


    @Override
    public void onActivityCreated(Bundle savedInstance)
    {
        super.onActivityCreated(savedInstance);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        super.onDetach(); //isn't this supposed to be called after onDestroy by itself?
        System.out.println("FRAGMENT ONDESTROY");
        IOHandler.deleteFromStorage(getActivity(),artist,song,false);

        if(musicPlayer != null){
            musicPlayer.reset();
            musicPlayer.release();
        }
    }

}
