package eventdelivery;

import media.ArtistName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public final class Broker extends Node
{
    public ArrayList<Consumer> registeredUsers = new ArrayList<>();
    public ArrayList<Publisher> registeredPublishers = new ArrayList<>();
    public ArrayList<String> songNamesArray = new ArrayList<>();

    public int hashedValue;

    public Broker(String ip, int port){
        this.ipAddr = ip;
        this.portNum = port;
    }

    public Broker(){}

    public void calculateKeys(){}

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

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Broker broker = (Broker) o;
        return registeredUsers.equals(broker.registeredUsers) &&
                registeredPublishers.equals(broker.registeredPublishers);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), registeredUsers, registeredPublishers);
    }

    public void initiate(){
        ServerSocket providerSocket = null;
        Socket connection = null;
        ObjectInputStream in = null;
        ObjectOutputStream out = null ;

        try {
            providerSocket = new ServerSocket(4040, 10);
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
                            int hashValue = -1;     /*This number is bs i just dont know what to do here*/
                            out.writeObject(hashValue);
                            break;

                        case "SendingSongArrayStream":      //Receive an array list of songs that the broker can transmit
                            ArrayList<String> songNameArray = (ArrayList<String>)in.readObject();
                            songNamesArray.addAll(songNameArray);
                            break;

                        case "SongRequest":     //Customer notifies the broker that he is about to request a song
                            break;
                            /*
                             * Check song name array
                             * A handler must be made and passed on to a thread here

                             */


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

    public static void main(String args[]){
        Broker test = new Broker("127.0.0.1", 4040);
        test.initiate();
    }
}
