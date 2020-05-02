package com.dsproject.musicstreamingservice.assist.network;

import java.io.Serializable;
import java.util.Objects;

public final class ConnectionInfo implements Serializable
{
    private static final long serialVersionUID = -2194559770414809890L;

    private String IP;
    private int port;

    public ConnectionInfo(String IP, int port)
    {
        this.IP = IP;
        this.port = port;
    }

    public static ConnectionInfo of(String IP, int port)
    {
        return new ConnectionInfo(IP, port);
    }

    public String getIP()
    {
        return IP;
    }

    public int getPort()
    {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionInfo that = (ConnectionInfo) o;
        return port == that.port &&
                IP.equals(that.IP);
    }

    @Override
    public int hashCode() {
        return Objects.hash(IP, port);
    }
}
