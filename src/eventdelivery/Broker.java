package eventdelivery;

import media.ArtistName;

import java.util.List;
import java.util.Objects;

import java.io.*;
import java.net.*;

public final class Broker extends Node
{

    public List<Consumer> registeredUsers;
    public List<Publisher> registeredPublishers;

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

    public void initiateServer(){   //Runs in an infinite loop
        Socket newConnection = acceptConnection(4321);
        System.out.println("Consumer connected");
        System.out.println("Creating handler");
        ConsumerHandler handler = new ConsumerHandler(newConnection);   //Making a handler
        new Thread(handler).start();    //Passing it off to a different thread so other consumers can be dealt with
    }

    public static void main(String args[]){
        Broker test = new Broker();
        test.initiateServer();
    }
}
