package com.dsproject.musicstreamingservice.ui;

import android.media.MediaDataSource;

import androidx.annotation.NonNull;

import java.util.List;


public class ByteListMediaDataSource extends MediaDataSource
{
    private final List<Byte> data;

    public ByteListMediaDataSource(@NonNull final List<Byte> data)
    {
        this.data = data;
    }

    @Override
    public int readAt(final long pos, final byte[] buffer, final int offset, final int size)
    {
        int count = 0;
        for (int i = (int)pos; i < pos+size; i++) {
            buffer[offset + count++] = data.get(i);
        }

        return size;
    }

    @Override
    public long getSize()
    {
        return data.size();
    }

    @Override
    public void close()
    {
        //nothing to do
    }
}
