package com.dsproject.musicstreamingservice.ui;

import android.app.Activity;
import android.widget.Toast;

public abstract class UtilitiesUI
{
    private UtilitiesUI(){}

    public static void showToast(final Activity activity, final String msg)
    {
        activity.runOnUiThread(() -> {
            final Toast toast = Toast.makeText(activity, msg, Toast.LENGTH_LONG);
            toast.show();
        });
    }
}
