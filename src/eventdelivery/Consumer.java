package eventdelivery;

import media.ArtistName;
import media.Value;

public final class Consumer extends Node
{
    public void register(Broker broker, ArtistName artistName){}

    public void disconnect(Broker broker, ArtistName artistName){}

    public void playData(ArtistName artistName, Value value){}
}
