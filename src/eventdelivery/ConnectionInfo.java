package eventdelivery;


//use this class to save information used to identify Nodes
public final class ConnectionInfo
{
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
