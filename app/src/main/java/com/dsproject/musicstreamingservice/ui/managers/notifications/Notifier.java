package com.dsproject.musicstreamingservice.ui.managers.notifications;

public interface Notifier
{
    enum NotificationType{
        PLAIN, //A simple message notification that can be dismissed any time.
        PROGRESS //A notification that displays some progress and needs to be updated (like a download)
    }

    void makeAndShowPlainNotification(String title, String description, Integer rawResourceIcon);

    void makeAndShowProgressNotification(String id, String title, String description, int maxProgress,
                                         boolean indeterminate, Integer rawResourceIcon);
    void updateProgressNotification(String id, int maxProgress, int progress, boolean indeterminate);
    void completeProgressNotification(String id, String msg);

    void vibrate(int duration);
    void vibrateRepeating(int duration, int delay, int repeats);
    void playNotificationSound(Integer rawResourceSound);

}
