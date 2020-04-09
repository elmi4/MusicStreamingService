package dsproject;

import dsproject.assist.Utilities;
import dsproject.assist.io.IOHandler;
import dsproject.assist.network.ConnectionInfo;
import dsproject.media.ArtistName;
import dsproject.media.MusicFile;
import dsproject.media.SongInfo;

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

    public Consumer(final ConnectionInfo connInfo)
    {
        super(connInfo);
    }


    @Override
    public void init()
    {
        super.init();
        artistToBroker = requestState();
    }


    /**
     * Make song requests to appropriate brokers ASYNCHRONOUSLY
     * Define what happens with the received data by specifying the "requestType"
     */
    public void requestSongData(final String artistName, final String songName,
                                final RequestType requestType) throws IllegalStateException
    {
        if(artistToBroker == null) throw new IllegalStateException("Consumer was not initialized correctly.");

        new Thread(() -> {

            ArtistName artistObj = ArtistName.of(artistName);
            if(!artistIsServed(artistObj)){
                System.out.println("The artist '"+artistObj.getArtistName()+"' is not being served.");
                return;
            }

            //get the broker that serves the artist you want
            Socket requestSocket = connect(artistToBroker.get(artistObj));
            if(requestSocket == null){
                System.err.println("Could not connect to broker");
                return;
            }

            try(ObjectOutputStream out = new ObjectOutputStream(requestSocket.getOutputStream());
                ObjectInputStream  in  = new ObjectInputStream(requestSocket.getInputStream()))
            {
                //Notify broker that you will make a song request
                out.writeObject("SongRequest");
                out.writeObject(SongInfo.of(artistObj, songName));

                //Accept all the song chunks from broker
                Object ob;
                while ((ob = in.readObject()) != null){
                    MusicFile mf = (MusicFile)ob;

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
                }

                if(requestType == RequestType.DOWNLOAD_FULL_SONG){
                    System.out.println("Downloading the whole song '"+songName+"' ...");
                    IOHandler.writeToFile(artistObj.getArtistName(), songName, "FULL", reconstructSong(songName));
                }
            }catch(IOException | ClassNotFoundException e){
                e.printStackTrace();
            }

        }).start();
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
        //for production, instead of * 9 use the STANDARD_CHUNK_SIZE of IOHandler
        int arrLength = ((keyLength - 1) * 9) + lastChunk.length;
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


class ConsumerEntry
{
    public static void main(String[] args)
    {
        Consumer c1 = new Consumer(ConnectionInfo.of("127.0.0.1", 4030));

        //initialize the consumer (request the state of the eventDelivery - get the artists and brokers that serve them)
        c1.init();

        // 2 ASYNCHRONOUS requests for songs
        c1.requestSongData("testArtist", "testSong", Consumer.RequestType.DOWNLOAD_CHUNKS);
        c1.requestSongData("testArtist1", "testSong1", Consumer.RequestType.DOWNLOAD_FULL_SONG);

        //(elena test)
        //c1.requestSongData("Kacey Smith", "Poison", Consumer.RequestType.NONE);
        //c1.requestSongData("testArtist1", "testSong1", Consumer.RequestType.NONE);
    }
}
