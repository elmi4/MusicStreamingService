package com.dsproject.musicstreamingservice.ui.managers.fragments;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.ui.fragments.ArtistsFragment;
import com.dsproject.musicstreamingservice.ui.fragments.CustomRequestFragment;
import com.dsproject.musicstreamingservice.ui.fragments.GenericFragment;
import com.dsproject.musicstreamingservice.ui.fragments.SettingsFragment;

public abstract class MyFragmentManager
{
    public static final int ARTISTS_FRAG_LAYOUT = R.layout.artists_fragment,
                            CUSTOM_REQ_FRAG_LAYOUT = R.layout.custom_request_fragment,
                            SETTINGS_FRAG_LAYOUT = R.layout.settings_fragment;
//                            CREDITS_FRAG_LAYOUT = R.layout.INSERT_HERE,
//                            INSTRUCTIONS_FRAG_LAYOUT = R.layout.INSERT_HERE;


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

}
