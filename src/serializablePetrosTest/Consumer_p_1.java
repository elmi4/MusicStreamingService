package serializablePetrosTest;

import assist.Utilities;
import media.MusicFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class Consumer_p_1
{
    private Map<String, Map<Integer,Byte[]>> songNamesToChunksMap = new HashMap<>();

    private void run()
    {
        //Establish connection with a random broker
        //Send request to broker

        //broker sends serialized MusicFile
        byte[] testSerializedChunk = {21,122,0,31,91}; //does the broker send us a byte[] ?

        //hash and verify that the package was sent correctly
        Utilities.MD5HashChunk(testSerializedChunk);

        //reconstruct the object
        MusicFile chunk = deserialize(testSerializedChunk);
        if(chunk == null){ //just for test
            System.out.println("Test data used, cannot add null to map");
            System.exit(1);
        }

        //put the object data in the map
        Map<Integer, Byte[]> songChunks = songNamesToChunksMap.get(chunk.getTrackName());
        if(songChunks == null){
            Map<Integer, Byte[]> numOfChunkToInfo = new HashMap<>();
            numOfChunkToInfo.put(chunk.getChunkNumber(), Utilities.toByteObjectArray(chunk.getMusicFileExtract()));
            songNamesToChunksMap.put(chunk.getTrackName(), numOfChunkToInfo);
        }else{
            songChunks.put(chunk.getChunkNumber(), Utilities.toByteObjectArray(chunk.getMusicFileExtract()));
        }
    }

    private MusicFile deserialize(final byte[] serializedChunk)
    {
        try(ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(serializedChunk))){
            return (MusicFile)in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            if(e.getClass() == StreamCorruptedException.class){ //just for test
                System.err.println("\nException thrown cause the bytes did not come from a real class");
            }else {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static void main(String[] args)
    {
        new Consumer_p_1().run();
    }
}
