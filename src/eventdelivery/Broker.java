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

public final class Broker extends Node
{
    public ArrayList<Consumer> registeredUsers = new ArrayList<>();
    public ArrayList<Publisher> registeredPublishers = new ArrayList<>();
    private HashMap<ConnectionInfo, ArrayList<String>> publishersToArtists = new HashMap<>();

    // give artist name and get the connection info of the broker that serves this artist
    private Map<ArtistName, ConnectionInfo> artistToBroker = new HashMap<>();

    public BigInteger hashedValue;

    public Broker(String ip, int port){
        this.ipAddr = ip;
        this.portNum = port;
    }

    public Broker(){}

    public String getIp() {return ipAddr;}
    public int getPort() {return portNum;}

    public void calculateKey() {
        try {
            String input = ipAddr + String.valueOf(portNum);

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(input.getBytes());

            hashedValue = new BigInteger(1, messageDigest);

            System.out.println("Broker's Hash Hey: "+ hashedValue);


        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket acceptConnection(int serverPort){
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

    public void notifyPublisher(String s){}

    public void pull(ArtistName artistName){}

    public void initiate(){
        ServerSocket providerSocket = null;
        Socket connection = null;
        ObjectInputStream in = null;
        ObjectOutputStream out = null ;

        try {
            providerSocket = new ServerSocket(this.portNum, 10);
            try {

                while (true) {      //Infinite loop for accepting connections
                    connection = providerSocket.accept();       //Accepting a connection

                    out = new ObjectOutputStream(connection.getOutputStream());     //Creating input and output tools
                    in = new ObjectInputStream(connection.getInputStream());

                    System.out.println("Client connected.");
                    String request = (String)in.readObject();

                    //The first message that arrives will always be a string
                    //requesting further action
                    switch (request){
                        case "HashValue":       //Send the ip and port hashed
                            //BigInteger hashValue = new BigInteger("123456789");     /*This number is bs i just dont know what to do here*/
                            this.calculateKey();
                            out.writeObject(hashedValue);
                            break;

                        case "SendingSongArrayStream":      //Receive an array list of artists that the broker can serve
                            System.out.println("HERE");
                            ArrayList<String> songNameArray = (ArrayList<String>)in.readObject();
                            int port = in.readInt();
                            String ip = in.readUTF();
                            ConnectionInfo info = new ConnectionInfo(ip, port);
                            publishersToArtists.put(info, songNameArray);
                            break;

                        case "ListArtists":
                            System.out.println("Sending the list...");
                            artistToBroker.put(new ArtistName("testArtist"), new ConnectionInfo("127.0.0.1",4040));
                            out.writeObject(artistToBroker);
                            break;

                        case "SongRequest": //Customer notifies the broker that he is about to request a song
                            SongInfo msg = (SongInfo) in.readObject();
                            System.out.println("A request was made for the song: '"+msg.getSongName()+"'");
                            //pull MusicFiles from publisher

                            //get buffer (byte[]) of MusicFile
                            //print result of Utilities.MD5HashChunk() to be able to validate it later on consumer
                            byte[] testBuffer = {1,2,3,4,5,6,7,8,9};
                            int chunkNumber = 1;
                            MusicFile mf = new MusicFile(msg.getSongName(), msg.getArtistName().getArtistName(),
                                    "", "", chunkNumber, 10, testBuffer);
                            System.out.println("(Broker) Hash of chunk "+chunkNumber+" : \n"+ Utilities.MD5HashChunk(testBuffer));
                            out.writeObject(mf);
                            out.flush();
                            break;
                    }

                }

            }catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                if(in != null)in.close();
                if(out != null)out.close();
                if(providerSocket != null)providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }
    public void print (){
        for (ConnectionInfo c : publishersToArtists.keySet()) {                //test
            System.out.println("Publisher: " );
            for (String artistList : publishersToArtists.get(c)) {
                System.out.println(artistList);
            }
            System.out.println(" ");
        }
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

    public static void main(String args[]){
        Broker test = new Broker("127.0.0.1", 4040);
       // Broker test2 = new Broker("127.0.0.1", 4080);
        //test.calculateKey();
        test.initiate();
       // test2.initiate();
        test.print();
    }
}
