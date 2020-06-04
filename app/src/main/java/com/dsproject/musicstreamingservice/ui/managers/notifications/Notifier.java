package com.dsproject.musicstreamingservice.ui.managers.notifications;

import android.app.PendingIntent;

import androidx.annotation.Nullable;


public interface Notifier
{
    enum NotificationType
    {
        //A simple message notification that can be dismissed any time.
        PLAIN,

        //A message notification with ID, that cannot be dismissed manually.
        //Use appropriate dismiss method by providing the creation ID
        PERSISTENT,

        //A notification with ID, that displays a progress bar (like a download).
        //It needs to be updated
        //Cannot be dismissed manually
        PROGRESS
    }

    //______________________________________ Type == PLAIN _________________________________________
    void makeAndShowPlainNotification(String title, String description,
                                      @Nullable Integer drawableIcon,
                                      @Nullable PendingIntent pendingIntent);


    //______________________________________ Type == PERSISTENT _________________________________________
    void makeAndShowPersistentNotification(String id, String title, String description,
                                           @Nullable Integer drawableIcon,
                                           @Nullable PendingIntent pendingIntent);
    void makePersistentNotificationDismissible(String id);
    void dismissPersistentNotification(String id);


    //______________________________________ Type == PROGRESS _________________________________________
    void makeAndShowProgressNotification(String id, String title, String description, int maxProgress,
                                         boolean indeterminate, @Nullable Integer drawableIcon);
    void updateProgressNotification(String id, int maxProgress, int progress, boolean indeterminate);
    void completeProgressNotification(String id, String msg, @Nullable PendingIntent contentIntent);
}
