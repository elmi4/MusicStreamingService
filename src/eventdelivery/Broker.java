package eventdelivery;

import media.ArtistName;

import java.util.List;
import java.util.Objects;

public final class Broker extends Node
{
    public List<Consumer> registeredUsers;
    public List<Publisher> registeredPublishers;

    public void calculateKeys(){}

    public Publisher acceptConnection(Publisher publisher){return null;}

    public Consumer acceptConnection(Consumer consumer){return null;}

    public void notifyPublisher(String s){}

    public void pull(ArtistName artistName){}

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Broker broker = (Broker) o;
        return registeredUsers.equals(broker.registeredUsers) &&
                registeredPublishers.equals(broker.registeredPublishers);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), registeredUsers, registeredPublishers);
    }
}
