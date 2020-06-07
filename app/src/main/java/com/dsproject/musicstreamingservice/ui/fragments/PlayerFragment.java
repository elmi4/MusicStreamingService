package com.dsproject.musicstreamingservice.ui.fragments;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaDataSource;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.ui.MainActivity;
import com.dsproject.musicstreamingservice.ui.managers.fragments.MyFragmentManager;
import com.dsproject.musicstreamingservice.ui.managers.notifications.MyNotificationManager;
import com.dsproject.musicstreamingservice.ui.util.ByteListMediaDataSource;
import com.dsproject.musicstreamingservice.ui.util.OnBufferInitializedEvent;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PlayerFragment extends GenericFragment
        implements OnBufferInitializedEvent
{
    public static final String NOTIFICATION_ID = "player";

    private TextView title;
    private ImageView songImage;
    private ImageButton pausePlayButton;
    private ProgressBar songProgressBar;
    private String artist, song;

    private static boolean songIsOngoing = true;
    private static MediaMetadataRetriever metadataRetriever;
    private static MediaPlayer musicPlayer;
    private static List<Byte> sourceBuffer;
    private static PlayerFragment instance;
    private static MediaDataSource dataSource;

    private enum PlayerRequestState
    {
        ERROR,
        FIRST_REQUEST,
        SAME_REQUEST,
        NEW_REQUEST
    }
    private PlayerRequestState state;


    private PlayerFragment(List<Byte> buffer)
    {
        super(MyFragmentManager.getLayoutOf(PlayerFragment.class));
        sourceBuffer = buffer;
        state = PlayerRequestState.FIRST_REQUEST;
    }

    /**
     * Singleton pattern to persist object and control the initialization of some important variables
     */
    public static PlayerFragment getInstance(final List<Byte> buffer)
    {
        if (instance == null) {
            instance = new PlayerFragment(buffer);
            return instance;
        }

        if (buffer != null && !buffer.equals(sourceBuffer)) {
            sourceBuffer = buffer;
            instance.state = PlayerRequestState.NEW_REQUEST;
        } else {
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
        if (bundle != null) {
            ArrayList<String> songInfo = bundle.getStringArrayList("songInfo");
            artist = songInfo.get(0);
            song = songInfo.get(1);

            setTitleText(song + " by " + artist);

            pausePlayButton.setOnClickListener(pausePlayBtnOnClick);

            //These variables need to be initialized only when a new song request is made
            if (state != PlayerRequestState.SAME_REQUEST) {
                dataSource = new ByteListMediaDataSource(sourceBuffer);
                musicPlayer = new MediaPlayer();
                musicPlayer.setOnCompletionListener(playerOnCompletion);
                musicPlayer.setOnErrorListener(playerOnError);
                musicPlayer.setDataSource(dataSource);
                metadataRetriever = new MediaMetadataRetriever();
            }

            if(state == PlayerRequestState.SAME_REQUEST){
                new BitmapWorkerTask(songImage).execute();
            }
        }

        state = PlayerRequestState.SAME_REQUEST;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        songIsOngoing = false;

        MainActivity.getNotificationManager().dismissPersistentNotification(NOTIFICATION_ID);

        if (musicPlayer != null) {
            musicPlayer.release();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        songIsOngoing = false;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        songIsOngoing = true;
        updateProgressBarNewThread();
    }

    /**
     * Method To be called by the consumer when it receives the first chunk of data
     */
    public void notifyMediaPlayer(final boolean isValidRequest)
    {
        if(isValidRequest){
            try {
                musicPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
                setTitleText("Error playing file.");
                return;
            }
            musicPlayer.start();

            metadataRetriever.setDataSource(dataSource);
            updateProgressBarNewThread();
            new BitmapWorkerTask(songImage).execute();

            createPlayerNotification();
        }else {
            MainActivity.getNotificationManager().dismissPersistentNotification(NOTIFICATION_ID);
            getActivity().getSupportFragmentManager().beginTransaction().remove(instance).commit();
        }
    }


    /**
     * A new Thread responsible of updating the progressbar of the player
     */
    private void updateProgressBarNewThread()
    {
        // Change the progress of the progress bar
        new Thread(() -> {
            try {
                long dataSourcePrevSize = dataSource.getSize();
                long dataSourceCurrSize;
                String durationMeta = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                float totalSongSize = (durationMeta == null) ? dataSourcePrevSize : Float.parseFloat(durationMeta);

                while (songIsOngoing) {
                    dataSourceCurrSize = dataSource.getSize();
                    if(dataSourceCurrSize != dataSourcePrevSize){
                        dataSourcePrevSize = dataSourceCurrSize;
                        metadataRetriever.setDataSource(dataSource);
                        totalSongSize = Float.parseFloat(
                                metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    }

                    if (dataSourceCurrSize > 1 && metadataRetriever != null &&
                            musicPlayer != null && musicPlayer.isPlaying())
                    {
                        songProgressBar.setProgress((int)(calculateSongProgress(totalSongSize) * 100));
                        SystemClock.sleep(100);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private float calculateSongProgress(final float totalSize)
    {
        return ((float) musicPlayer.getCurrentPosition() / totalSize);
    }

    private void setTitleText(final String titleText)
    {
        title.setText(titleText);
    }

    /**
     * Creating a persistent notification used to redirect the user to the player fragment
     */
    private void createPlayerNotification()
    {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(MainActivity.REDIRECT_TAG, MyFragmentManager.PLAYER_FRAG_NAME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        MyNotificationManager notificationManager = MainActivity.getNotificationManager();
        notificationManager.makeAndShowPersistentNotification(NOTIFICATION_ID, song + " by " + artist,
                "Click to open player", null, pi);
    }


    /**
     * Async class used to load the image of the provided mp3 if it exists.
     */
    static class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap>
    {
        private final WeakReference<ImageView> imageViewReference;

        BitmapWorkerTask(ImageView imageView)
        {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params)
        {
            try {
                if (dataSource.getSize() > 1 && metadataRetriever != null &&
                        musicPlayer != null && musicPlayer.isPlaying())
                {
                    byte[] imageData = metadataRetriever.getEmbeddedPicture();

                    // convert the byte array to a bitmap
                    if (imageData != null) {
                        return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            if (bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }


    //________________________________________ LISTENERS ___________________________________________

    private MediaPlayer.OnCompletionListener playerOnCompletion = (v) -> {
        songIsOngoing = false;
        musicPlayer.release();

        //remove the persistent notification that redirected to the player fragment
        MainActivity.getNotificationManager().dismissPersistentNotification(NOTIFICATION_ID);

        //close player and return to the previous fragment
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        getActivity().getSupportFragmentManager().popBackStackImmediate();
    };

    private MediaPlayer.OnErrorListener playerOnError = (mp, what, why) -> {
        String TAG = "mediaplayererror";
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
    };

    private View.OnClickListener pausePlayBtnOnClick = (v) -> {
        if (musicPlayer.isPlaying()) {
            musicPlayer.pause();
            pausePlayButton.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                    R.drawable.ic_play_circle_outline_black_24dp));
        } else {
            musicPlayer.start();
            pausePlayButton.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                    R.drawable.ic_pause_circle));
        }
    };

}

