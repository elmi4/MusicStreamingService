package com.dsproject.musicstreamingservice.ui.util;

import android.app.Activity;
import android.os.AsyncTask;

import com.dsproject.musicstreamingservice.domain.Consumer;
import com.dsproject.musicstreamingservice.ui.MainActivity;
import com.dsproject.musicstreamingservice.ui.fragments.PlayerFragment;
import com.dsproject.musicstreamingservice.ui.managers.connections.MyConnectionsManager;

import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.List;

public class AsyncConsumerRequest extends AsyncTask<RequestDetails, Void, Boolean>
{
    private WeakReference<Activity> activity;

    public AsyncConsumerRequest(Activity activity)
    {
        this.activity = new WeakReference<>(activity);
    }

    @Override
    protected Boolean doInBackground(final RequestDetails... details)
    {
        Socket brokerConnection = MyConnectionsManager.getConnectionWithABroker(activity.get());
        if(brokerConnection == null){
            UtilitiesUI.showToast(activity.get(), MyConnectionsManager.CANNOT_CONNECT_MSG);
            MainActivity.getNotificationManager().makeNoConnectionNotification();
            return false;
        }

        String artistName = details[0].getArtistName();
        String songName = details[0].getSongName();
        Consumer.RequestType type = details[0].getType();
        List<Byte> dataBuffer = details[0].getBuffer();
        PlayerFragment playerFragment = details[0].getPlayerFragment();

        Consumer c1 = new Consumer(brokerConnection, activity.get());
        c1.init();

        if(dataBuffer == null){
            return c1.requestSongData(artistName, songName, type);
        }else{
            return c1.requestAndAppendSongDataToBuffer(artistName, songName, dataBuffer, playerFragment);
        }
    }
}
