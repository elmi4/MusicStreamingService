package eventdelivery;

import media.ArtistName;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class BrokerRequestHandler extends Thread {
    private Socket socket;
    private String dataFolder;
    private HashMap<ArtistName, ArrayList<String>> artistsToSongs;


    public BrokerRequestHandler(Socket s, String path, HashMap artistsToSongs) {
        socket = s;
        dataFolder = path;
        this.artistsToSongs = artistsToSongs;
    }

    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            String song = (String)in.readObject();
            System.out.println(song);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
