package eventdelivery;

import javax.imageio.IIOException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public abstract class Node
{
    public String ipAddr;
    public int portNum;
    public Socket connection;

    public HashMap<ConnectionInfo, BigInteger> brokers;

    public void init(){}

    public List<Broker> getBrokers(){return null;}

    public Socket connect(String ip , int portNumber){
        Socket connection = null;
        try {
            connection = new Socket(ip,portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public void disconnect(Socket connection){
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateNodes(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return portNum == node.portNum &&
                Objects.equals(ipAddr, node.ipAddr) &&
                Objects.equals(connection, node.connection) &&
                Objects.equals(brokers, node.brokers);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(brokers);
    }
}
