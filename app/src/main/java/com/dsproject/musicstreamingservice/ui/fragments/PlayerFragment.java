package com.dsproject.musicstreamingservice.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import com.dsproject.musicstreamingservice.ui.managers.fragments.MyFragmentManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PlayerFragment extends GenericFragment {

    private TextView title;
    private ImageView songImage;
    private ImageButton pausePlayButton;
    private ProgressBar songProgressBar;

    private MediaPlayer musicPlayer = new MediaPlayer();
    private ArrayList<String> songArray = new ArrayList<>();
    private boolean isPlaying = false;
    private int songArrayIndex = 0;

    private int progress = 0;


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
            String artist = songInfo.get(0);
            String song = songInfo.get(1);

            title.setText(song + " by " + artist);

            pausePlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(!isPlaying) {
                        isPlaying = true;
                        playSong(artist, song);

                    }else {
                        if(musicPlayer.isPlaying())musicPlayer.pause();
                        else musicPlayer.start();
                    }



                }
            });


            musicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer player) {
                    musicPlayer.reset();

                    if (songArrayIndex < songArray.size()) {

                        musicPlayer.setOnCompletionListener(this);
                        try {

                            System.out.println("Playing Chunk : " + (songArrayIndex+1));
                            musicPlayer.setDataSource(getActivity() ,Uri.fromFile(new File(songArray.get(songArrayIndex))));
                            musicPlayer.prepare();
                            musicPlayer.start();
                            songArrayIndex++;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }else songArrayIndex = 0;
                }
            });








        }

        return v;
    }


   private void playSong(String artist , String song){

       int chunkCounter = 1;

       String songPath = getActivity().getExternalFilesDir(null).getAbsolutePath()+ "/" + artist + "___" + song + "/" + chunkCounter + "_" + song + "__" + artist + ".mp3";

       File songFile = new File(songPath);


       while(songFile.exists()){

           songArray.add(songPath);

           System.out.println("Found chunk at : " + songPath);

           chunkCounter++;
           songPath = getActivity().getExternalFilesDir(null).getAbsolutePath() + "/" + artist + "___" + song + "/" + chunkCounter + "_" + song + "__" + artist + ".mp3";
           songFile = new File(songPath);
       }

       if(!songArray.isEmpty()){

           try {

               System.out.println("Playing Chunk : " + 1);
               musicPlayer.setDataSource(getActivity(), Uri.fromFile(new File(songArray.get(0))));
               musicPlayer.prepare();
               musicPlayer.start();
               songArrayIndex++;
           } catch (IOException e) {
               e.printStackTrace();
           }
       }else System.out.println("No chunks found");

   }


}
