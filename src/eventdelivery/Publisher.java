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
    private HashMap<BigInteger, ArrayList<String>> songsToBroker = new HashMap<>();

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
        brokers = new HashMap<>();
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

        //Phase 2 calculate which songs correspond to which brokers
        this.distributeArtistsToBrokers();

        //Phase 3 transmit their names in an array list
        for(ConnectionInfo br : brokers.keySet()){
            try {
                requestSocket = new Socket(br.getIP(),br.getPort());
                out = new ObjectOutputStream(requestSocket.getOutputStream());

                for(BigInteger key: songsToBroker.keySet()) {
                    if(key.equals(brokers.get(br))) {
                        out.writeObject("SendingSongArrayStream");
                        ArrayList<String> artistList = songsToBroker.get(key);
                        out.writeObject(artistList);
                        out.writeInt(portNum);              //Connection info, for broker to know who the artists belong to
                        out.writeUTF(ipAddr);
                    }
                }

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
    public void distributeArtistsToBrokers(){
        //get hashkey list seperately
        int size = brokers.values().size();
        BigInteger hashkeys[] = new BigInteger[size+3];

        int i = 0;
        for (ConnectionInfo broker : brokers.keySet()) {
            hashkeys[i++] = brokers.get(broker);
            System.out.println(" INCOMING " + brokers.get(broker));         //test
        }
        //TEST-> add more values to broker hash list
        hashkeys[1]= hashTopic("156.647.225.685" + "2460");
        hashkeys[2]= hashTopic("466.472.256.855"+"6460");
        hashkeys[3]= hashTopic("656.647.225.685"+"7460");
        //

        Arrays.sort(hashkeys);

        //TEST-> see order of hash keys
        System.out.println(" ORDERED KEYS ");
        for (BigInteger b : hashkeys) {
            System.out.println(b);
        }
        System.out.println(" ");
        //

        for (ArtistName artist : map.keySet()) {
            BigInteger artistHash = this.hashTopic(artist.getArtistName());
            if(artistHash!=null) {
                BigInteger one= new BigInteger("1");
                BigInteger low = new BigInteger("0");
                BigInteger high = hashkeys[0];
                int j=0;
                while(true){
                    if(artistHash.compareTo(low)==1 && artistHash.compareTo(high)==-1){
                        songsToBroker.computeIfAbsent(high, k->new ArrayList<>());
                        songsToBroker.get(high).add(artist.getArtistName());
                        System.out.println("this range");
                        System.out.println("LOW : " + low);
                        System.out.println("KEY : " + artistHash+ " ARTIST: "+ artist.getArtistName());
                        System.out.println("HIGH: " + high +"\n");
                        break;
                    } else if(++j<size+3) {             //remove +3 after test
                        low = high.add(one);
                        high = hashkeys[j];
                    }else {
                        songsToBroker.computeIfAbsent(hashkeys[0], k->new ArrayList<>());
                        songsToBroker.get(hashkeys[0]).add(artist.getArtistName());
                        System.out.println("ARTIST TO SMALLEST BROKER ");
                        System.out.println("KEY : " + artistHash+ " ARTIST: "+ artist.getArtistName()+"\n");
                        break;
                    }
                }
            } else {
                System.out.println("Problem in hashing Artist's name");
            }
        }
        for (BigInteger b : songsToBroker.keySet()) {                //test
            System.out.println("Broker with hash: " + b);
            for (String songTitleList : songsToBroker.get(b)) {
                System.out.println(songTitleList);
            }
            System.out.println(" ");
        }

    }
    public static void main(String args[]){
        Publisher test = new Publisher("ip",9999,"C:\\Users\\elena\\Desktop\\mp3_dataset");
        test.init();
        test.initiate();


    }

}
