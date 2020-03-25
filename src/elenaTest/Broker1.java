package elenaTest;

import media.ArtistName;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Broker1 {
    private String ip;
    private int portNumber;
    private BigInteger brokerHashKey;
    private HashMap<String,ArrayList<ArtistName>> map = new HashMap<String,ArrayList<ArtistName>>();        //maps publisher to artists served by broker,
    //(publisher is a string, change that later)
    private HashMap<String,BigInteger> allBrokersList = new HashMap<String, BigInteger>();                  //maps broker to hashkey,

    public Broker1 (String ip, int portNumber){
        this.ip= ip;
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


        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    public void receiveArtistList(){
        //this method receives artist list from a publisher and adds it to the map

        //test values:
        String artistname1 = "AAA";
        String artistname2 = "BBB";
        String artistname3 = "CCC";
        String artistname4 = "DDD";
        String artistname5 = "EEE";
        String artistname6 = "FFF";
        map.put("publisher1", new ArrayList<ArtistName>());
        map.put("publisher2", new ArrayList<ArtistName>());

        map.get("publisher1").add(new ArtistName(artistname1));             //artists 1,2,3 come from publisher 1
        map.get("publisher1").add(new ArtistName(artistname2));
        map.get("publisher1").add(new ArtistName(artistname3));
        map.get("publisher2").add(new ArtistName(artistname4));             //artists 4,5,6 come from publisher 2
        map.get("publisher2").add(new ArtistName(artistname5));
        map.get("publisher2").add(new ArtistName(artistname6));

        for (String pub: map.keySet()){
            System.out.println(pub);
            ArrayList<ArtistName> list = map.get(pub);
            for(ArtistName name: list) {
                System.out.println(name.getArtistName());
            }
            System.out.println(" ");
        }
    }
    public void receiveOtherBrokersHashKey(){               //SOCKETS
        //....

        allBrokersList.put("broker1",new BigInteger("6236953858533838745673820483269577160206417064359"));
        //allBrokersList[2] = new BigInteger("4729562475902758467673820483269577160206417064359");
        //allBrokersList[3] = new BigInteger("2457246555552763663654827467286487160206417064359");

        //Arrays.sort(allBrokersList);
    }
    public void hashTopic(){
        for (String pub: map.keySet()){
            System.out.println(pub);
            ArrayList<ArtistName> list = map.get(pub);
            for(ArtistName name: list) {
                //keep only artists within range
            }
            System.out.println(" ");
        }

    }
    public static void main(String args[]){
        Broker1 test = new Broker1("123.123.123.123", 4321);
        test.calculateKey();
        test.receiveArtistList();
        test.receiveOtherBrokersHashKey();
    }
}
