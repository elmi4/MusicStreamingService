package com.dsproject.musicstreamingservice.domain;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.dsproject.musicstreamingservice.BuildConfig;
import com.dsproject.musicstreamingservice.domain.assist.Utilities;
import com.dsproject.musicstreamingservice.domain.assist.io.IOHandler;
import com.dsproject.musicstreamingservice.domain.assist.network.ConnectionInfo;
import com.dsproject.musicstreamingservice.domain.media.ArtistName;
import com.dsproject.musicstreamingservice.domain.media.MusicFile;
import com.dsproject.musicstreamingservice.domain.media.SongInfo;
import com.dsproject.musicstreamingservice.ui.MainActivity;
import com.dsproject.musicstreamingservice.ui.managers.notifications.MyNotificationManager;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class Consumer
{
    private final Map<String, Map<Integer,Byte[]>> songNamesToChunksMap = Collections.synchronizedMap(new HashMap<>());
    private final Context context;
    private final Socket randomBrokerSocket;
    private final MyNotificationManager notificationManager;

    private Map<ArtistName, ConnectionInfo> artistToBroker;


    public enum RequestType{
        NONE,
        VALIDATE,
        DOWNLOAD_FULL_SONG,
        DOWNLOAD_CHUNKS,
        PLAY_CHUNKS;
    }


    public Consumer(final Socket randomBrokerSocket, final Context context)
    {
        this.context = context;
        this.notificationManager = MainActivity.getNotificationManager();

        if(randomBrokerSocket == null || randomBrokerSocket.isClosed()){
            throw new IllegalStateException("No valid connection provided");
        }
        this.randomBrokerSocket = randomBrokerSocket;
    }


    public void init()
    {
        artistToBroker = requestState();
        closeConnectionWithRandomBroker();
    }


    /**
     * Make song requests to appropriate brokers ASYNCHRONOUSLY
     * Define what happens with the received data by specifying the "requestType"
     */
    public void requestSongData(final String artistName, final String songName,
                                final RequestType requestType) throws IllegalStateException
    {

        if(artistToBroker == null) throw new IllegalStateException("Consumer was not initialized correctly.");

        ArtistName artistObj = ArtistName.of(artistName);

        if(!artistIsServed(artistObj)){
            showToastMsg("This artist isn't being served");
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
                showToastMsg((String)ob);
                return;
            }

            //Create the ID for the download notification and initialize the notification.
            MusicFile chunk = (MusicFile)ob;
            String notificationID = chunk.getTrackName()+"_"+chunk.getArtistName();
            if(requestType == RequestType.DOWNLOAD_FULL_SONG){
                notificationManager.makeAndShowProgressNotification(notificationID,
                        "Download ("+chunk.getTrackName()+")", "Download in progress...",
                        chunk.getTotalChunks(), false, null);
            }

            //Accept all the song chunks from broker until null is received (no more chunks)
            do{
                MusicFile mf = (MusicFile)ob;
                System.out.println("Got " + mf + "  Chunk: " +mf.getChunkNumber());

                if(requestType == RequestType.DOWNLOAD_FULL_SONG) {
                    notificationManager.updateProgressNotification(notificationID, mf.getTotalChunks(),
                            mf.getChunkNumber(), false);
                }

                Map<Integer, Byte[]> chunkNumToChunkData = new HashMap<>();
                chunkNumToChunkData.put(mf.getChunkNumber(), Utilities.toByteObjectArray(mf.getMusicFileExtract()));

                //Adding chunk. "songNamesToChunksMap" is synchronized
                Map<Integer,Byte[]> existing = songNamesToChunksMap.putIfAbsent(mf.getTrackName(), chunkNumToChunkData);
                if(existing != null){
                    existing.put(mf.getChunkNumber(), Utilities.toByteObjectArray(mf.getMusicFileExtract()));
                }

                if(requestType == RequestType.VALIDATE){
                    validateData(mf.getMusicFileExtract(), mf.getChunkNumber());
                }
                if(requestType == RequestType.DOWNLOAD_CHUNKS){
                    System.out.println("Downloading chunk:"+mf.getChunkNumber()+" ...");
                    IOHandler.writeFileInAppStorage(this.context, mf);
                }
            } while ((ob = in.readObject()) != null);

            if(requestType == RequestType.DOWNLOAD_FULL_SONG){
                System.out.println("Downloading the whole song '"+songName+"' ...");
                String path = IOHandler.writeFileInAppStorage(this.context, artistObj.getArtistName(),
                        songName, "FULL", reconstructSong(songName));

                notificationManager.completeProgressNotification(notificationID, "Download Complete",
                        makeDownloadPlayable(path));
                notificationManager.vibrate(600);
            }
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }


    /**
     * Get a list of all the song names of a particular artist from the eventDelivery.
     */
    @SuppressWarnings("unchecked")
    public List<String> requestSongsOfArtist(final String artistName) throws IllegalStateException
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

    public Map<ArtistName, ConnectionInfo> getArtistToBroker()
    {
        return artistToBroker;
    }

    // ---------------------------------   PRIVATE METHODS    ----------------------------------

    /**
     * (Initialization) Ask a random <b>Broker</b> to send you the existing artists and the brokers that serve them
     * */
    @SuppressWarnings("unchecked")
    private Map<ArtistName, ConnectionInfo> requestState()
    {
        Map<ArtistName, ConnectionInfo> outMap = null;
        try(ObjectOutputStream out = new ObjectOutputStream(randomBrokerSocket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(randomBrokerSocket.getInputStream()))
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

    private Socket connect(final ConnectionInfo connInfo)
    {
        Socket connection = null;
        try {
            connection = new Socket(connInfo.getIP(),connInfo.getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connection;
    }

    private void closeConnectionWithRandomBroker()
    {
        try{
            randomBrokerSocket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
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


    /**
     * Create a PendingIntent Object that will play the downloaded song when the notification is clicked.
     */
    private PendingIntent makeDownloadPlayable(final String path)
    {
        final Uri data = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID+".provider", new File(path));
        context.grantUriPermission(context.getPackageName(), data, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        final Intent intent = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(data, "audio/*")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void showToastMsg(final String msg)
    {
        ((Activity)context).runOnUiThread(() -> {
            final Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
            toast.show();
        });
    }
}
