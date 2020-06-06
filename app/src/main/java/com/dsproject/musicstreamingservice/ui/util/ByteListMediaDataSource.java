package com.dsproject.musicstreamingservice.ui.util;

import android.media.MediaDataSource;

import androidx.annotation.NonNull;

import java.util.List;


public class ByteListMediaDataSource extends MediaDataSource
{
    private List<Byte> data;

    public ByteListMediaDataSource(@NonNull final List<Byte> data)
    {
        this.data = data;
    }

    @Override
    public int readAt(final long pos, final byte[] buffer, final int offset, final int size)
    {
        //System.out.println("Size of internal: "+getSize()+"  ,  Requested position of internal: "+pos+"  ,  Offset: "+offset);
        //System.out.println("Wanted buffer size: "+ size);

        int count = 0;
        int sizeToBeRead = (int)Math.min(getSize(), pos+size);
        int counter = 0;
        for (int i = (int)pos; i < sizeToBeRead; i++) {
            buffer[offset + count++] = data.get(i);
            ++counter;
        }
        //System.out.println("Gave  :"+counter+"  bytes");

        return count;
    }

    @Override
    public long getSize()
    {
        return data.size();
    }

    @Override
    public void close()
    {
        data = null;
    }
}
