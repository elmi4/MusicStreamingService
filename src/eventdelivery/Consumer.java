package eventdelivery;

import media.ArtistName;
import media.Value;
import java.io.*;
import java.net.*;

public final class Consumer extends Node{
    public void register(Broker broker, ArtistName artistName){}

    public void disconnect(Broker broker, ArtistName artistName){}

    public void playData(ArtistName artistName, Value value) {      //Crude proof of concept

        Socket requestSocket = connect("127.0.0.1",4321);
        ObjectOutputStream out = null;
        ObjectInputStream in = null;


        try {
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());
            try {
                String init = (String)in.readObject();
                System.out.println("Init : "+ init);
                out.writeObject("Song");
                System.out.println("request sent");
                String response = (String)in.readObject();
                System.out.println("This is what came back " + response);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }finally {
                try {
                    in.close(); out.close();
                    requestSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]){
        Consumer test = new Consumer();
        test.playData(null,null);

    }
}
