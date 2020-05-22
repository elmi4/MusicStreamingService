package com.dsproject.musicstreamingservice.ui.managers.notification;

import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.ui.managers.setup.ApplicationSetup;

import java.util.HashMap;
import java.util.Map;

public class NotificationManager implements Notifier
{
    private Context context;
    private NotificationManagerCompat notificationManager;

    private final Map<String, NotificationCompat.Builder> notificationsMap = new HashMap<>();
    private final Map<String, Integer> notificationIDsMap = new HashMap<>();

    private int highestID = 0;


    public NotificationManager(final Context context)
    {
        this.context = context;
        notificationManager = NotificationManagerCompat.from(context);
    }


    @Override
    public void makeNotification(final String id, final String title, final String description,
                                 final NotificationType type)
    {
        switch (type)
        {
            case PLAIN_STRIPPED:
                addToMaps(id, makePlainStrippedNotification(title, description));

            case PLAIN_VIBRATE:
                addToMaps(id, makePlainVibrateNotification(title, description));
        }

    }


    @Override
    public void showNotification(final String title, final String description)
    {

    }

    @Override
    public void makeAndShowNotification(final String title, final String description,
                                        final NotificationType type)
    {

    }

    @Override
    public void updateNotification(final String id, final int progress)
    {

    }

    @Override
    public void completeNotification(final String id, final String msg)
    {

    }


    //_____________________________________ PRIVATE METHODS ________________________________________


    private void addToMaps(final String id, final NotificationCompat.Builder builder)
    {
        notificationsMap.put(id, builder);
        notificationIDsMap.put(id, ++highestID);
    }

    private NotificationCompat.Builder makePlainStrippedNotification(final String title,
                                                                     final String description)
    {
        return new NotificationCompat.Builder(context, ApplicationSetup.CHANNEL_1_ID)
                        .setSmallIcon(R.drawable.ic_file_download_black_24dp)
                        .setContentTitle(title)
                        .setContentText(description)
                        .setPriority(ApplicationSetup.CHANNEL_1_PRIORITY)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE);
    }

    private NotificationCompat.Builder makePlainVibrateNotification(final String title,
                                                                    final String description)
    {
        return new NotificationCompat.Builder(context, ApplicationSetup.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_file_download_black_24dp)
                .setContentTitle(title)
                .setContentText(description)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setPriority(ApplicationSetup.CHANNEL_1_PRIORITY)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);
    }

}
