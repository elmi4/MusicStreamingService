package com.dsproject.musicstreamingservice.ui.fragments;

import android.os.Bundle;
import com.dsproject.musicstreamingservice.ui.managers.fragments.MyFragmentManager;


public class CreditsFragment extends GenericFragment {

    public CreditsFragment() {
        super(MyFragmentManager.getLayoutOf(CreditsFragment.class));
    }
    @Override
    public void onActivityCreated(Bundle savedInstance)
    {
        super.onActivityCreated(savedInstance);
    }
}
