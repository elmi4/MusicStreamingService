package com.dsproject.musicstreamingservice.eventdelivery;

import com.dsproject.musicstreamingservice.Node;
import com.dsproject.musicstreamingservice.assist.Utilities;
import com.dsproject.musicstreamingservice.assist.network.ConnectionInfo;
import com.dsproject.musicstreamingservice.media.ArtistName;
import com.dsproject.musicstreamingservice.media.SongInfo;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;

public final class Broker extends Node
{
    private final Map<ConnectionInfo, List<ArtistName>> publishersToArtists = new HashMap<>();
    private final Map<ArtistName, ConnectionInfo> artistToBroker = new HashMap<>();
    private final BigInteger hashedValue = calculateKey();

    public Broker(ConnectionInfo connInfo) {
        super(connInfo);
    }


    @SuppressWarnings("unchecked")
    public void serveRequests()
    {
        try(ServerSocket providerSocket = new ServerSocket(super.getPort(), 10)) {

            while (true) { //Infinite loop for accepting connections

                Socket connection = providerSocket.accept();
                ObjectOutputStream out;
                ObjectInputStream in;
                try{
                    out = new ObjectOutputStream(connection.getOutputStream());
                    in  = new ObjectInputStream(connection.getInputStream());
                }catch(IOException e){
                    e.printStackTrace();
                    return;
                }
                new Thread(()->{

                    try {
                        System.out.println("Just connected to client "+connection.getInetAddress()+" "+connection.getPort());

                        String request = (String) in.readObject();
                        //The first message that arrives will always be a string
                        switch (request) {
                            case "HashValue": //(from Publisher) Send the ip and port hashed
                                out.writeObject(hashedValue);
                                break;

                            case "SendingArtistArray": //(from Publisher) Receive a list of artists that the broker can serve
                                String subcase = "";
                                while (!subcase.equals("over")) {
                                    subcase = (String) in.readObject();
                                    if (subcase.equals("YourData")) {
                                        List<String> songNameArray = (ArrayList<String>) in.readObject();
                                        List<ArtistName> artists = new ArrayList<>();
                                        for (String name : songNameArray) {
                                            ArtistName artist = new ArtistName(name);
                                            artistToBroker.put(artist, ConnectionInfo.of(super.getIP(), super.getPort()));
                                            artists.add(artist);
                                        }
                                        ConnectionInfo thisBrokerInfo = (ConnectionInfo)in.readObject();
                                        publishersToArtists.put(thisBrokerInfo, artists);
                                    } else if (subcase.equals("OtherBrokers'Data")) {
                                        List<String> artistArray = (ArrayList<String>) in.readObject();
                                        ConnectionInfo brokerInfoInfo = (ConnectionInfo)in.readObject();
                                        for (String name : artistArray) {
                                            ArtistName artist = new ArtistName(name);
                                            artistToBroker.put(artist, brokerInfoInfo);
                                        }
                                    }
                                }
                                break;

                            case "ListArtists": //Consumer artists request
                                System.out.println("Consumer's first connection.");
                                out.writeObject(artistToBroker);
                                break;

                            case "SongRequest": //Consumer notifies the broker that he is about to request a song
                                SongInfo msg;
                                try {
                                    msg = (SongInfo) in.readObject();
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                    return;
                                }

                                System.out.println("\nA request was made for the song: '" + msg.getSongName() + "'");

                                pull(msg, out);
                                break;
                        }
                    }catch(IOException | ClassNotFoundException e){
                        e.printStackTrace();
                    }

                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    // ---------------------------------   PRIVATE METHODS    ----------------------------------


    /**
     * Generate key by hashing the IP and Port
     */
    public BigInteger calculateKey()
    {
        try {
            String input = super.getIP() + String.valueOf(super.getPort());

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(input.getBytes());

            return new BigInteger(1, messageDigest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    private void pull(final SongInfo songInfo, final ObjectOutputStream consumerOut)
    {
        ArtistName wantedArtist = songInfo.getArtistName();

        //Get the connectionInfo of the publisher that serves the artist
        ConnectionInfo publisherInfo = null;
        for (Map.Entry<ConnectionInfo, List<ArtistName>> entry: publishersToArtists.entrySet()) {
            if((entry.getValue()).contains(wantedArtist)){
                publisherInfo = ConnectionInfo.of((entry.getKey()).getIP(),(entry.getKey()).getPort());
                break;
            }
        }
        if(publisherInfo == null){
            System.err.println("Couldn't find a publisher for this song.");
            return;
        }

        System.out.println("This is the corresponding publisher's connectionInfo: " + publisherInfo.getIP() + " " + publisherInfo.getPort());

        Socket finalPublisherSocket = super.connect(publisherInfo);
        try {
            ObjectOutputStream out = new ObjectOutputStream(finalPublisherSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(finalPublisherSocket.getInputStream());

            out.writeObject("SongRequest");
            out.writeObject(songInfo);

            //if first msg is a string, then it is the "artist not found" msg from the publisher
            Object answer = in.readObject();
            if(Utilities.isStringLiteral(answer)){
                consumerOut.writeObject(answer);
                return;
            }

            do {
                System.out.println("Just received and sent: " + answer);
                consumerOut.writeObject(answer);
                answer = in.readObject();
            }while (answer!=null);
            consumerOut.writeObject(null); //Notify consumer that all the packages have been sent
        }catch  (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

class BrokerEntry
{
    public static void main(String[] args)
    {
        Broker br = new Broker(ConnectionInfo.of("127.0.0.1", 4040));
        br.serveRequests();
    }
}

class BrokerEntry1
{
    public static void main(String[] args)
    {
        Broker br = new Broker(ConnectionInfo.of("127.0.0.1", 4080));
        br.serveRequests();
    }
}

class BrokerEntry2
{
    public static void main(String[] args)
    {
        Broker br = new Broker(ConnectionInfo.of("127.0.0.1", 4120));
        br.serveRequests();
    }
}