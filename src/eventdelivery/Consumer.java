package eventdelivery;

import assist.Utilities;

import io.IOHandler;
import media.ArtistName;
import media.MusicFile;
import media.SongInfo;
import java.io.*;
import java.net.*;
import java.util.*;

public final class Consumer extends Node
{
    private final Map<String, Map<Integer,Byte[]>> songNamesToChunksMap = new HashMap<>();
    private final List<String> unfinishedSongs = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Queue<Consumer.PendingRequest>> songNameToRequests = new HashMap<>();

    private Map<ArtistName, ConnectionInfo> artistToBroker;


    @Override
    public void init()
    {
        artistToBroker = requestState();
    }


    /**
     * Make song requests to appropriate brokers ASYNCHRONOUSLY
     */
    public void requestSongData(final String artistName, final String songName) throws IllegalStateException
    {
        if(artistToBroker == null) throw new IllegalStateException("Consumer was not initialized correctly.");
        unfinishedSongs.add(songName);

        new Thread(() -> {

            ArtistName artistObj = ArtistName.of(artistName);
            if(!artistIsServed(artistObj)){
                System.out.println("The artist '"+artistObj.getArtistName()+"' is not being served.");
                unfinishedSongs.remove(songName);
                return;
            }

            //get the broker that serves the artist you want
            ConnectionInfo appropriateBrokerInfo = artistToBroker.get(artistObj);
            Socket requestSocket = connect(appropriateBrokerInfo.getIP(),appropriateBrokerInfo.getPort());
            if(connection == null){
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

                    //get exclusive access to "songNamesToChunksMap" in order to add the chunk
                    synchronized (this) {
                        Map<Integer,Byte[]> existing = songNamesToChunksMap.putIfAbsent(mf.getTrackName(), chunkNumToChunkData);
                        if(existing != null){
                            existing.put(mf.getChunkNumber(), Utilities.toByteObjectArray(mf.getMusicFileExtract()));
                        }
                    }

                    validateData(mf.getMusicFileExtract(), mf.getChunkNumber());
                    //IOHandler.writeToFile(mf); //it works but the mp3 cannot play with test data
                }
                unfinishedSongs.remove(songName);
            }catch(IOException | ClassNotFoundException e){
                e.printStackTrace();
            }

        }).start();
    }


    /**
     * Play chunks of the mp3
     */
    public void playData()
    {

    }


    /**
     * Save a whole reconstructed song locally as an mp3
     */
    public void downloadSong(final String songName)
    {
        if(unfinishedSongs.contains(songName)){
            makeRequest(songName, PendingRequest.Type.DOWNLOAD_FULL);
        }

        if(songNamesToChunksMap.get(songName) == null){
            System.out.println("Song '"+songName+"' doesn't exist and cannot be downloaded");
        }
    }



    // ---------------------------------   PRIVATE METHODS    ----------------------------------



    /**
     * (Initialization) Ask a random <b>Broker</b> to send you the existing artists and the brokers that serve them
     * */
    @SuppressWarnings("unchecked")
    private Map<ArtistName, ConnectionInfo> requestState()
    {
        //get a random broker - how??
        Socket requestSocket = connect("127.0.0.1",4040);

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


    /**
     * Add a request to the queue of requests of a song if it doesn't exist
     */
    private void makeRequest(final String referringSong, final PendingRequest.Type requestType)
    {
        PendingRequest request = new PendingRequest(requestType);

        Queue<Consumer.PendingRequest> requestQueue = new ArrayDeque<>();
        requestQueue.add(request);
        Queue<Consumer.PendingRequest> existing = songNameToRequests.putIfAbsent(referringSong, requestQueue);
        if(existing != null){
            if(existing.contains(request)) return;
            existing.add(request);
        }
    }


    private void servePendingRequests()
    {

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
    public byte[] reconstructSong(final String songName)
    {
        if(songNamesToChunksMap.get(songName) == null){
            return null;
        }

        Map<Integer,Byte[]> chunkMap = songNamesToChunksMap.get(songName);
        List<Integer> keys = new ArrayList<>(chunkMap.keySet());
        Collections.sort(keys);

        int keyLength = keys.size();
        Byte [] lastChunk = chunkMap.get(keys.get(keyLength - 1));
        //for testing instead of STANDARD_CHUNK_SIZE use the test chunk size of broker (9 in my case)
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


    /**
     * Class used to create and queue requests about a song, while it is being retrieved / not yet retrieved
     * and serve them when it is available
     */
    public static class PendingRequest
    {
        public Type type;

        public enum Type{
            DOWNLOAD_FULL,
            DOWNLOAD_CHUNK,
            PLAY_CHUNK
        }

        public PendingRequest(Type t)
        {
            this.type = t;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PendingRequest that = (PendingRequest) o;
            return type == that.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type);
        }
    }
}


class ConsumerEntry
{
    public static void main(String[] args)
    {
        Consumer c1 = new Consumer();

        //initialize the consumer (request the state of the eventDelivery - get the artists and brokers that serve them)
        c1.init();

        // 2 ASYNCHRONOUS requests for songs
        c1.requestSongData("testArtist1", "testSong1");

        System.out.println(22);
        //c1.requestSongData("Kacey Smith", "Poison");
        //c1.requestSongData("testArtist1", "testSong1");
    }
}
