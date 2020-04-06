package eventdelivery;

import media.ArtistName;
import media.SongInfo;

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
            SongInfo songInfo = (SongInfo) in.readObject();
            System.out.println("INSIDE THREAD");
            System.out.println(songInfo.getSongName()+ " - "+songInfo.getArtistName().getArtistName());

            //search if the requested song exists in data folder
            ArrayList<String> songs = artistsToSongs.get(songInfo.getArtistName());
            boolean exists = false;
            for (String title : songs) {
                if (title.equals(songInfo.getSongName())) {
                    exists = true;
                    System.out.println("ok");
                }
            }
            //if "true" do push
            //else send appropriate message
            if(exists){
                out.writeObject("Sending song chunks");
                push();
            }
            else {
                out.writeObject("No such song");
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private void push(){}
}
