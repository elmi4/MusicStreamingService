package eventdelivery;
import java.io.*;
import java.net.*;



public class ConsumerHandler extends Thread {
    ObjectInputStream in = null;
    ObjectOutputStream out = null;

    public ConsumerHandler(Socket connection) {
        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            try {
                String initiate = "Hello";
                out.writeObject(initiate);
                System.out.println("Initializing coms");
                String request = (String)in.readObject();
                System.out.println("request is : "+ request);
                System.out.println("Sending response");
                out.writeObject(request + "Broker");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
