
        package com.dsproject.musicstreamingservice.ui.fragments;

        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.media.MediaDataSource;
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
        import android.widget.ProgressBar;
        import android.widget.TextView;

        import androidx.annotation.NonNull;
        import androidx.annotation.Nullable;

        import com.dsproject.musicstreamingservice.R;
        import com.dsproject.musicstreamingservice.domain.assist.io.IOHandler;
        import com.dsproject.musicstreamingservice.ui.managers.fragments.MyFragmentManager;
        import com.dsproject.musicstreamingservice.ui.util.ByteListMediaDataSource;
        import com.dsproject.musicstreamingservice.ui.util.OnBufferInitializedEvent;

        import java.io.File;
        import java.io.FileNotFoundException;
        import java.io.IOException;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.Scanner;

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
    MediaDataSource dataSource;


    private MediaMetadataRetriever metadataRetriever;
    private boolean stop = false;
    private boolean imageSet = false;


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
            dataSource = new ByteListMediaDataSource(sourceBuffer);

            musicPlayer = new MediaPlayer();
            musicPlayer.setOnCompletionListener(MediaPlayer::release);
            musicPlayer.setDataSource(dataSource);

            metadataRetriever = new MediaMetadataRetriever();


//            try {
//                musicPlayer.prepare();
//            } catch (IOException e) {
//                e.printStackTrace();
//                title.setText("Error playing file");
//            }
//            System.out.println("PAAAASEEED PREPAAAREEEEEEEEEEEEEEEEEEEEE");
//            musicPlayer.start();

            while(!imageSet) {
                try {
                    if(dataSource.getSize() > 1 && metadataRetriever != null && musicPlayer !=null && musicPlayer.isPlaying()) {

                        metadataRetriever.setDataSource(dataSource);

                        byte [] imageData = metadataRetriever.getEmbeddedPicture();

                        // convert the byte array to a bitmap
                        if(imageData != null)
                        {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                            songImage.setImageBitmap(bitmap); //associated cover art in bitmap
                            imageSet = true;
                            System.out.println("Cover art set");
                        }

                    }
                } catch (IOException e) {

                }
            }


            new Thread((Runnable) () -> {
                while(!stop){
                    try {
                        if(dataSource.getSize() > 1 && metadataRetriever != null && musicPlayer!=null && musicPlayer.isPlaying() ) {
                            metadataRetriever.setDataSource(dataSource);
                            float percentage = (float)( (float)musicPlayer.getCurrentPosition() / Float.parseFloat(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
                            songProgressBar.setProgress((int)(percentage * (float)100));
                            SystemClock.sleep(100);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();



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
        stop = true;
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

