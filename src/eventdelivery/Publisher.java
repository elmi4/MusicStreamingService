package eventdelivery;

import media.ArtistName;
import media.Value;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Publisher extends Node
{
    public void getBrokerList(){}

    public Broker hashTopic(ArtistName artistName){
        try {
            String input = artistName.getArtistName();

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(input.getBytes());

            BigInteger hashkey = new BigInteger(1, messageDigest);

        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public void push(ArtistName artistName, Value value){}

    public void notifyFailure(Broker broker){}

    public static void main (String args[]){
        ArtistName name1 = new ArtistName("AAA");
        ArtistName name2 = new ArtistName("BBB");
        ArtistName name3 = new ArtistName("CCC");

    }
}
