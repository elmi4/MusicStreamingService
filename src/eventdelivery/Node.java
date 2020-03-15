package eventdelivery;

import java.util.List;
import java.util.Objects;

public abstract class Node
{
    public List<Broker> brokers;

    public void init(int i){}

    public List<Broker> getBrokers(){return null;}

    public void connect(){}

    public void disconnect(){}

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
