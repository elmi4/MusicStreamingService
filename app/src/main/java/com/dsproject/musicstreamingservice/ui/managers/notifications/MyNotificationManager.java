package com.dsproject.musicstreamingservice.ui.managers.notifications;

import android.content.ContentResolver;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;

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

    private final Map<String, NotificationCompat.Builder> progressNotificationsMap = new HashMap<>();
    private final Map<String, Integer> progressNotificationIDsMap = new HashMap<>();

    private int highestID = 0;


    public MyNotificationManager(final Context context)
    {
        this.context = context;
        notificationManagerCompat = NotificationManagerCompat.from(context);
    }


    /**
     * Creates and shows a Notifier.NotificationType.PLAIN notification.
     * @param title Specified header of the notification.
     * @param description Specified description that goes under the title.
     * @param drawableIcon Specified icon from the res>drawable folder. Can be retrieved as:
     *                     R.drawable.icon_name
     */
    @Override
    public void makeAndShowPlainNotification(final String title, final String description,
                                             Integer drawableIcon)
    {
        if(drawableIcon == null){
            drawableIcon = R.drawable.ic_default_notification_black_24dp;
        }

        NotificationCompat.Builder builder = makePlainNotification(
                title, description, drawableIcon);
        showNotification(builder, ++highestID);
    }


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
                                                Integer drawableIcon)
    {
        if(progressNotificationsMap.get(id) != null){
            System.err.println("Notification of id: "+id+" already exists.");
            return;
        }

        if(drawableIcon == null){
            drawableIcon = R.drawable.ic_file_download_black_24dp;
        }

        NotificationCompat.Builder builder = makeOngoingNotification(
                title,description, maxProgress, indeterminate, drawableIcon);
        addToMaps(id, builder);
        showNotification(builder, progressNotificationIDsMap.get(id));
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
        NotificationCompat.Builder notification = progressNotificationsMap.get(id);
        Integer notifyID = progressNotificationIDsMap.get(id);
        if(notification == null || isPlain(notification) || notifyID == null) return;

        notification.setProgress(maxProgress, progress, indeterminate);
        notificationManagerCompat.notify(notifyID, notification.build());
    }


    /**
     * Replace the progress bar of the Notifier.NotificationType.PROGRESS notification with a finish text.
     * @param id The identifier specified by the user, that refers to the particular notification.
     * @param msg The finish text to be displayed instead of the progress bar.
     */
    @Override
    public void completeProgressNotification(final String id, final String msg)
    {
        NotificationCompat.Builder notification = progressNotificationsMap.get(id);
        Integer notifyID = progressNotificationIDsMap.get(id);
        if(notification == null || isPlain(notification) || notifyID == null) return;

        notification
                .setContentText(msg)
                .setProgress(0, 0, false)
                .setOngoing(false);
        notificationManagerCompat.notify(notifyID, notification.build());

        free(id);
    }


    /**
     * Create vibration effect.
     * @param duration The time in milliseconds that the phone should vibrate
     */
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
    public void playNotificationSound(final Integer rawResourceSound)
    {
        Uri alarmSound;
        if(rawResourceSound == null){
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }else{
            alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + context.getPackageName() + "/" +  R.raw.notification_sound);
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
        progressNotificationsMap.put(id, builder);
        progressNotificationIDsMap.put(id, ++highestID);
    }

    private void free(final String id)
    {
        progressNotificationsMap.remove(id);
        progressNotificationIDsMap.remove(id);
    }

    private boolean isPlain(final NotificationCompat.Builder builder)
    {
        return builder.getExtras().getBoolean("plain");
    }


    private void showNotification(final NotificationCompat.Builder builder, final int notifyID)
    {
        notificationManagerCompat.notify(notifyID, builder.build());
    }

    private NotificationCompat.Builder makePlainNotification(final String title,
                                                             final String description,
                                                             final int drawableIcon)
    {
        Bundle extras = new Bundle();
        extras.putBoolean("plain", true);

        return new NotificationCompat.Builder(context, ApplicationSetup.CHANNEL_1_ID)
                        .setSmallIcon(drawableIcon)
                        .setContentTitle(title)
                        .setContentText(description)
                        .setPriority(ApplicationSetup.CHANNEL_1_PRIORITY)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setExtras(extras);
    }

    private NotificationCompat.Builder makeOngoingNotification(final String title,
                                                               final String description,
                                                               final int maxProgress,
                                                               final boolean indeterminate,
                                                               final int drawableIcon)
    {
        Bundle extras = new Bundle();
        extras.putBoolean("plain", false);

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
