package eventdelivery;

import assist.Utilities;
import media.ArtistName;
import media.MusicFile;
import media.SongInfo;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

public final class Broker extends Node {
    public ArrayList<Consumer> registeredUsers = new ArrayList<>();
    public ArrayList<Publisher> registeredPublishers = new ArrayList<>();
    private HashMap<ConnectionInfo, ArrayList<ArtistName>> publishersToArtists = new HashMap<>();           //HERE artist is a string NOT Artistname object!

    // give artist name and get the connection info of the broker that serves this artist
    private HashMap<ArtistName, ConnectionInfo> artistToBroker = new HashMap<>();

    public BigInteger hashedValue;

    public Broker(String ip, int port) {
        this.ipAddr = ip;
        this.portNum = port;
    }

    public Broker() {
    }

    public String getIp() {
        return ipAddr;
    }

    public int getPort() {
        return portNum;
    }

    public void calculateKey() {
        try {
            String input = ipAddr + String.valueOf(portNum);

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(input.getBytes());

            hashedValue = new BigInteger(1, messageDigest);

            System.out.println("Broker's Hash Hey: " + hashedValue);


        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket acceptConnection(int serverPort) {
        ServerSocket server = null;
        Socket conntection = null;
        try {
            server = new ServerSocket(serverPort);
            conntection = server.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return conntection;
    }

    public void notifyPublisher(String s) {
    }



    public void initiate() {
        ServerSocket providerSocket = null;
        Socket connection = null;
        ObjectInputStream in = null;
        ObjectOutputStream out = null;

        try {
            providerSocket = new ServerSocket(this.portNum, 10);
            try {

                while (true) {      //Infinite loop for accepting connections

                    connection = providerSocket.accept();       //Accepting a connection

                    out = new ObjectOutputStream(connection.getOutputStream());     //Creating input and output tools
                    in = new ObjectInputStream(connection.getInputStream());
                    System.out.println("Client connected.");

                    String request = (String) in.readObject();

                    //The first message that arrives will always be a string
                    //requesting further action
                    switch (request) {
                        case "HashValue":       //Send the ip and port hashed
                            this.calculateKey();
                            out.writeObject(hashedValue);
                            break;

                        case "SendingArtistArray":      //Receive an array list of artists that the broker can serve          //change happened here
                            String subcase = "";
                            while(!subcase.equals("over")) {
                                subcase = (String)in.readObject();
                                if (subcase.equals("YourData")) {
                                    ArrayList<String> songNameArray = (ArrayList<String>) in.readObject();
                                    ArrayList<ArtistName> artists = new ArrayList<>();
                                    for (String name : songNameArray) {
                                        ArtistName artist = new ArtistName(name);
                                        artistToBroker.put(artist, new ConnectionInfo(this.ipAddr,this.portNum));               //to add this broker's data to the list with all brokers' data
                                        artists.add(artist);
                                    }
                                    int publisherPort = in.readInt();
                                    String publisherIP = in.readUTF();
                                    ConnectionInfo info = new ConnectionInfo(publisherIP, publisherPort);
                                    publishersToArtists.put(info, artists);
                                } else if (subcase.equals("OtherBrokers'Data")) {
                                    ArrayList<String> artistArray = (ArrayList<String>) in.readObject();
                                    int brokerPort = in.readInt();
                                    String brokerIP = in.readUTF();
                                    ConnectionInfo brokerInfo = new ConnectionInfo(brokerIP, brokerPort);
                                    for (String name : artistArray) {
                                        ArtistName artist = new ArtistName(name);
                                        artistToBroker.put(artist, brokerInfo);
                                    }
                                }
                            }
                            print();        //test
                            break;

                        case "ListArtists":
                            System.out.println("Sending the list...");
                            artistToBroker.put(new ArtistName("testArtist"), new ConnectionInfo("127.0.0.1", 4040));
                            artistToBroker.put(new ArtistName("testArtist1"), new ConnectionInfo("127.0.0.1", 4040));

                            out.writeObject(artistToBroker);
                            break;

                        case "SongRequest": //Consumer notifies the broker that he is about to request a song
                            SongInfo msg = (SongInfo) in.readObject();
                            System.out.println("A request was made for the song: '" + msg.getSongName() + "'");
                            //pull MusicFiles from publisher
                            pull(msg);                  //elena test

                            //get buffer (byte[]) of MusicFile
                            //print result of Utilities.MD5HashChunk() to be able to validate it later on consumer
                            byte[] testBuffer = {1, 2, 3, 4, 5, 6, 7, 8, 9};
                            int chunkNumber = 1;
                            MusicFile mf = new MusicFile(msg.getSongName(), msg.getArtistName().getArtistName(),
                                    "", "", chunkNumber, 10, testBuffer);
                            System.out.println("(Broker) Hash of chunk " + chunkNumber + " : \n" + Utilities.MD5HashChunk(testBuffer));
                            out.writeObject(mf);
                            out.flush();
                            break;
                    }

                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (providerSocket != null) providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }
    public void pull(SongInfo songInfo) {            //oti exo grapsei einai gia na kano test tin push ston publisher

        try {
            Socket socket = new Socket("127.0.0.1", 9999);
            ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());

            outStream.writeObject(songInfo);
            System.out.println("INSIDE PULL");
            outStream.close();
            outStream.close();
            socket.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void print() {
        for (ConnectionInfo c : publishersToArtists.keySet()) {
            System.out.println("IN THIS BROKER FROM PUBLISHER WITH IP: " + c.getIP() + ", PORT: " + c.getPort());
            for (ArtistName artistList : publishersToArtists.get(c)) {
                System.out.println(artistList.getArtistName());
            }
            System.out.println(" ");
        }
        System.out.println("IN ALL BROKERS: ");
        for (ArtistName name : artistToBroker.keySet()) {
            System.out.println("Artist: " + name.getArtistName() + ", is served on broker with IP: " + artistToBroker.get(name).getIP() + ", PORT: " + artistToBroker.get(name).getPort());
        }
        System.out.println(" ");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Broker broker = (Broker) o;
        return Objects.equals(registeredUsers, broker.registeredUsers) &&
                Objects.equals(registeredPublishers, broker.registeredPublishers) &&
                Objects.equals(publishersToArtists, broker.publishersToArtists) &&
                Objects.equals(hashedValue, broker.hashedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), registeredUsers, registeredPublishers, publishersToArtists, hashedValue);
    }

    public static void main(String args[]) {
        Broker test = new Broker("127.0.0.1", 4040);

        //test.calculateKey();
       test.initiate();

        //test.print();
    }
}
