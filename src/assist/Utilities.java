package assist;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//helper class with irrelevant useful functions
public abstract class Utilities
{
    public static Byte[] toByteObjectArray(final byte[] bytesPrim)
    {
        Byte[] Bytes = new Byte[bytesPrim.length];
        int i = 0;
        for (byte b : bytesPrim){
            Bytes[i++] = b;
        }

        return Bytes;
    }


    public static byte[] toBytePrimitiveArray(final Byte[] bytesObj)
    {
        byte[] bytes = new byte[bytesObj.length];
        for(int i = 0; i < bytesObj.length; i++){
            bytes[i] = bytesObj[i];
        }

        return bytes;
    }


    //calculate the hash of a serialized hash and compare it before and after sending it in the network
    public static String MD5HashChunk(final byte[] chunk)
    {
        StringBuilder hex = null;

        try {
            //calculate md5 hash
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(chunk);
            byte[] digest = md.digest();

            //transform it to hex String
            hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return (hex == null) ? null : hex.toString();
    }
}
