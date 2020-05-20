package com.dsproject.musicstreamingservice.domain;

import android.content.Context;
import android.util.Log;

import com.dsproject.musicstreamingservice.domain.assist.Utilities;
import com.dsproject.musicstreamingservice.domain.assist.io.IOHandler;
import com.dsproject.musicstreamingservice.domain.assist.network.ConnectionInfo;
import com.dsproject.musicstreamingservice.domain.media.ArtistName;
import com.dsproject.musicstreamingservice.domain.media.MusicFile;
import com.dsproject.musicstreamingservice.domain.media.SongInfo;

import java.io.*;
import java.net.*;
import java.util.*;

public final class Consumer extends Node
{
    private final Map<String, Map<Integer,Byte[]>> songNamesToChunksMap = Collections.synchronizedMap(new HashMap<>());

    private Map<ArtistName, ConnectionInfo> artistToBroker;

    public enum RequestType{
        NONE,
        VALIDATE,
        DOWNLOAD_FULL_SONG,
        DOWNLOAD_CHUNKS,
        PLAY_CHUNKS;
    }

    public Consumer(final ConnectionInfo connInfo, final Context context)
    {
        super(connInfo, context);
    }


    @Override
    public void init()
    {
        super.init();
        artistToBroker = requestState();
        //System.out.println("Requested artists from event delivery");
        Log.i("DEBUG", "init");
    }


    /**
     * Make song requests to appropriate brokers ASYNCHRONOUSLY
     * Define what happens with the received data by specifying the "requestType"
     */
    public void requestSongData(final String artistName, final String songName,
                                final RequestType requestType) throws IllegalStateException
    {
        Log.i("DEBUG", "requestSongData");
        if(artistToBroker == null) throw new IllegalStateException("Consumer was not initialized correctly.");

        //new Thread(() -> {

            ArtistName artistObj = ArtistName.of(artistName);
            if(!artistIsServed(artistObj)){
                System.out.println("The artist '"+artistObj.getArtistName()+"' is not being served.");
                return;
            }

            Socket requestSocket = getConnectionWithBrokerOfArtist(artistObj);
            assert requestSocket != null;

            try(ObjectOutputStream out = new ObjectOutputStream(requestSocket.getOutputStream());
                ObjectInputStream  in  = new ObjectInputStream(requestSocket.getInputStream()))
            {
                //Notify broker that you will make a song request
                out.writeObject("SongRequest");
                out.flush();
                out.writeObject(SongInfo.of(artistObj, songName));
                out.flush();
                System.out.println("Asked for song " + songName);

                //If error msg was sent, return. (song doesn't exist)
                Object ob = in.readObject();
                if(Utilities.isStringLiteral(ob)){
                    System.out.println(ob);
                    return;
                }

                //Accept all the song chunks from broker until null is received (no more chunks)
                do{
                    MusicFile mf = (MusicFile)ob;
                    System.out.println("Got " + mf + "  Chunk: " +mf.getChunkNumber());

                    Map<Integer, Byte[]> chunkNumToChunkData = new HashMap<>();
                    chunkNumToChunkData.put(mf.getChunkNumber(), Utilities.toByteObjectArray(mf.getMusicFileExtract()));

                    //Adding chunk. "songNamesToChunksMap" is synchronized
                    Map<Integer,Byte[]> existing = songNamesToChunksMap.putIfAbsent(mf.getTrackName(), chunkNumToChunkData);
                    if(existing != null){
                        existing.put(mf.getChunkNumber(), Utilities.toByteObjectArray(mf.getMusicFileExtract()));
                    }

                    if(requestType == RequestType.VALIDATE){
                        validateData(mf.getMusicFileExtract(), mf.getChunkNumber());
                    }else if(requestType == RequestType.DOWNLOAD_CHUNKS){
                        System.out.println("Downloading chunk:"+mf.getChunkNumber()+" ...");
                        IOHandler.writeToFile(mf);
                    }
                } while ((ob = in.readObject()) != null);

                if(requestType == RequestType.DOWNLOAD_FULL_SONG){
                    System.out.println("Downloading the whole song '"+songName+"' ...");
                    IOHandler.writeToFile(artistObj.getArtistName(), songName, "FULL", reconstructSong(songName));
                }
            }catch(IOException | ClassNotFoundException e){
                e.printStackTrace();
            }

        //}).start();
    }


