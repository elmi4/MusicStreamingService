package eventdelivery;

import media.ArtistName;
import media.Value;

public final class Publisher extends Node
{
    public void getBrokerList(){}

    public Broker hashTopic(ArtistName artistName){return null;}

    public void push(ArtistName artistName, Value value){}

    public void notifyFailure(Broker broker){}
}
