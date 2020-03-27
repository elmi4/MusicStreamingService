package eventdelivery;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.*;
import java.net.*;
import media.ArtistName;
import media.Value;

public final class Publisher extends Node {


    public Publisher(String ip , int port){
        this.ipAddr = ip;
        this.portNum = port;
    }

    public Publisher(){}

    //Reads the credentials of all the brokers from a file
    //In the file are contained ip addresses and port numbers in sequence
    //so two lines (the first is the ip and the second the port)
    //indicate a separate broker node
    public void getBrokerList(String filePath){
        try {
            File file = new File(filePath);
            Scanner reader = new Scanner(file);
            String line;
            while(reader.hasNextLine()){
                line = reader.nextLine();
                String brokerIp = line;
                line = reader.nextLine();
                int brokerPort = Integer.parseInt(line);
                Broker brokerNode = new Broker(brokerIp,brokerPort);
                brokers.add(brokerNode);
            }
            reader.close();
        }
        catch (FileNotFoundException e){
            System.out.println("File not found.");
            e.printStackTrace();
        }
    }

    public Broker hashTopic(ArtistName artistName){return null;}

    public void push(ArtistName artistName, Value value){}

    public void notifyFailure(Broker broker){}

    public void initiate() {
        brokers = new ArrayList<>();
        getBrokerList("BrokerCredentials.txt");

        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        //Phase 1 collect the ip and port hash values from all of the brokers

        for(int i = 0; i <brokers.size(); i++){
            try {
                requestSocket = new Socket(brokers.get(i).ipAddr,brokers.get(i).portNum);
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                in = new ObjectInputStream(requestSocket.getInputStream());
                try {
                    out.writeObject("HashValue");
                    int hasedValue = (Integer)in.readObject();
                    System.out.println(hasedValue);
                    brokers.get(i).hashedValue = hasedValue;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }


            } catch (UnknownHostException unknownHost) {
                System.err.println("You are trying to connect to an unknown host!");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } finally {
                try {
                    in.close(); out.close();
                    requestSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }

        //Phase 2 calculate the which songs correspond to which brokers and transmit their names in an array list




    }
    public static void main(String args[]){
        Publisher test = new Publisher();
        test.initiate();

    }

}
