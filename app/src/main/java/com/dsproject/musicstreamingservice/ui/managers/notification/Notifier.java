package com.dsproject.musicstreamingservice.ui.managers.notification;

public interface Notifier
{
    enum NotificationType{
        PLAIN_STRIPPED,
        PLAIN_VIBRATE,
        PLAIN_SOUND,
        DOWNLOAD_INDETER,
        DOWNLOAD_PROGRESS
    }

    void makeNotification(String id, String title, String description, NotificationType type);
    void showNotification(String title, String description);
    void makeAndShowNotification(String title, String description, NotificationType type);
    void updateNotification(String id, int progress);
    void completeNotification(String id, String msg);
}
