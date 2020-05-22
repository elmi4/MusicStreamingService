package com.dsproject.musicstreamingservice.ui.managers.setup;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Class That runs before any activity is created.
 */
public class ApplicationSetup extends Application
{
    public static final String CHANNEL_1_ID = "downloads_channel";
    public static final int CHANNEL_1_PRIORITY = NotificationCompat.PRIORITY_HIGH;

    @Override
    public void onCreate()
    {
        super.onCreate();
        createNotificationChannels();
    }

    /**
     * Takes care of the creating of notification channels (API >= 26)
     */
    private void createNotificationChannels()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Downloads Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.enableVibration(true);
            channel1.setDescription("This is the channel that handles download notifications");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if(manager == null){
                throw new IllegalStateException("No Notification manager could be retrieved from the system");
            }
            manager.createNotificationChannel(channel1);
        }
    }

    public static boolean isNotificationChannelEnabled(Context context, @Nullable String channelId)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(!TextUtils.isEmpty(channelId)) {
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel channel = manager.getNotificationChannel(channelId);
                if(channel == null) throw new IllegalStateException("NULL CHANNEL");
                return channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
            }
            return false;
        } else {
            return NotificationManagerCompat.from(context).areNotificationsEnabled();
        }
    }
}
