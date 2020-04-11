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


public final class Publisher extends Node
{
    private final HashMap<SongInfo , String> songInfoToFilePath = new HashMap<>();
    private final HashMap<ArtistName, ArrayList<String>> artistsToSongs = new HashMap<>();
    private final HashMap<BigInteger, ArrayList<String>> artistsToBroker = new HashMap<>();

    private Map<ConnectionInfo, BigInteger> brokersConnToHash;
    private String ownFolder;

    public static final String GENERAL_DATA_FOLDER = "files/Tracks/";


    public Publisher(ConnectionInfo connInfo, String ownFolder)
    {
        super(connInfo);
        this.ownFolder = ownFolder;
    }


    /**
     * Reads mp3 directory and initializes artistsToSongs, songInfoToFilePath
     */
    @Override
    public void init() {
        super.init(); //read and get the brokers list

        File folder = new File(GENERAL_DATA_FOLDER+ownFolder);
        File[] mp3s = folder.listFiles();
        if(mp3s == null){
            System.err.println("The directory given for the mp3s doesn't exist.");
            return;
        }

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
                    artistsToSongs.computeIfAbsent(artist, k -> new ArrayList<>());
                    artistsToSongs.get(artist).add(title);
                    songInfoToFilePath.put(SongInfo.of(artist,title),mp3.getAbsolutePath());
                }
            }
        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            e.printStackTrace();
        }
    }


    public void initiate()
    {
        //Phase 1
        brokersConnToHash = getBrokerHashes();

        //Phase 2
        distributeArtistsToBrokers(); //can you make this return value for 'artistsToBroker'? No idea wtf is going on in that while() lol

        //Phase 3
        sendArtistsToBrokers();
    }


    public void serveBrokerRequests()
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
                        String request = (String)in.readObject();
                        if(request!=null && request.equals("SongRequest")){
                            SongInfo songRequest = (SongInfo)in.readObject();
                            push(songRequest,out);
                        }

                    }catch (IOException | ClassNotFoundException e){
                        e.printStackTrace();
                    }
                }).start();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }



    // ---------------------------------   PRIVATE METHODS    ----------------------------------



    /**
     * Collect the ip and port hash values from all of the brokers
     */
    private Map<ConnectionInfo, BigInteger> getBrokerHashes()
    {
        Map<ConnectionInfo, BigInteger> outMap = new HashMap<>();
        for (ConnectionInfo br : super.brokers) {
            try (Socket requestSocket   = new Socket(br.getIP(), br.getPort());
                 ObjectOutputStream out = new ObjectOutputStream(requestSocket.getOutputStream());
                 ObjectInputStream  in  = new ObjectInputStream(requestSocket.getInputStream()))
            {
                out.writeObject("HashValue");
                BigInteger hashedValue = (BigInteger) in.readObject();
                outMap.put(br, hashedValue);
            } catch (UnknownHostException e) {
                System.err.println("You are trying to connect to an unknown host!");
                e.printStackTrace();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return (outMap.size() == 0) ? null : outMap;
    }


    /**
     * Calculate which songs correspond to which brokers
     */
    private void distributeArtistsToBrokers()
    {
        int brokersCount = super.brokers.size();
        BigInteger[] hashKeys = new BigInteger[brokersCount];

        int i = 0;
        for (ConnectionInfo broker : super.brokers) {
            hashKeys[i++] = brokersConnToHash.get(broker);
        }

        Arrays.sort(hashKeys);

        for (ArtistName artist : artistsToSongs.keySet()) {
            BigInteger artistHash = this.hashTopic(artist.getArtistName());
            if(artistHash == null){
                System.out.println("Problem in hashing Artist's name");
                continue;
            }

            BigInteger one = new BigInteger("1");
            BigInteger low = new BigInteger("0");
            BigInteger high = hashKeys[0];
            int j = 0;
            while (true) {
                if (artistHash.compareTo(low) > 0 && artistHash.compareTo(high) < 0) {
                    artistsToBroker.computeIfAbsent(high, k -> new ArrayList<>());
                    artistsToBroker.get(high).add(artist.getArtistName());
                    /*System.out.println("this range");     //keeping it in case it is needed
                    System.out.println("LOW : " + low);
                    System.out.println("KEY : " + artistHash + " ARTIST: " + artist.getArtistName());
                    System.out.println("HIGH: " + high + "\n");*/
                    break;
                } else if (++j < brokersCount) {
                    low = high.add(one);
                    high = hashKeys[j];
                } else {
                    artistsToBroker.computeIfAbsent(hashKeys[0], k -> new ArrayList<>());
                    artistsToBroker.get(hashKeys[0]).add(artist.getArtistName());
                    //System.out.println("ARTIST TO SMALLEST BROKER ");     //keeping it in case it is needed
                    //System.out.println("KEY : " + artistHash + " ARTIST: " + artist.getArtistName() + "\n");
                    break;
                }
            }
        }
    }


    private BigInteger hashTopic(String artist)
    {
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


    /**
     * Send to brokers the artists that they serve themselves and all the other brokers too
     */
    private void sendArtistsToBrokers()
    {
        for(ConnectionInfo br : super.brokers){
            try (Socket requestSocket   = new Socket(br.getIP(), br.getPort());
                 ObjectOutputStream out = new ObjectOutputStream(requestSocket.getOutputStream()))
            {
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
                                out.writeObject(new ConnectionInfo(b.getIP(), b.getPort()));
                                out.flush();
                                break;
                            }
                        }
                    }
                }
                out.writeObject("over");
            } catch (UnknownHostException e) {
                e.printStackTrace();
                System.err.println("You are trying to connect to an unknown host!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Send requested data to broker
     */
    private void push(SongInfo info , ObjectOutputStream out)
    {
        try {
            String filePath = songInfoToFilePath.get(info);

            System.out.println("I was just asked for song " + info.getSongName());

            //Check if file already exists and consequently if the client is signed up
            if(filePath == null || !Files.exists(Paths.get(filePath))){
                out.writeObject("Song '"+info.getSongName()+"' isn't served by the publisher or it doesn't exist.");
                System.out.println("Song '"+info.getSongName()+"' isn't served by the publisher or it doesn't exist.");
                return;
            }

            Mp3File mp3;
            try {
                mp3 = new Mp3File(filePath);
            } catch (UnsupportedTagException | InvalidDataException e) {
                e.printStackTrace();
                return;
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
            out.writeObject(null);
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        Publisher test = new Publisher(ConnectionInfo.of("127.0.0.1", 9999), "folder1");
        test.init();
        test.initiate();
        test.serveBrokerRequests();

    }
}
