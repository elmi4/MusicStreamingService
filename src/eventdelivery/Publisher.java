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
import media.Value;

public final class Publisher extends Node {
    private HashMap<ArtistName, ArrayList<String>> map = new HashMap<>();            //Maps Artists to their songs
    private String dataFolder;
    private HashMap<ConnectionInfo, ArrayList<String>> songsToBroker = new HashMap<>();

    public Publisher(String ip , int port, String dataFolder){
        this.ipAddr = ip;
        this.portNum = port;
        this.dataFolder = dataFolder;
    }

    public Publisher(){}

    @Override
    public void init() {                                 //initializes publisher's data
        File folder = new File(dataFolder);
        File[] mp3s = folder.listFiles();

        ArtistName artist;
        String title;

        try {
            for (File mp3 : mp3s) {

                String name = mp3.getName();
                Mp3File song = new Mp3File(name);
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
                    //System.out.println("Artist: " + artist.getArtistName() + " Title: " + title);
                    if (map.get(artist) == null) {
                        map.put(artist, new ArrayList<String>());
                    }
                    map.get(artist).add(title);
                }
            }

        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            System.err.println("Exception");
        }

        for (ArtistName name : map.keySet()) {                //test
            String key = name.getArtistName().toUpperCase();
            System.out.println(key);
            for (String songTitleList : map.get(name)) {
                System.out.println(songTitleList);
            }
            System.out.println(" ");
        }
    }
    //Reads the credentials of all the brokers from a file
    //In the file are contained ip addresses and port numbers in sequence
    //so two lines (the first is the ip and the second the port)
    //indicate a separate broker node
    public void getBrokerList(String filePath){
        try {
            File file = new File(filePath);
            Scanner reader = new Scanner(file);
            String line;
            while(reader.hasNextLine()){
                line = reader.nextLine();
                String brokerIp = line;
                line = reader.nextLine();
                int brokerPort = Integer.parseInt(line);
                ConnectionInfo brokerNode = new ConnectionInfo(brokerIp,brokerPort);
                brokers.put(brokerNode,null);                                       //in the beginning hash keys are unknown (null)
            }
            reader.close();
        }
        catch (FileNotFoundException e){
            System.out.println("File not found.");
            e.printStackTrace();
        }
    }

    public void push(ArtistName artistName, Value value){}

    public void notifyFailure(Broker broker){}

    public void initiate() {
        brokers = new HashMap<>();                    // allagi
        getBrokerList("C:\\Users\\elena\\Desktop\\BrokerCredentials.txt");

        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        //Phase 1 collect the ip and port hash values from all of the brokers

        for(ConnectionInfo br : brokers.keySet()){
            try {
                requestSocket = new Socket(br.getIP(),br.getPort());
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                in = new ObjectInputStream(requestSocket.getInputStream());
                try {
                    out.writeObject("HashValue");
                    BigInteger hashedValue = (BigInteger)in.readObject();
                    System.out.println(hashedValue);                                            //test
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
                    in.close(); out.close();
                    requestSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }

        //Phase 2 calculate the which songs correspond to which brokers and transmit their names in an array list

        //get hashkey list
        int size = brokers.values().size();                                 //make static array which can be send through socket
        BigInteger hashkeys[] = new BigInteger[size];
        int i = 0;
        for (ConnectionInfo broker : brokers.keySet()) {
            hashkeys[i++] = brokers.get(broker);
            System.out.println(brokers.get(broker));
        }
        Arrays.sort(hashkeys);

    }
    public void hashTopic(String artist) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(artist.getBytes());

            BigInteger artistHash = new BigInteger(1, messageDigest);
            System.out.println(artistHash + "       " + artist);                                                 //test
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }
    public static void main(String args[]){
        Publisher test = new Publisher();
        test.initiate();

    }

}
