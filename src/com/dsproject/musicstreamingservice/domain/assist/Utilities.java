package com.dsproject.musicstreamingservice.domain.assist;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * helper class with irrelevant useful functions
 */
public abstract class Utilities
{
    /**
     * Convert a byte primitive array to an Object Byte[]
     * @param bytesPrim Primitive byte[]
     * @return Object Byte[]
     */
    public static Byte[] toByteObjectArray(final byte[] bytesPrim)
    {
        Byte[] Bytes = new Byte[bytesPrim.length];
        int i = 0;
        for (byte b : bytesPrim){
            Bytes[i++] = b;
        }

        return Bytes;
    }


    /**
     * Convert a Byte Object array to a primitive byte[].
     * @param bytesObj Array of Byte Objects.
     * @return Primitive byte[].
     */
    public static byte[] toBytePrimitiveArray(final Byte[] bytesObj)
    {
        byte[] bytes = new byte[bytesObj.length];
        for(int i = 0; i < bytesObj.length; i++){
            bytes[i] = bytesObj[i];
        }

        return bytes;
    }


    /**
     * Calculate the hash of a byte[] and return it as a hex String.
     * @param chunk byte[] to be hashed.
     * @return Hex String representation of the hash.
     */
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

    /**
     * Checks whether a String can be converted to a number (double).
     * @param strNum String to be checked
     * @return {@code false} if an exception occurs while converting.
     */
    public static boolean isNumeric(final String strNum)
    {
        if (strNum == null) {
            return false;
        }

        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * Checks whether an Object can be casted to a String.
     * @param ob Object to be checked.
     * @return {@code false} if an exception occurs while converting.
     */
    public static boolean isStringLiteral(final Object ob)
    {
        try{
            String s = (String)ob;
            return true;
        }catch (ClassCastException e){
            return false;
        }
    }

    /**
     * Get the IP of the current machine automatically.
     * @return The IP of the machine, or throw {@code IllegalStateException}.
     */
    public static String getCurrentIP()
    {
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            String ip = socket.getLocalAddress().getHostAddress();
            return ip;
        }catch (SocketException | UnknownHostException e){
            System.err.println("WARNING - the current IP of the machine cannot be retrieved");
            e.printStackTrace();
            throw new IllegalStateException("No IP");
        }
    }

    /**
     * Change the IP from localhost to the machine's IP, and use the method
     */
    public static String getCustomIP()
    {
        return "127.0.0.1";
    }

    public static String getMachineIP()
    {
        try{
            return getCurrentIP();
        }catch (IllegalStateException e){
            return getCustomIP();
        }
    }
}
