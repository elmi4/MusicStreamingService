package com.dsproject.musicstreamingservice.ui.managers.notifications;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.ui.managers.setup.ApplicationSetup;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MyNotificationManager implements Notifier
{
    private Context context;
    private NotificationManagerCompat notificationManagerCompat;

    private final Map<String, NotificationCompat.Builder> notificationsMap = new HashMap<>();
    private final Map<String, Integer> notificationIDsMap = new HashMap<>();

    private static final String PLAIN_TYPE = "plain";
    private static final String PERSISTENT_TYPE = "persistent";
    private static final String PROGRESS_TYPE = "progress";


    /**
     * The instance reference should be outside of all methods.
     * @param context The context of the calling activity.
     */
    public MyNotificationManager(final Context context)
    {
        this.context = context;
        notificationManagerCompat = NotificationManagerCompat.from(context);
    }


    //____________________________ Notifier.NotificationType == PLAIN ______________________________

    /**
     * Creates and shows a Notifier.NotificationType.PLAIN notification.
     * @param title Specified header of the notification.
     * @param description Specified description that goes under the title.
     * @param drawableIcon Specified icon from the res>drawable folder. Can be retrieved as:
     *                     R.drawable.icon_name
     * @param contentIntent Intent that specifies what happens on user click. Null for nothing.
     */
    @Override
    public void makeAndShowPlainNotification(final String title, final String description,
                                             Integer drawableIcon, final PendingIntent contentIntent)
    {
        if(drawableIcon == null){
            drawableIcon = R.drawable.ic_default_notification_black_24dp;
        }

        NotificationCompat.Builder builder = makePlainNotification(
                title, description, drawableIcon, contentIntent);
        showNotification(builder, (int)System.currentTimeMillis());
    }



    //____________________________ Notifier.NotificationType == PERSISTENT ______________________________

    /**
     * Creates and shows a Notifier.NotificationType.PERSISTENT notification.
     * @param id User given identifier used to access the notification later.
     * @param title Specified header of the notification.
     * @param description Specified description that goes under the title.
     * @param drawableIcon Specified icon from the res>drawable folder. Can be retrieved as:
     *                     R.drawable.icon_name
     *
     * @param contentIntent Used to define an action that should happen when the notification is pressed.
     */
    @Override
    public void makeAndShowPersistentNotification(final String id, final String title,
                                                  final String description,
                                                  @Nullable Integer drawableIcon,
                                                  @Nullable final PendingIntent contentIntent)
    {
        if(notificationsMap.containsKey(id)){
            throw new IllegalStateException("Notification of id: "+id+" already exists.");
        }

        if(drawableIcon == null){
            drawableIcon = R.drawable.ic_default_notification_black_24dp;
        }

        NotificationCompat.Builder builder = makePersistentNotification(
                title, description, drawableIcon, contentIntent);
        addToMaps(id, builder);

        showNotification(builder, notificationIDsMap.get(id));
    }

    /**
     * Make the notification dismissible by swiping right or clearing all.
     * @param id The identifier of the notification.
     */
    @Override
    public void makePersistentNotificationDismissible(final String id)
    {
        NotificationCompat.Builder notification = notificationsMap.get(id);
        Integer notifyID = notificationIDsMap.get(id);
        if(notification == null || !isPersistent(notification) || notifyID == null) return;

        notification.setOngoing(false);

        notificationManagerCompat.notify(notifyID, notification.build());
    }

    /**
     * Dismiss programmatically a Notifier.NotificationType.PLAIN notification that cannot be
     * cleared by the user by swiping right or clearing all.
     * @param id The identifier of the notification.
     */
    @Override
    public void dismissPersistentNotification(final String id)
    {
        NotificationCompat.Builder notification = notificationsMap.get(id);
        Integer notifyID = notificationIDsMap.get(id);
        if(notification == null || !isPersistent(notification) || notifyID == null) return;

        notificationManagerCompat.cancel(notifyID);
        free(id);
    }



    //____________________________ Notifier.NotificationType == PROGRESS ______________________________

    /**
     * Creates and shows a Notifier.NotificationType.PROGRESS notification
     * @param id User given identifier used to access the notification later.
     * @param title Specified header of the notification.
     * @param description Specified description that goes under the title.
     * @param maxProgress Max value for the progress bar (can be anything like 100).
     * @param indeterminate {@code true} if the progress bar should not display any real progress,
     *                      just a moving bar until it is finished.
     * @param drawableIcon Specified icon from the res>drawable folder. Can be retrieved as:
     *                     R.drawable.icon_name
     */
    @Override @SuppressWarnings("ConstantConditions")
    public void makeAndShowProgressNotification(final String id, final String title, final String description,
                                                final int maxProgress, final boolean indeterminate,
                                                @Nullable Integer drawableIcon)
    {
        if(notificationsMap.containsKey(id)){
            throw new IllegalStateException("Notification of id: "+id+" already exists.");
        }

        if(drawableIcon == null){
            drawableIcon = R.drawable.ic_file_download_black_24dp;
        }

        NotificationCompat.Builder builder = makeOngoingNotification(
                title, description, maxProgress, indeterminate, drawableIcon);
        addToMaps(id, builder);

        showNotification(builder, notificationIDsMap.get(id));
    }


    /**
     * Updates the progress of an existing Notifier.NotificationType.PROGRESS notification.
     * @param id The identifier specified by the user, that refers to the particular notification.
     * @param maxProgress (probably best to put the original value).
     * @param progress The current value of the progressbar that has to be filled, relative to the max
     * @param indeterminate (probably best to put the original value).
     */
    @Override
    public void updateProgressNotification(final String id, final int maxProgress,
                                           final int progress, final boolean indeterminate)
    {
        NotificationCompat.Builder notification = notificationsMap.get(id);
        Integer notifyID = notificationIDsMap.get(id);
        if(notification == null || !isProgress(notification) || notifyID == null) return;

        notification.setProgress(maxProgress, progress, indeterminate);
        notificationManagerCompat.notify(notifyID, notification.build());
    }


    /**
     * Replace the progress bar of the Notifier.NotificationType.PROGRESS notification with a finish text.
     * After this function call, the notification cannot be referenced any more.
     * @param id The identifier specified by the user, that refers to the particular notification.
     * @param msg The finish text to be displayed instead of the progress bar.
     * @param contentIntent Used to define an action that should happen when the notification is pressed.
     */
    @Override
    public void completeProgressNotification(final String id, final String msg, final PendingIntent contentIntent)
    {
        NotificationCompat.Builder notification = notificationsMap.get(id);
        Integer notifyID = notificationIDsMap.get(id);
        if(notification == null || !isProgress(notification) || notifyID == null) return;

        notification
                .setContentText(msg)
                .setProgress(0, 0, false)
                .setOngoing(false);

        if(contentIntent != null){
            notification.setContentIntent(contentIntent);
        }

        notificationManagerCompat.notify(notifyID, notification.build());
        free(id);
    }


    /**
     * Create vibration effect.
     * @param duration The time in milliseconds that the phone should vibrate
     */
    @Override
    public void vibrate(final int duration)
    {
        Vibrator vibrator =  (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if(vibrator == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //if current API >= 26
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            vibrator.vibrate(duration);
        }
    }


    /**
     * Create repeating vibration effects, with specified number of repeats and delay between them.
     * @param duration The time in milliseconds that the phone should vibrate.
     * @param delay Milliseconds to wait before the next vibration, after the last one has finished.
     * @param repeats The number of vibration repeats.
     */
    @Override
    public void vibrateRepeating(final int duration, final int delay, final int repeats)
    {
        Vibrator vibrator =  (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if(vibrator == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //if current API >= 26
            long[] pattern = new long[repeats * 2];
            for (int i = 0; i < repeats * 2; i+=2) {
                pattern[i] = duration;
                pattern[i+1] = delay;
            }
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        }else{
            vibrateRecursive(duration,delay,repeats,0);
        }
    }


    /**
     * Plays a specified sound or the default android notification sound.
     * @param rawResourceSound The id of the file inside the "raw" folder of "res", that can be
     *                         retrieved as R.raw.file_name, or null to use the default sound.
     *                         If the "raw" folder doesn't exist, right click on "res" and select:
     *                         New>Folder>Raw Resources Folder
     *                         and put in there the custom audio file for the notification.
     */
    @Override
    public void playNotificationSound(final Integer rawResourceSound)
    {
        Uri alarmSound;
        if(rawResourceSound == null){
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }else{
            alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + context.getPackageName() + "/" +  rawResourceSound);
        }

        Ringtone r = RingtoneManager.getRingtone(context, alarmSound);
        if(r == null) return;
        r.play();
    }




    //_____________________________________ PRIVATE METHODS ________________________________________

    private void vibrateRecursive(final int duration, final int delay, final int maxRepeats,
                                  final int currentRepeat)
    {
        vibrate(duration);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(currentRepeat < maxRepeats - 1){
                    vibrate(duration);
                    vibrateRecursive(duration,delay,maxRepeats,currentRepeat+1);
                }
            }
        }, delay+duration);
    }

    private void addToMaps(final String id, final NotificationCompat.Builder builder)
    {
        notificationsMap.put(id, builder);
        notificationIDsMap.put(id, (int)System.currentTimeMillis());
    }

    private void free(final String id)
    {
        notificationsMap.remove(id);
        notificationIDsMap.remove(id);
    }

    private boolean isPlain(final NotificationCompat.Builder builder)
    {
        return builder.getExtras().getString("NotificationType").equals(PLAIN_TYPE);
    }

    private boolean isPersistent(final NotificationCompat.Builder builder)
    {
        return builder.getExtras().getString("NotificationType").equals(PERSISTENT_TYPE);

    }

    private boolean isProgress(final NotificationCompat.Builder builder)
    {
        return builder.getExtras().getString("NotificationType").equals(PROGRESS_TYPE);
    }

    private void showNotification(final NotificationCompat.Builder builder, final int notifyID)
    {
        notificationManagerCompat.notify(notifyID, builder.build());
    }

    private NotificationCompat.Builder makePlainNotification(final String title,
                                                             final String description,
                                                             final int drawableIcon,
                                                             final PendingIntent contentIntent)
    {
        Bundle extras = new Bundle();
        extras.putString("NotificationType", PLAIN_TYPE);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, ApplicationSetup.CHANNEL_1_ID)
                        .setSmallIcon(drawableIcon)
                        .setContentTitle(title)
                        .setContentText(description)
                        .setPriority(ApplicationSetup.CHANNEL_1_PRIORITY)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setExtras(extras);

        if(contentIntent != null){
            builder.setContentIntent(contentIntent);
        }

        return builder;
    }

    private NotificationCompat.Builder makePersistentNotification(final String title,
                                                             final String description,
                                                             final int drawableIcon,
                                                             final PendingIntent contentIntent)
    {
        Bundle extras = new Bundle();
        extras.putString("NotificationType", PERSISTENT_TYPE);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, ApplicationSetup.CHANNEL_1_ID)
                        .setSmallIcon(drawableIcon)
                        .setContentTitle(title)
                        .setContentText(description)
                        .setOngoing(true)
                        .setPriority(ApplicationSetup.CHANNEL_1_PRIORITY)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setExtras(extras);

        if(contentIntent != null){
            builder.setContentIntent(contentIntent);
        }

        return builder;
    }

    private NotificationCompat.Builder makeOngoingNotification(final String title,
                                                               final String description,
                                                               final int maxProgress,
                                                               final boolean indeterminate,
                                                               final int drawableIcon)
    {
        Bundle extras = new Bundle();
        extras.putString("NotificationType", PROGRESS_TYPE);

        return new NotificationCompat.Builder(context, ApplicationSetup.CHANNEL_1_ID)
                    .setSmallIcon(drawableIcon)
                    .setContentTitle(title)
                    .setContentText(description)
                    .setProgress(maxProgress,0, indeterminate)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setExtras(extras);
    }
}
