package eventdelivery;

import assist.Utilities;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import io.IOHandler;
import media.ArtistName;
import media.MusicFile;
import media.SongInfo;
import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Consumer extends Node
{
    private final Map<String, Map<Integer,Byte[]>> songNamesToChunksMap = new HashMap<>();

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

        new Thread(() -> {
            ArtistName artistObj = ArtistName.of(artistName);

            //get the broker that serves the artist you want
            ConnectionInfo appropriateBrokerInfo = artistToBroker.get(artistObj);
            Socket requestSocket = connect(appropriateBrokerInfo.getIP(),appropriateBrokerInfo.getPort());

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

            }catch(IOException | ClassNotFoundException e){
                e.printStackTrace();
            }

        }).start();
    }


    /**
     * Play a chunk of the mp3
     */
    public void playData()
    {

    }


    /**
     * Save a whole reconstructed song locally as an mp3
     */
    public void downloadSong()
    {

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
     * Show the hash of a chunk and see if it matches the hash before it was sent
     */
    private static void validateData(final byte[] chunk, final int chunkNumber)
    {
        System.out.println("(Consumer) Hash of chunk "+chunkNumber+" : \n"+ Utilities.MD5HashChunk(chunk));
    }


    /**
     * Put together all the chunks of a song
     */
    private void reconstructSong()
    {

    }


    private void showSavedSongInformation() //as debug validation
    {
        for(String songName : songNamesToChunksMap.keySet()){
            System.out.println("Name: '"+songName+"'  Data: ");
            Map<Integer, Byte[]> songData = songNamesToChunksMap.get(songName);
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
        c1.requestSongData("testArtist", "testSong");
        c1.requestSongData("testArtist1", "testSong1");
    }
}
