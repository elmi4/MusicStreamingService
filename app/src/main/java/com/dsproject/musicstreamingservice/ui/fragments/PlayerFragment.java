package com.dsproject.musicstreamingservice.ui.fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.domain.Consumer;
import com.dsproject.musicstreamingservice.domain.assist.io.IOHandler;
import com.dsproject.musicstreamingservice.ui.MainActivity;
import com.dsproject.musicstreamingservice.ui.managers.connections.MyConnectionsManager;
import com.dsproject.musicstreamingservice.ui.managers.fragments.MyFragmentManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class PlayerFragment extends GenericFragment
{
    private TextView title;
    private ImageView songImage;
    private ImageButton pausePlayButton;
    private ProgressBar songProgressBar;
    private String artist, song;
    private int progress = 0;
    private File songFile;
    private boolean stop = false;
    private int timeoutIntervals = 40;          //In milliseconds
    private int songProgressBuffer = 40;        //time before the end of the song that the player start buffering also in ms

    private boolean stoppedByAsyncRunner = true;        //booleans for checking which process stopped the Media Player
    private boolean stoppedByButton = false;

    MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();

    private static MediaPlayer musicPlayer = new MediaPlayer();


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
            songFile = new File(getActivity().getExternalFilesDir(null),dataFolder+fileName);

            pausePlayButton.setOnClickListener(v1 -> {
                musicPlayer.pause();
                if (musicPlayer.isPlaying()) {
                    stoppedByButton = true;
                    musicPlayer.pause();
                    pausePlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_circle_outline_black_24dp));
                }
                if(!musicPlayer.isPlaying() && !stoppedByAsyncRunner && musicPlayer != null){
                    stoppedByButton = false;
                    musicPlayer.start();
                    pausePlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_circle));
                }
            });




            PlayerFragment.AsyncTaskRunner runner = new PlayerFragment.AsyncTaskRunner();
            runner.execute();




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
       stop = true;

       System.out.println("FRAGMENT ONDESTROY");
        IOHandler.deleteFromStorage(getActivity(),artist,song,false);

        if(musicPlayer != null){
            musicPlayer.reset();
            musicPlayer.release();
        }
    }

   class AsyncTaskRunner extends AsyncTask<String, Void, Void>
    {

        @Override
        protected Void doInBackground(String... params) {
            System.out.println("IN A BACKGROUD THREAD");
            long expectedFileSize = 0;

            String dataFolder = artist + "___" + song + "/";
            File songParentFolder = new File(context.getExternalFilesDir(null), dataFolder);
            File infoFile = new File(songParentFolder,"tmp.txt");


            while(true){
                if(songFile.length() < IOHandler.STANDARD_CHUNK_SIZE){      //Check if at least one chunk has arrived
                    System.out.println("PUTTING THREAD TO SLEEP");
                    SystemClock.sleep(timeoutIntervals);        //If not sleep
                    continue;
                }else {
                    try(Scanner sc = new Scanner(infoFile)){

                        if(sc.hasNextLine()) {
                            int chunkNum = Integer.parseInt(sc.nextLine());
                            System.out.println("Total chunks : " +chunkNum);
                            expectedFileSize = chunkNum*IOHandler.STANDARD_CHUNK_SIZE;
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }



                    musicPlayer.start();
                    stoppedByAsyncRunner = false;
                    break;
                }

            }

            while(!stop){

                    if(Integer.parseInt(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) < musicPlayer.getCurrentPosition() - songProgressBuffer &&
                    songFile.length() > expectedFileSize - IOHandler.STANDARD_CHUNK_SIZE){
                        musicPlayer.pause();
                        stoppedByAsyncRunner = true;
                        SystemClock.sleep(timeoutIntervals);
                        continue;
                    }

                    if(!stoppedByButton && stoppedByAsyncRunner && Integer.parseInt(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) > musicPlayer.getCurrentPosition() + songProgressBuffer*2){
                        musicPlayer.start();
                        stoppedByAsyncRunner=false;
                    }


                }


            return null;
        }
    }

}
