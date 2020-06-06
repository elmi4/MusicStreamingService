package com.dsproject.musicstreamingservice.ui.fragments;

import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.domain.assist.io.IOHandler;
import com.dsproject.musicstreamingservice.ui.MainActivity;
import com.dsproject.musicstreamingservice.ui.managers.fragments.MyFragmentManager;
import com.dsproject.musicstreamingservice.ui.managers.notifications.MyNotificationManager;
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
    private  String artist, song;
    private int progress = 0;

    private static MediaPlayer musicPlayer;

    private static List<Byte> sourceBuffer;
    private boolean playerInitialized;
    private static PetrosPlayerFragment instance;

    private enum PlayerRequestState
    {
        ERROR,
        FIRST_REQUEST,
        SAME_REQUEST,
        NEW_REQUEST
    }
    private PlayerRequestState state;


    private PetrosPlayerFragment(List<Byte> buffer) {
        super(MyFragmentManager.getLayoutOf(PlayerFragment.class));
        sourceBuffer = buffer;
        state = PlayerRequestState.FIRST_REQUEST;
    }

    public static PetrosPlayerFragment getInstance(List<Byte> buffer)
    {
        if(instance == null){
            instance = new PetrosPlayerFragment(buffer);
            return instance;
        }

        if(buffer != null && !buffer.equals(sourceBuffer)){
            sourceBuffer = buffer;
            instance.state = PlayerRequestState.NEW_REQUEST;
        }else{
            instance.state = PlayerRequestState.SAME_REQUEST;
        }

        return instance;
    }

    @Override
    public void onActivityCreated(Bundle savedInstance)
    {
        super.onActivityCreated(savedInstance);

        title = view.findViewById(R.id.playerSongTitle);
        songImage = view.findViewById(R.id.playerImageSong);
        pausePlayButton = view.findViewById(R.id.playerPlayPauseButton);
        songProgressBar = view.findViewById(R.id.playerProgressBar);

        Bundle bundle = getArguments();
        if(bundle!= null){
            ArrayList<String> songInfo = bundle.getStringArrayList("songInfo");
            artist = songInfo.get(0);
            song = songInfo.get(1);

            title.setText(song + " by " + artist);

            if(state != PlayerRequestState.SAME_REQUEST) {
                MediaDataSource dataSource = new ByteListMediaDataSource(sourceBuffer);
                musicPlayer = new MediaPlayer();
                musicPlayer.setOnCompletionListener(onPlayerCompleted());
                musicPlayer.setDataSource(dataSource);
            }

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

        state = PlayerRequestState.SAME_REQUEST;
    }


    private MediaPlayer.OnCompletionListener onPlayerCompleted()
    {
        return mp -> {
            musicPlayer.release();
            MainActivity.getNotificationManager().dismissPersistentNotification("player");
        };
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
        musicPlayer.prepare();
        musicPlayer.start();

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(MainActivity.REDIRECT_TAG, MyFragmentManager.PLAYER_FRAG_NAME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        MyNotificationManager notificationManager = MainActivity.getNotificationManager();
        notificationManager.makeAndShowPersistentNotification("player", song+" by "+artist,
                "Click to open player", null, pi);
    }
}
