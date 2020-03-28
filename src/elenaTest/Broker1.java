package elenaTest;

import media.ArtistName;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Broker1 {
    private String ip;
    private int portNumber;
    private BigInteger brokerHashKey;
    private HashMap<Integer, ArrayList<ArtistName>> map = new HashMap<Integer, ArrayList<ArtistName>>();        //maps publisher to artists served by broker,
    // (publisher is a defined from port#, change that later)
    private HashMap<String, BigInteger> allBrokersList = new HashMap<String, BigInteger>();                  //maps broker to hashkey,

    public Broker1(String ip, int portNumber) {
        this.ip = ip;
        this.portNumber = portNumber;
    }

    public void calculateKey() {
        try {
            String input = ip + String.valueOf(portNumber);

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(input.getBytes());

            brokerHashKey = new BigInteger(1, messageDigest);
            allBrokersList.put("this broker", brokerHashKey);
            System.out.println(brokerHashKey);


        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void receiveArtistList(int port) {
        //this method receives artist list from a publisher and adds it to the map
        Socket socket = null;
        ServerSocket server = null;
        ObjectInputStream in = null;

        try {
            server = new ServerSocket(port);
            System.out.println("Server started");

            System.out.println("Waiting for a client ...");

            socket = server.accept();
            System.out.println("Client accepted");

            in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

            try {
                String[] artistList = (String[])in.readObject();
                for(String artist: artistList){
                    hashTopic(artist, port);
                }
            } catch (Exception e) {
                System.out.println(e);
            }

            System.out.println("Closing connection");

            // close connection
            socket.close();
            in.close();
        } catch (IOException i) {
            System.out.println(i);
        }

        for (int pub : map.keySet()) {                      //test
            System.out.println("Publisher " + pub);
            ArrayList<ArtistName> list = map.get(pub);
            for (ArtistName name : list) {
                System.out.println(name.getArtistName());
            }
            System.out.println(" ");
        }
    }

    public void receiveOtherBrokersHashKey() {               //SOCKETS ή και οχι

    }

    public void hashTopic(String artist, int publisherId) {     //for now publisher id is port#
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(artist.getBytes());

            BigInteger artistHash = new BigInteger(1, messageDigest);
            System.out.println(artistHash + "       " + artist);                                                 //test
            if(artistHash.compareTo(brokerHashKey)<0 || artistHash.equals(brokerHashKey)){
                if (map.get(publisherId) == null) {
                    map.put(publisherId, new ArrayList<ArtistName>());
                }
                map.get(publisherId).add(new ArtistName(artist));
            }


        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String args[]) {
        Broker1 test = new Broker1("123.123.123.123", 4323);
        test.calculateKey();
        test.receiveArtistList(4323);
        test.receiveOtherBrokersHashKey();
    }
}
