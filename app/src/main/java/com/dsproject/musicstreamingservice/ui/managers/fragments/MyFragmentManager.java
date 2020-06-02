package com.dsproject.musicstreamingservice.ui.managers.fragments;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.ui.fragments.ArtistsFragment;
import com.dsproject.musicstreamingservice.ui.fragments.CreditsFragment;
import com.dsproject.musicstreamingservice.ui.fragments.CustomRequestFragment;
import com.dsproject.musicstreamingservice.ui.fragments.GenericFragment;
import com.dsproject.musicstreamingservice.ui.fragments.InstructionsFragment;
import com.dsproject.musicstreamingservice.ui.fragments.PlayerFragment;
import com.dsproject.musicstreamingservice.ui.fragments.SettingsFragment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class MyFragmentManager
{
    //layouts of fragments
    public static final int ARTISTS_FRAG_LAYOUT = R.layout.fragment_artists,
                            CUSTOM_REQ_FRAG_LAYOUT = R.layout.fragment_custom_request,
                            SETTINGS_FRAG_LAYOUT = R.layout.fragment_settings,
                            CREDITS_FRAG_LAYOUT = R.layout.fragment_credits,
                            INSTRUCTIONS_FRAG_LAYOUT = R.layout.fragment_instructions,
                            PLAYER_FRAG_LAYOUT = R.layout.fragment_player;

    //String names of fragments
    public static final String  ARTISTS_FRAG_NAME = "fragment_artists",
                                CUSTOM_REQ_FRAG_NAME = "fragment_custom_request",
                                SETTINGS_FRAG_NAME = "fragment_settings",
                                CREDITS_FRAG_NAME = "fragment_credits",
                                INSTRUCTIONS_FRAG_NAME = "fragment_instructions",
                                PLAYER_FRAG_NAME = "player_fragment";

    private static final Map<Class, Integer> classToLayout = Collections.unmodifiableMap(
                                                                populateClassToLayoutMap());


    private MyFragmentManager(){}

    private static Map<Class, Integer> populateClassToLayoutMap()
    {
        Map<Class, Integer> classToLayout = new HashMap<>();
        classToLayout.put(ArtistsFragment.class, ARTISTS_FRAG_LAYOUT);
        classToLayout.put(SettingsFragment.class, SETTINGS_FRAG_LAYOUT);
        classToLayout.put(CustomRequestFragment.class, CUSTOM_REQ_FRAG_LAYOUT);
        classToLayout.put(InstructionsFragment.class, INSTRUCTIONS_FRAG_LAYOUT);
        classToLayout.put(CreditsFragment.class, CREDITS_FRAG_LAYOUT);
        classToLayout.put(PlayerFragment.class, CREDITS_FRAG_LAYOUT);

        return classToLayout;
    }

    //TODO: More cases to be added for each fragment class implemented
    public static <T extends GenericFragment> Integer getLayoutOf(final Class<T> fragClass)
    {
        if(!classToLayout.containsKey(fragClass)){
            throw new IllegalArgumentException("No layout found for the provided class.");
        }

        return classToLayout.get(fragClass);
    }

    public static GenericFragment getFragmentByName(final String fragName)
    {
        switch (fragName) {
            case ARTISTS_FRAG_NAME:
                return new ArtistsFragment();
            case PLAYER_FRAG_NAME:
                return new PlayerFragment();
            case CUSTOM_REQ_FRAG_NAME:
                return new CustomRequestFragment();
            case SETTINGS_FRAG_NAME:
                return new SettingsFragment();
            case CREDITS_FRAG_NAME:
                return new CreditsFragment();
            case INSTRUCTIONS_FRAG_NAME:
                return new InstructionsFragment();
            default:
                throw new IllegalArgumentException("Couldn't find class of " + fragName);
        }
    }

}
