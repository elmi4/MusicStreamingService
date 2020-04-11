package dsproject;


import dsproject.assist.io.IOHandler;
import dsproject.assist.network.ConnectionInfo;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public abstract class Node
{
    protected ConnectionInfo connInfo;
    protected List<ConnectionInfo> brokers = new ArrayList<>();

    protected Node(final ConnectionInfo connInfo)
    {
        this.connInfo = connInfo;
    }

    protected String getIP()
    {
        return connInfo.getIP();
    }

    protected int getPort()
    {
        return connInfo.getPort();
    }

    protected void init()
    {
        this.brokers = IOHandler.readBrokerCredentials();
    }

    protected Socket connect(final ConnectionInfo connInfo){
        Socket connection = null;
        try {
            connection = new Socket(connInfo.getIP(),connInfo.getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connection;
    }

    protected void disconnect(final Socket connection){
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
