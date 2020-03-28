package elenaTest;

import java.util.*;
import java.io.*;
import java.net.*;

import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
import media.ArtistName;
import media.MusicFile;
import media.Value;

public final class Publisher {
    private HashMap<ArtistName, ArrayList<String>> map = new HashMap<>();            //Maps Artists to their songs
    private String dataFolder;

    public Publisher(String dataFolder) {
        this.dataFolder = dataFolder;
    }


    private void init() {                                 //initializes publisher's data
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

    public void informBroker(String address, int port) {          //sends all Artists' name to each broker through socket communication
        Socket socket = null;
        ObjectOutputStream out = null;

        // constructor to put ip address and port

        // establish a connection
        try {
            socket = new Socket(address, port);
            System.out.println("Connected");

            out = new ObjectOutputStream(socket.getOutputStream());           // sends output to the socket
        } catch (UnknownHostException u) {
            System.out.println(u);
        } catch (IOException i) {
            System.out.println(i);
        }

        int size = map.keySet().size();                                 //make static array which can be send through socket
        String artistList[] = new String[size];
        int i = 0;
        for (ArtistName artist : map.keySet()) {
            artistList[i++] = artist.getArtistName();
            System.out.println(artist.getArtistName());
        }

        try {
            out.writeObject(artistList);                  //variable key is send to broker
            out.flush();
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            try {
                out.close();
                socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }


    }


    public void getBrokerList() {

    }


    public void push(ArtistName artistName, Value value) {
    }

    //public void notifyFailure(Broker broker) {}


    private byte[] serializeChunk(final MusicFile chunk) {
        try (ByteArrayOutputStream baOut = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(baOut)) {
            out.writeObject(chunk);
            return baOut.toByteArray();
        } catch (IOException i) {
            i.printStackTrace();
        }

        return null;
    }


    /*public static void main(String args[]) {

        Publisher p = new Publisher("C:\\Users\\elena\\Desktop\\mp3_dataset");

        p.init();
        p.informBroker("127.0.0.1", 4323);


    }*/
}

