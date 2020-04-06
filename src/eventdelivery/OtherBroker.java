package eventdelivery;

public class OtherBroker {
    public static void main(String args[]){
        Broker test2 = new Broker("127.0.0.1", 4080);
        test2.initiate();
    }
}