    @SuppressWarnings("unchecked")
    public List<String> requestSongsOfArtist(String artistName) throws IllegalStateException
    {
        if (artistToBroker == null) throw new IllegalStateException("Consumer was not initialized correctly.");

        Socket requestSocket = getConnectionWithBrokerOfArtist(ArtistName.of(artistName));
        assert requestSocket != null;

        try (ObjectOutputStream out = new ObjectOutputStream(requestSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(requestSocket.getInputStream()))
        {
            //Notify broker that you will make a Songs Of Artist request
            out.writeObject("SongsOfArtistRequest");
            out.flush();
            out.writeObject(artistName);
            out.flush();
            System.out.println("Asked for the songs of " + artistName);

            return (ArrayList<String>)in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ---------------------------------   PRIVATE METHODS    ----------------------------------



    /**
     * (Initialization) Ask a random <b>Broker</b> to send you the existing artists and the brokers that serve them
     * */
    @SuppressWarnings("unchecked")
    private Map<ArtistName, ConnectionInfo> requestState()
    {
        Socket requestSocket = connect(getRandomBroker());

        Map<ArtistName, ConnectionInfo> outMap = null;
        try(ObjectOutputStream out = new ObjectOutputStream(requestSocket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(requestSocket.getInputStream()))
        {
            //Notify broker that he has to send the available artists and the brokers that serve them
            out.writeObject("ListArtists");
            out.flush();
            outMap = (Map<ArtistName, ConnectionInfo>)in.readObject();
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }

        return (outMap == null) ? null : Collections.unmodifiableMap(outMap);
    }


    private ConnectionInfo getRandomBroker() throws IllegalStateException
    {
        if(super.brokers == null || super.brokers.size() == 0){
            throw new IllegalStateException("Brokers not found");
        }

        return super.brokers.get(0);
    }


    /**
     * Finds the broker that serves the given artist, and returns a Socket connection with it
     */
    private Socket getConnectionWithBrokerOfArtist(final ArtistName artist)
    {
        Socket requestSocket = connect(artistToBroker.get(artist));
        if (requestSocket == null) {
            System.err.println("Could not connect to broker");
            return null;
        } else {
            System.out.println("Connected to broker " + artistToBroker.get(artist).getIP()
                    + " " + artistToBroker.get(artist).getPort());
            return requestSocket;
        }
    }


    /**
     * Show the hash of a chunk and see if it matches the hash before it was sent
     */
    private void validateData(final byte[] chunk, final int chunkNumber)
    {
        System.out.println("(Consumer) Hash of chunk "+chunkNumber+" : \n"+ Utilities.MD5HashChunk(chunk));
    }


    /**
     * Put together all the chunks of a song in the correct order
     */
    private byte[] reconstructSong(final String songName)
    {
        if(songNamesToChunksMap.get(songName) == null){
            return null;
        }

        Map<Integer,Byte[]> chunkMap = songNamesToChunksMap.get(songName);
        List<Integer> keys = new ArrayList<>(chunkMap.keySet());
        Collections.sort(keys);

        int keyLength = keys.size();
        Byte [] lastChunk = chunkMap.get(keys.get(keyLength - 1));
        int arrLength = ((keyLength - 1) * IOHandler.STANDARD_CHUNK_SIZE) + lastChunk.length;
        byte[] reconstructed = new byte[arrLength];

        int offset = 0;
        for(Integer i : keys){
            Byte[] currentChunk = chunkMap.get(i);
            int repetitions = chunkMap.get(i).length;
            for (int j = 0; j < repetitions; j++) {
                reconstructed[offset + j] = currentChunk[j];
            }
            offset += repetitions;
        }

        return reconstructed;
    }


    private boolean artistIsServed(final ArtistName artistName)
    {
        return artistToBroker.containsKey(artistName);
    }
}
