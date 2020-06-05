package com.dsproject.musicstreamingservice.ui.util;

import java.io.IOException;

public interface OnBufferInitializedEvent
{
    void prepareAndStartSong() throws IOException;
}
