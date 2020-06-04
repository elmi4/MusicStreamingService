package com.dsproject.musicstreamingservice.domain.assist.io;


import android.content.Context;
import android.util.Log;

import com.dsproject.musicstreamingservice.domain.assist.network.ConnectionInfo;
import com.dsproject.musicstreamingservice.domain.media.MusicFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public abstract class IOHandler
{
    public static final int STANDARD_CHUNK_SIZE = 512 * 1024; //512KB


    public static void deleteFromStorage (final Context context, final String artistName,
                                          final String trackName , boolean downloadVersion)
    {
        Log.d("DEBUG", "DELETING FILE FROM INTERNAL STORAGE");
        String dataFolder = artistName + "___" + trackName + "/";
        String fileName = trackName + "__" + artistName + ".mp3";
        if(downloadVersion)fileName = "FULL_"+trackName + "__" + artistName + ".mp3";
        String filePath =  dataFolder+fileName;
        File songFile = new File(context.getExternalFilesDir(null), filePath);

        if(songFile.exists()){
            songFile.delete();
        }
    }


    public static String appendFileInAppStorage(final Context context, final MusicFile mf) throws IOException
    {
        return appendFileInAppStorage(context, mf.getArtistName(), mf.getTrackName(), mf.getMusicFileExtract());
    }

    public static String appendFileInAppStorage(final Context context, final String artistName,
                                                final String trackName,
                                                final byte[] data) throws IOException
    {
        Log.d("DEBUG", "WRITING ON INTERNAL STORAGE");
        String dataFolder = artistName + "___" + trackName + "/";
        File songParentFolder = new File(context.getExternalFilesDir(null), dataFolder);
        if(!songParentFolder.exists()){
            songParentFolder.mkdir();
        }

        String fileName = trackName + "__" + artistName + ".mp3";
        File songFile = new File(songParentFolder, fileName);
        System.out.println("Song saved at: "+ songFile.getAbsolutePath());

        try (FileOutputStream stream = new FileOutputStream(songFile,true)) {
            stream.write(data);
            return songFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

    }

    public static String writeFileInAppStorage(final Context context, final MusicFile mf) throws IOException
    {
        return writeFileInAppStorage(context, mf.getArtistName(), mf.getTrackName(),
                String.valueOf(mf.getChunkNumber()), mf.getMusicFileExtract());
    }


    public static String writeFileInAppStorage(final Context context, final String artistName,
                                               final String trackName, final String part,
                                               final byte[] data) throws IOException
    {
        Log.d("DEBUG", "WRITING ON INTERNAL STORAGE");
        String dataFolder = artistName + "___" + trackName + "/";
        File songParentFolder = new File(context.getExternalFilesDir(null), dataFolder);
        if(!songParentFolder.exists()){
            songParentFolder.mkdir();
        }

        String fileName = part + "_" + trackName + "__" + artistName + ".mp3";
        File songFile = new File(songParentFolder, fileName);
        System.out.println("Song saved at: "+ songFile.getAbsolutePath());

        try (FileOutputStream stream = new FileOutputStream(songFile)) {
            stream.write(data);
            return songFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }


    public static List<byte[]> readMp3(final String path)
    {
        List<byte[]> chunks = new ArrayList<>();

        File mp3File = new File(path);
        int remainingSize = (int) mp3File.length();
        int chunkCounter = 0;

        try (BufferedInputStream buf = new BufferedInputStream(new FileInputStream(mp3File))) {
            while (remainingSize > 0){
                int currentChunkSize = Math.min(remainingSize, STANDARD_CHUNK_SIZE);

                chunks.add(new byte[currentChunkSize]);
                buf.read(chunks.get(chunkCounter++));

                remainingSize -= currentChunkSize;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return chunks;
    }


    public static List<ConnectionInfo> readBrokerCredentials(String brokerCredentials , Context context)
    {
        List<ConnectionInfo> out = new ArrayList<>();

        context.getResources().getIdentifier("BrokerCredentials","raw",context.getPackageName());
        try (Scanner reader = new Scanner(context.getAssets().open(brokerCredentials))) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                if(line.isEmpty()) break;
                String brokerIp = line;
                line = reader.nextLine();
                int brokerPort = Integer.parseInt(line);
                out.add(new ConnectionInfo(brokerIp, brokerPort));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out;
    }
}
