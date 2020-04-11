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
    public void requestSongData(final String artistName, final String songName,             //added the final String fileName for pull to work; Eleni
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
            }else{
                System.out.println("Connected to broker " + artistToBroker.get(artistObj).getIP()
                        +" "+ artistToBroker.get(artistObj).getPort());
            }

            try(ObjectOutputStream out = new ObjectOutputStream(requestSocket.getOutputStream());
                ObjectInputStream  in  = new ObjectInputStream(requestSocket.getInputStream()))
            {
                //Notify broker that you will make a song request
                out.writeObject("SongRequest");
                out.writeObject(SongInfo.of(artistObj, songName));
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


class ConsumerEntry
{
    public static void main(String[] args)
    {
        Consumer c1 = new Consumer(ConnectionInfo.of("127.0.0.1", 4030));

        //initialize the consumer (request the state of the eventDelivery - get the artists and brokers that serve them)
        c1.init();

        //make ASYNCHRONOUS requests
        c1.requestSongData("Alexander Nakarada", "Uberpunch", Consumer.RequestType.DOWNLOAD_FULL_SONG);
    }
}
