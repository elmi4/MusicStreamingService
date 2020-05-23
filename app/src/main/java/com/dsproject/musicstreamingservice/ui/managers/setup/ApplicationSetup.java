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

import com.dsproject.musicstreamingservice.domain.Consumer;
import com.dsproject.musicstreamingservice.domain.assist.network.ConnectionInfo;


/**
 * This class should run before any activity. To do that, add:
 * android:name="PACKAGE_NAME_HERE.ApplicationSetup"
 * Inside AndroidManifest after <application
 */
public class ApplicationSetup extends Application
{
    public static final String CHANNEL_1_ID = "default_channel";
    public static final int CHANNEL_1_PRIORITY = NotificationCompat.PRIORITY_DEFAULT;

    @Override
    public void onCreate()
    {
        super.onCreate();
        createNotificationChannels();
    }

    /**
     * Takes care of the creating of notification channels (API >= 26)
     * Doesn't create problems with API < 26, since it will not do any setup.
     */
    private void createNotificationChannels()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //channel1 init
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Downloads Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel1.setDescription("This should be a real description of the channel's use normally.");

            //adding channel
            NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if(manager == null){
                throw new IllegalStateException("No Notification manager could be retrieved from the system");
            }
            manager.createNotificationChannel(channel1);
        }
    }

    /**
     * Utility function that checks if a channelID is associated with an active notification channel
     */
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
