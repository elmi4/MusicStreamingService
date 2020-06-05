package com.dsproject.musicstreamingservice.ui.fragments;

import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
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
import com.dsproject.musicstreamingservice.ui.util.ByteListMediaDataSource;
import com.dsproject.musicstreamingservice.ui.util.OnBufferInitializedEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PetrosPlayerFragment extends GenericFragment
        implements MediaPlayer.OnErrorListener, OnBufferInitializedEvent
{
    private TextView title;
    private ImageView songImage;
    private ImageButton pausePlayButton;
    private ProgressBar songProgressBar;
    private String artist, song;
    private int progress = 0;

    private List<Byte> sourceBuffer;//CopyOnWriteArrayList
    private MediaPlayer musicPlayer;


    public PetrosPlayerFragment(List<Byte> buffer) {
        super(MyFragmentManager.getLayoutOf(PlayerFragment.class));
        sourceBuffer = buffer;
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

//            creating a custom MediaDataSource from where the media player will load the data. It contains
//            a List<Byte> and needs to be updated inside the consumer while he receives the chunks
//            method "requestAndAppendSongDataToByteArray" is supposed to do that. Takes as parameter
//            the same List<Byte> as the ByteListMediaDataSource. If synchronization is needed,
//            instead of ArrayList, the List could be a CopyOnWriteArrayList

            //test method to get the mp3 bytes into memory
           // sourceBuffer = IOHandler.readMp31(getActivity());
            MediaDataSource dataSource = new ByteListMediaDataSource(sourceBuffer);

            musicPlayer = new MediaPlayer();
            musicPlayer.setOnCompletionListener(MediaPlayer::release);
            musicPlayer.setDataSource(dataSource);
//            try {
//                musicPlayer.prepare();
//            } catch (IOException e) {
//                e.printStackTrace();
//                title.setText("Error playing file");
//            }
//            System.out.println("PAAAASEEED PREPAAAREEEEEEEEEEEEEEEEEEEEE");
//            musicPlayer.start();

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


    //debug info about fails
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int why){
        String TAG ="mediaplayererror";
        Log.e(TAG, "onError");
        if (MediaPlayer.MEDIA_ERROR_UNKNOWN == what) {
            Log.d(TAG, "MEDIA_ERROR_UNKNOWN");
            if (MediaPlayer.MEDIA_ERROR_IO == why) {
                Log.e(TAG, "MEDIA_ERROR_IO");
            }
            if (MediaPlayer.MEDIA_ERROR_MALFORMED == why) {
                Log.e(TAG, "MEDIA_ERROR_MALFORMED");
            }
            if (MediaPlayer.MEDIA_ERROR_UNSUPPORTED == why) {
                Log.e(TAG, "MEDIA_ERROR_UNSUPPORTED");
            }
            if (MediaPlayer.MEDIA_ERROR_TIMED_OUT == why) {
                Log.e(TAG, "MEDIA_ERROR_TIMED_OUT");
            }
        } else if (MediaPlayer.MEDIA_ERROR_SERVER_DIED == what) {
            Log.e(TAG, "MEDIA_ERROR_SERVER_DIED");
        }
        return true;
    }


    public void prepareAndStartSong() throws IOException
    {
        System.out.println("PLAYER LISTENEEEEEEEEEEEEEEEEED");
        musicPlayer.prepare();
        musicPlayer.start();
    }
}
