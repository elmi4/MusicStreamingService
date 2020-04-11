package dsproject.eventdelivery;

import dsproject.assist.network.ConnectionInfo;

public class Publisher2 {
    public static void main(String args[]){
        Publisher test2 = new Publisher(ConnectionInfo.of("127.0.0.1", 8888), "folder2");
        test2.init();
        test2.initiate();
        test2.serveBrokerRequests();
    }
}
