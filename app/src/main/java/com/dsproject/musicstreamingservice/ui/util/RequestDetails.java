package com.dsproject.musicstreamingservice.ui.util;

import com.dsproject.musicstreamingservice.domain.Consumer;
import com.dsproject.musicstreamingservice.ui.fragments.PetrosPlayerFragment;

import java.util.List;

public class RequestDetails
{
    private final String artistName, songName;
    private final Consumer.RequestType type;
    private final List<Byte> buffer;
    private final PetrosPlayerFragment playerFragment;

    public RequestDetails(final String artistName, final String songName, final Consumer.RequestType type,
                          final List<Byte> buffer, final PetrosPlayerFragment playerFragment)
    {
        this.artistName = artistName;
        this.songName = songName;
        this.type = type;
        this.buffer = buffer;
        this.playerFragment = playerFragment;
    }

    public String getArtistName()
    {
        return artistName;
    }

    public String getSongName()
    {
        return songName;
    }

    public Consumer.RequestType getType()
    {
        return type;
    }

    public List<Byte> getBuffer()
    {
        return buffer;
    }

    public PetrosPlayerFragment getPlayerFragment()
    {
        return playerFragment;
    }
}
