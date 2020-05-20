package com.dsproject.musicstreamingservice.domain.assist;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

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

    /**
     * Get IPv4 address from first non-localhost interface.
     * @return IPv4 String or throws {@code IllegalStateException} if it fails.
     */
    public static String getCurrentIP() throws IllegalStateException
    {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;
                        if (isIPv4) return sAddr;
                    }
                }
            }
        } catch (Exception ignored) {}

        System.err.println("WARNING - IPv4 of the machine cannot be retrieved automatically.");
        throw new IllegalStateException("No IPv4");
    }

    /**
     * Change the IP from localhost to the machine's IP, and use the method
     */
    public static String getCustomIP()
    {
        return "192.168.2.2";
    }

    /**
    * Get the IPv4 of the mobile automatically or if it fails, return {@code getCustomIP()}
     */
    public static String getMachineIP()
    {
        try{
            return getCurrentIP();
        }catch (IllegalStateException e){
            return getCustomIP();
        }
    }

}
