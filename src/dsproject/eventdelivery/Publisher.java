package dsproject.eventdelivery;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.io.*;
import java.net.*;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import dsproject.Node;
import dsproject.assist.io.IOHandler;
import dsproject.assist.network.ConnectionInfo;
import dsproject.media.ArtistName;
import dsproject.media.MusicFile;
import dsproject.media.SongInfo;

import javax.swing.*;


public final class Publisher extends Node
{
    private final HashMap<SongInfo , String> songInfoToFilePath = new HashMap<>();
    private final HashMap<ArtistName, ArrayList<String>> artistsToSongs = new HashMap<>();
    private final HashMap<ConnectionInfo, BigInteger> brokersConnToHash = new HashMap<>();
    private final HashMap<BigInteger, ArrayList<String>> artistsToBroker = new HashMap<>();

    private String ownFolder;

    public static final String GENERAL_DATA_FOLDER = "files/Tracks/";


    public Publisher(ConnectionInfo connInfo, String ownFolder) {
        super(connInfo);
        this.ownFolder = ownFolder;
    }


    @Override
    public void init() {                                 //initializes publisher's data
        super.init(); //read and get the brokers list

        File folder = new File(GENERAL_DATA_FOLDER+ownFolder);
        File[] mp3s = folder.listFiles();

        ArtistName artist;
        String title;

        try {
            for (File mp3 : mp3s) {
                Mp3File song = new Mp3File(mp3.getAbsolutePath());
                if (song.hasId3v1Tag()) {
                    title = song.getId3v1Tag().getTitle();
                    artist = new ArtistName(song.getId3v1Tag().getArtist());

                } else if (song.hasId3v2Tag()) {
                    title = song.getId3v2Tag().getTitle();
                    artist = new ArtistName(song.getId3v2Tag().getArtist());
                } else {
                    title = null;
                    artist = null;
                }

                if (title != null && artist.getArtistName() != null) {
                    if (artistsToSongs.get(artist) == null) {
                        artistsToSongs.put(artist, new ArrayList<String>());
                    }
                    artistsToSongs.get(artist).add(title);
                    songInfoToFilePath.put(SongInfo.of(artist,title),mp3.getAbsolutePath());
                }
            }

        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            e.printStackTrace();
        }
    }

    public void notifyFailure(Broker broker) {
    }

