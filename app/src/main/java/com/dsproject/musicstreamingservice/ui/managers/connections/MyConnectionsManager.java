package com.dsproject.musicstreamingservice.ui.managers.connections;

import android.content.Context;

import com.dsproject.musicstreamingservice.domain.assist.network.ConnectionInfo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

public abstract class MyConnectionsManager
{
    public static final String CANNOT_CONNECT_MSG = "Could not establish connection with broker. " +
            "Please update the connection information from the settings.";

    private static final int CONNECTION_TIMEOUT_MS = 2000;
    private static ConnectionInfo readBrokerCredentials;


    private MyConnectionsManager(){}

    public static Socket getConnectionWithABroker(final Context context)
    {
        if(readBrokerCredentials == null){
            loadInitialBrokerCredentials(context);
        }

        if(readBrokerCredentials == null || readBrokerCredentials.getIP().trim().equals("")){
            return null;
        }

        return connectWithBroker();
    }

    public static void updateBrokerCredentials(final ConnectionInfo connInfo)
    {
        readBrokerCredentials = connInfo;
    }

    private static Socket connectWithBroker()
    {
        Socket connection = new Socket();
        try {
            InetSocketAddress endPoint = new InetSocketAddress(readBrokerCredentials.getIP(), readBrokerCredentials.getPort());
            connection.connect(endPoint, CONNECTION_TIMEOUT_MS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return (connection.isConnected()) ? connection : null;
    }

    private static void loadInitialBrokerCredentials(final Context context)
    {
        try (FileInputStream fis = context.openFileInput("BrokerCredentials.txt")){
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String credentials;
            if((credentials = br.readLine())!=null){
                sb.append(credentials);
                credentials = sb.toString();
            }
            String ip = credentials.substring(0,credentials.indexOf('@'));
            int port = Integer.parseInt(credentials.substring(credentials.indexOf('@')+1));
            readBrokerCredentials = new ConnectionInfo(ip,port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
