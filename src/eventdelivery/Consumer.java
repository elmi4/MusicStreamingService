package eventdelivery;

import assist.Utilities;
import media.ArtistName;
import media.MusicFile;
import media.SongInfo;
import media.Value;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class Consumer extends Node
{
    private final Map<String, Map<Integer,Byte[]>> songNamesToChunksMap = new HashMap<>();

    private Map<ArtistName, ConnectionInfo> artistToBroker;


    void run()
    {
        artistToBroker = requestState();
        requestSongData("testArtist", "testSong");
    }


    /**
     * Ask a random <b>Broker</b> to send you the existing artists
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

        return outMap;
    }


    /**
     * Request the chunks of the song you want
     */
    private void requestSongData(final String artistName, final String songName)
    {
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
            while ((ob = in.readObject()) != null){ //should wait for broker to send null to stop listening for packages?
                MusicFile mf = (MusicFile)ob;
                validateData(mf.getMusicFileExtract(), mf.getChunkNumber());
            }
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }


    /**
     * Show the hash of a chunk and see if it matches the hash before it was sent
     */
    private static void validateData(final byte[] chunk, final int chunkNumber)
    {
        System.out.println("(Consumer) Hash of chunk "+chunkNumber+" : \n"+ Utilities.MD5HashChunk(chunk));
    }


    /**
     * Play a chunk of the mp3
     */
    public void playData()
    {

    }


    /**
     * Put together all the chunks of a song
     */
    private void reconstructSong()
    {

    }


    /**
     * Save a reconstructed song locally as an mp3
     */
    private void saveSong()
    {

    }
}


class ConsumerEntry
{
    public static void main(String[] args) {
        new Consumer().run();
    }
}
