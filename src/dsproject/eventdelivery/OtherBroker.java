package dsproject.eventdelivery;

import dsproject.assist.network.ConnectionInfo;

public class OtherBroker {
    public static void main(String args[]){
        Broker test2 = new Broker(ConnectionInfo.of("127.0.0.1", 4080));
        test2.serveRequests();
    }
}
