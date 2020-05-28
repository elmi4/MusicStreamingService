package com.dsproject.musicstreamingservice.ui.managers.fragments;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.ui.fragments.ArtistsFragment;
import com.dsproject.musicstreamingservice.ui.fragments.CustomRequestFragment;
import com.dsproject.musicstreamingservice.ui.fragments.GenericFragment;
import com.dsproject.musicstreamingservice.ui.fragments.SettingsFragment;
import com.dsproject.musicstreamingservice.ui.irrelevantActivities.CreditsActivity;

public abstract class MyFragmentManager
{
    //layouts of fragments
    public static final int ARTISTS_FRAG_LAYOUT = R.layout.artists_fragment,
                            CUSTOM_REQ_FRAG_LAYOUT = R.layout.custom_request_fragment,
                            SETTINGS_FRAG_LAYOUT = R.layout.settings_fragment;
//                            CREDITS_FRAG_LAYOUT = R.layout.INSERT_HERE,
//                            INSTRUCTIONS_FRAG_LAYOUT = R.layout.INSERT_HERE;

    //String names of fragments
    public static final String  ARTISTS_FRAG_NAME = "artists_fragment",
                                CUSTOM_REQ_FRAG_NAME = "custom_request_fragment",
                                SETTINGS_FRAG_NAME = "settings_fragment",
                                CREDITS_FRAG_NAME = "credits_fragment",
                                INSTRUCTIONS_FRAG_NAME = "instructions_fragment";



    //TODO: More cases to be added for each fragment class implemented
    public static <T extends GenericFragment> int getLayoutOf(final Class<T> fragClass)
    {
        if(fragClass == ArtistsFragment.class){
            return ARTISTS_FRAG_LAYOUT;
        }else if(fragClass == CustomRequestFragment.class){
            return CUSTOM_REQ_FRAG_LAYOUT;
        }else if(fragClass == SettingsFragment.class){
            return SETTINGS_FRAG_LAYOUT;
        }else {
            throw new IllegalArgumentException("Couldn't find layout for "+fragClass);
        }
    }

    public static GenericFragment getFragmentByName(final String fragName)
    {
        switch (fragName) {
            case ARTISTS_FRAG_NAME:
                return new ArtistsFragment();
            case CUSTOM_REQ_FRAG_NAME:
                return new CustomRequestFragment();
            case SETTINGS_FRAG_NAME:
                return new SettingsFragment();
            default:
                throw new IllegalArgumentException("Couldn't find class of " + fragName);
        }
    }

}
