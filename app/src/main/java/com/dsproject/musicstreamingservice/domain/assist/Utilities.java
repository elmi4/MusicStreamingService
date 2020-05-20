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


    /**
     * calculate the hash of a serialized hash and compare it before and after sending it in the network
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

    public static boolean isStringLiteral(final Object ob)
    {
        try{
            String s = (String)ob;
            return true;
        }catch (ClassCastException e){
            return false;
        }
    }

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

    public static String getMachineIP()
    {
        try{
            return getCurrentIP();
        }catch (IllegalStateException e){
            return getCustomIP();
        }
    }
    /**
     * Change the IP from localhost to the machine's IP, and use the method
     */
    public static String getCustomIP()
    {
        return "192.168.1.7";
    }
}
