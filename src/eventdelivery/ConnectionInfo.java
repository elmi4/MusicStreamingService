package eventdelivery;


import java.io.Serializable;

//use this class to save information used to identify Nodes
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

    public String getIP()
    {
        return IP;
    }

    public int getPort()
    {
        return port;
    }
}
