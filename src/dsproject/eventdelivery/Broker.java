package dsproject.eventdelivery;

import dsproject.Node;
import dsproject.assist.Utilities;
import dsproject.assist.network.ConnectionInfo;
import dsproject.media.ArtistName;
import dsproject.media.MusicFile;
import dsproject.media.SongInfo;

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
    private final HashMap<ConnectionInfo, ArrayList<ArtistName>> publishersToArtists = new HashMap<>();
    private final HashMap<ArtistName, ConnectionInfo> artistToBroker = new HashMap<>();

    private BigInteger hashedValue;

    public Broker(ConnectionInfo connInfo) {
        super(connInfo);
    }


    public void calculateKey() {
        try {
            String input = super.getIP() + String.valueOf(super.getPort());

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(input.getBytes());

            hashedValue = new BigInteger(1, messageDigest);

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


    @SuppressWarnings("unchecked")
    public void initiate() {
        ServerSocket providerSocket = null;

        try {
            providerSocket = new ServerSocket(super.getPort(), 10);

                while (true) { //Infinite loop for accepting connections

                Socket connection = providerSocket.accept();       //Accepting a connection

                new Thread(()->{
                    try(ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
                        ObjectInputStream  in  = new ObjectInputStream(connection.getInputStream())) {
                        System.out.println("Just connected to client " + connection.getInetAddress() + " " +  connection.getPort());

                        String request = null;
                        try {
                            request = (String) in.readObject();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                    //The first message that arrives will always be a string
                    switch (request) {
                        case "HashValue": //(from Publisher) Send the ip and port hashed
                            this.calculateKey();
                            out.writeObject(hashedValue);
                            break;

                            case "SendingArtistArray": //Receive an array list of artists that the broker can serve
                                String subcase = "";
                                while (!subcase.equals("over")) {
                                    try {
                                        subcase = (String) in.readObject();
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    if (subcase.equals("YourData")) {
                                        ArrayList<String> songNameArray = null;
                                        try {
                                            songNameArray = (ArrayList<String>) in.readObject();
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                        ArrayList<ArtistName> artists = new ArrayList<>();
                                        for (String name : songNameArray) {
                                            ArtistName artist = new ArtistName(name);
                                            artistToBroker.put(artist, ConnectionInfo.of(super.getIP(), super.getPort()));
                                            artists.add(artist);
                                        }
                                        int publisherPort = in.readInt();
                                        String publisherIP = in.readUTF();
                                        ConnectionInfo info = new ConnectionInfo(publisherIP, publisherPort);
                                        publishersToArtists.put(info, artists);
                                    } else if (subcase.equals("OtherBrokers'Data")) {
                                        ArrayList<String> artistArray = null;
                                        try {
                                            artistArray = (ArrayList<String>) in.readObject();
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                        int brokerPort = in.readInt();
                                        String brokerIP = in.readUTF();
                                        ConnectionInfo brokerInfo = new ConnectionInfo(brokerIP, brokerPort);
                                        for (String name : artistArray) {
                                            ArtistName artist = new ArtistName(name);
                                            artistToBroker.put(artist, brokerInfo);
                                        }
                                    }
                                }
                                break;

                        case "ListArtists": //Consumer artists request
                            System.out.println("Consumer's first connection.");
                            out.writeObject(artistToBroker);
                            break;

                            case "SongRequest": //Consumer notifies the broker that he is about to request a song
                                SongInfo msg = null;
                                try {
                                    msg = (SongInfo) in.readObject();
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }

                                System.out.println("\nA request was made for the song: '" + msg.getSongName() + "'");

                                //Create new thread, pull MusicFiles from Publisher and send them to Consumer
                                pull(msg, out);
                                break;
                        }
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                if (providerSocket != null) providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }


    public void pull(SongInfo songInfo, ObjectOutputStream consumerOut) {
        ArtistName wantedArtist = songInfo.getArtistName();

        //Finding the corresponding publisher and establishing a connection
        ConnectionInfo publisherInfo = null;
        for (Map.Entry entry: publishersToArtists.entrySet()) {
            if(((ArrayList)entry.getValue()).contains(wantedArtist)){
                publisherInfo = ConnectionInfo.of(
                        ((ConnectionInfo) entry.getKey()).getIP(),((ConnectionInfo) entry.getKey()).getPort());
                break;
            }
        }

        if (publisherInfo != null) {
            System.out.println("This is the corresponding publisher's connectionInfo: " + publisherInfo.getIP() + publisherInfo.getPort());

            Socket publisherSocket = null;
            try {
                publisherSocket = new Socket(publisherInfo.getIP(), publisherInfo.getPort());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

            //Pulling the requested song from the publisher
            Socket finalPublisherSocket = publisherSocket;
            try {
                ObjectOutputStream out = new ObjectOutputStream(finalPublisherSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(finalPublisherSocket.getInputStream());

                //Asking for the song
                out.writeObject("SongRequest");
                out.writeObject(songInfo);

                //Getting publisher's answer and passing it on to consumer
                try {
                    Object answer = in.readObject();

                    do {
                        System.out.println("Just received and sent: " + answer);
                        consumerOut.writeObject(answer);
                        answer = in.readObject();
                    }
                    while (answer!=null);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }catch  ( IOException e) {
                e.printStackTrace();
            }
        }
        else { System.err.println("Couldn't find a publisher for this song."); }
    }


    public static void main(String args[]) {
        Broker test = new Broker(ConnectionInfo.of("127.0.0.1", 4040));
        test.initiate();
    }
}
