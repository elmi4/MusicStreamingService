package com.dsproject.musicstreamingservice;


import android.content.Context;
import android.os.AsyncTask;

import com.dsproject.musicstreamingservice.assist.io.IOHandler;
import com.dsproject.musicstreamingservice.assist.network.ConnectionInfo;


import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public abstract class Node
{
    protected ConnectionInfo connInfo;
    protected List<ConnectionInfo> brokers = new ArrayList<>();
    protected Context context;

    protected Node(final ConnectionInfo connInfo, final Context context)
    {
        this.connInfo = connInfo;
        this.context = context;
    }

    protected String getIP()
    {
        return connInfo.getIP();
    }

    protected int getPort()
    {
        return connInfo.getPort();
    }

    protected Context getContext()
    {
        return this.context;
    }

    protected void init()
    {
        this.brokers = IOHandler.readBrokerCredentials(this.context);
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
