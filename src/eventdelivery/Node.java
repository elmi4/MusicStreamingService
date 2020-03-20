package eventdelivery;

import javax.imageio.IIOException;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Objects;

public abstract class Node
{
    public List<Broker> brokers;

    public void init(int i){}

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
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return brokers.equals(node.brokers);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(brokers);
    }
}