    public void initiate() {
        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        //Phase 1 collect the ip and port hash values from all of the brokers

        for (ConnectionInfo br : super.brokers) {
            try {
                requestSocket = new Socket(br.getIP(), br.getPort());
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                in = new ObjectInputStream(requestSocket.getInputStream());
                try {
                    out.writeObject("HashValue");
                    BigInteger hashedValue = (BigInteger) in.readObject();
                    brokersConnToHash.put(br, hashedValue);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }


            } catch (UnknownHostException unknownHost) {
                System.err.println("You are trying to connect to an unknown host!");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                    requestSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }

        //Phase 2 calculate which songs correspond to which brokers
        this.distributeArtistsToBrokers();

        //Phase 3 transmit their names in an array list
        for(ConnectionInfo br : super.brokers){
            try {
                requestSocket = new Socket(br.getIP(),br.getPort());
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                out.writeObject("SendingArtistArray");

                for(BigInteger key: artistsToBroker.keySet()) {
                    if(key.equals(brokersConnToHash.get(br))) {
                        out.writeObject("YourData");
                        ArrayList<String> artistList = artistsToBroker.get(key);
                        out.writeObject(artistList);
                        out.writeObject(super.connInfo);
                        out.flush();
                    } else {
                        out.writeObject("OtherBrokers'Data");
                        ArrayList<String> artistList = artistsToBroker.get(key);
                        out.writeObject(artistList);
                        for(ConnectionInfo b : super.brokers){
                            if(key.equals(brokersConnToHash.get(b))) {
                                out.writeObject(super.connInfo);
                                out.flush();
                                break;
                            }
                        }
                    }
                }
                out.writeObject("over");

            } catch (UnknownHostException unknownHost) {
                System.err.println("You are trying to connect to an unknown host!");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } finally {
                try {
                    out.close();
                    requestSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
        //Phase 3 transmit their names in an array list

    }

    public BigInteger hashTopic(String artist) {
        BigInteger artistHash;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(artist.getBytes());

            artistHash = new BigInteger(1, messageDigest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return artistHash;
    }

    public void distributeArtistsToBrokers() {

        int size = super.brokers.size();
        BigInteger hashkeys[] = new BigInteger[size];

        int i = 0;
        for (ConnectionInfo broker : super.brokers) {
            hashkeys[i++] = brokersConnToHash.get(broker);
        }

        Arrays.sort(hashkeys);

        for (ArtistName artist : artistsToSongs.keySet()) {
            BigInteger artistHash = this.hashTopic(artist.getArtistName());
            if (artistHash != null) {
                BigInteger one = new BigInteger("1");
                BigInteger low = new BigInteger("0");
                BigInteger high = hashkeys[0];
                int j = 0;
                while (true) {
                    if (artistHash.compareTo(low) == 1 && artistHash.compareTo(high) == -1) {
                        artistsToBroker.computeIfAbsent(high, k -> new ArrayList<>());
                        artistsToBroker.get(high).add(artist.getArtistName());
                        /*System.out.println("this range");     //keeping it in case it is needed
                        System.out.println("LOW : " + low);
                        System.out.println("KEY : " + artistHash + " ARTIST: " + artist.getArtistName());
                        System.out.println("HIGH: " + high + "\n");*/
                        break;
                    } else if (++j < size) {
                        low = high.add(one);
                        high = hashkeys[j];
                    } else {
                        artistsToBroker.computeIfAbsent(hashkeys[0], k -> new ArrayList<>());
                        artistsToBroker.get(hashkeys[0]).add(artist.getArtistName());
                        //System.out.println("ARTIST TO SMALLEST BROKER ");     //keeping it in case it is needed
                        //System.out.println("KEY : " + artistHash + " ARTIST: " + artist.getArtistName() + "\n");
                        break;
                    }
                }
            } else {
                System.out.println("Problem in hashing Artist's name");
            }
        }
    }

    public void push(SongInfo info , ObjectOutputStream out){
        try {

            String filePath = songInfoToFilePath.get(info);

            System.out.println("I was just asked for song " + info.getSongName());

            if (filePath != null && Files.exists(Paths.get(filePath))) {     //Check if file already exists and consequently if the client is signed up
                Mp3File mp3 = null;

                try {
                    mp3 = new Mp3File(filePath);
                } catch (UnsupportedTagException | InvalidDataException e) {
                    e.printStackTrace();
                }

                List<byte[]> rawAudio = IOHandler.readMp3(filePath);

                MusicFile originalMp3 = new MusicFile(mp3);

                String trackName = originalMp3.getTrackName();
                String artistName = originalMp3.getArtistName();
                String albumInfo = originalMp3.getAlbumInfo();
                String genre = originalMp3.getGenre();

                int chunkNum = 1;

                while (!rawAudio.isEmpty()) {

                    byte[] chunk = rawAudio.remove(0);
                    MusicFile mf = new MusicFile(trackName, artistName, albumInfo, genre, chunkNum, chunk);
                    out.writeObject(mf);
                    System.out.println("Sending chunk " + chunkNum + " of song " + trackName);
                    chunkNum++;

                }
            } else {
                out.writeObject("Song isn't served by the publisher or it doesn't exist.");
                System.out.println("Song isn't served by the publisher or it doesn't exist.");
            }
            out.writeObject(null);
        }catch (IOException e){
            e.printStackTrace();
        }


    }

    public void acceptBrokerRequests()
    {
        try{
            ServerSocket server = new ServerSocket(super.getPort());
            System.out.println("Connected to server.");
            while(true){
                Socket clientSocket=server.accept();
                new Thread(()->{
                    try(ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                        ObjectInputStream  in  = new ObjectInputStream(clientSocket.getInputStream()))
                    {
                        String request=null;
                        try {
                            request = (String)in.readObject();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        if(request!=null && request.equals("SongRequest")){
                            SongInfo songRequest = null;
                            try {
                                songRequest = (SongInfo)in.readObject();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }

                            push(songRequest,out);

                        }

                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }).start();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        Publisher test = new Publisher(ConnectionInfo.of("127.0.0.1", 9999), "");
        test.init();
        test.initiate();
        test.acceptBrokerRequests();
    }
}
