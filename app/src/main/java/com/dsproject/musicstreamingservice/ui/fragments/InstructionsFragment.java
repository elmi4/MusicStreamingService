package com.dsproject.musicstreamingservice.ui.fragments;

import android.os.Bundle;
import com.dsproject.musicstreamingservice.ui.managers.fragments.MyFragmentManager;


public class InstructionsFragment extends GenericFragment {

    public InstructionsFragment() {
        super(MyFragmentManager.getLayoutOf(InstructionsFragment.class));
    }
    @Override
    public void onActivityCreated(Bundle savedInstance)
    {
        super.onActivityCreated(savedInstance);
    }
}
