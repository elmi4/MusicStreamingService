package eventdelivery;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.io.*;
import java.net.*;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import media.ArtistName;
import media.SongInfo;
import media.Value;

public final class Publisher extends Node {
    private HashMap<ArtistName, ArrayList<String>> artistsToSongs = new HashMap<>();            //Maps Artists to their songs
    private String dataFolder;
    private HashMap<BigInteger, ArrayList<String>> artistsToBroker = new HashMap<>();       //change happened here (rename)

    public Publisher(String ip, int port, String dataFolder) {
        this.ipAddr = ip;
        this.portNum = port;
        this.dataFolder = dataFolder;
    }

    public Publisher() {
    }

    @Override
    public void init() {                                 //initializes publisher's data
        File folder = new File(dataFolder);
        File[] mp3s = folder.listFiles();

        ArtistName artist;
        String title;

        try {
            for (File mp3 : mp3s) {

                String tracksDir = "files/Tracks/";
                String name = mp3.getName();
                Mp3File song = new Mp3File(tracksDir + name);
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
                }
            }

        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            e.printStackTrace();
        }

        /*for (ArtistName name : artistsToSongs.keySet()) {                //test
            String key = name.getArtistName().toUpperCase();
            System.out.println(key);
            for (String songTitleList : artistsToSongs.get(name)) {
                System.out.println(songTitleList);
            }
            System.out.println(" ");
        }*/
    }

    public void getBrokerList(String filePath) {
        try {
            File file = new File(filePath);
            Scanner reader = new Scanner(file);
            String line;
            while (reader.hasNextLine()) {
                line = reader.nextLine();
                String brokerIp = line;
                line = reader.nextLine();
                int brokerPort = Integer.parseInt(line);
                ConnectionInfo brokerNode = new ConnectionInfo(brokerIp, brokerPort);
                brokers.put(brokerNode, null);                                       //in the beginning hash keys are unknown (null)
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
            e.printStackTrace();
        }
    }       //read from file


    public void notifyFailure(Broker broker) {
    }

    public void initiate() {
        brokers = new HashMap<>();
        getBrokerList("BrokerCredentials.txt");


        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        //Phase 1 collect the ip and port hash values from all of the brokers

        for (ConnectionInfo br : brokers.keySet()) {
            try {
                requestSocket = new Socket(br.getIP(), br.getPort());
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                in = new ObjectInputStream(requestSocket.getInputStream());
                try {
                    out.writeObject("HashValue");
                    BigInteger hashedValue = (BigInteger) in.readObject();
                    brokers.replace(br, hashedValue);
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
        for(ConnectionInfo br : brokers.keySet()){
            try {
                requestSocket = new Socket(br.getIP(),br.getPort());
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                out.writeObject("SendingArtistArray");

                for(BigInteger key: artistsToBroker.keySet()) {
                    if(key.equals(brokers.get(br))) {               //to find connection info data for broker, using hashValue as identifier
                        out.writeObject("YourData");
                        ArrayList<String> artistList = artistsToBroker.get(key);
                        out.writeObject(artistList);
                        out.writeInt(portNum);              //Connection info, for broker to know who (publisher) the artists belong to
                        out.writeUTF(ipAddr);
                        out.flush();
                    } else {
                        out.writeObject("OtherBrokers'Data");
                        ArrayList<String> artistList = artistsToBroker.get(key);
                        out.writeObject(artistList);
                        for(ConnectionInfo b : brokers.keySet()){
                            if(key.equals(brokers.get(b))) {
                                out.writeInt(b.getPort());              //Connection info, to know who (broker) serves these artists
                                out.writeUTF(b.getIP());
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

        int size = brokers.values().size();
        BigInteger hashkeys[] = new BigInteger[size];

        int i = 0;
        for (ConnectionInfo broker : brokers.keySet()) {
            hashkeys[i++] = brokers.get(broker);
        }

        Arrays.sort(hashkeys);

        //TEST-> see order of hash keys
        /*System.out.println(" ORDERED KEYS ");
        for (BigInteger b : hashkeys) {
            System.out.println(b);
        }
        System.out.println(" ");*/
        //END TEST

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
                        /*System.out.println("this range");
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
                        //System.out.println("ARTIST TO SMALLEST BROKER ");
                        //System.out.println("KEY : " + artistHash + " ARTIST: " + artist.getArtistName() + "\n");
                        break;
                    }
                }
            } else {
                System.out.println("Problem in hashing Artist's name");
            }
        }
        for (BigInteger b : artistsToBroker.keySet()) {                //test
            System.out.println("Broker with hash: " + b);
            for (String songTitleList : artistsToBroker.get(b)) {
                System.out.println(songTitleList);
            }
            System.out.println(" ");
        }

    }

    public void acceptBrokerRequests(){

        try{
            ServerSocket server=new ServerSocket(portNum);
            System.out.println("Server Started ....");
            while(true){
                Socket clientSocket=server.accept();
                BrokerRequestHandler handler = new BrokerRequestHandler(clientSocket, dataFolder, artistsToSongs); //send  the request to a separate thread
                handler.start();
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }

    public static void main(String args[]) {
        Publisher test = new Publisher("127.0.0.1", 9999, "files/Tracks/");
        test.init();


        test.initiate();
        test.acceptBrokerRequests();
    }

}
