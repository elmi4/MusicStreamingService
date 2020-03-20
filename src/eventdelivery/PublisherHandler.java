package eventdelivery;
import java.io.*;
import java.net.*;

public class PublisherHandler extends Thread {
    ObjectInputStream in;
    ObjectOutputStream out;

    public PublisherHandler(Socket connection) {
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
                out.writeObject("Hello");
                String request = (String)in.readObject();
                System.out.println("Message received");
                request += "Broker";
                System.out.println("Constructing response");
                out.writeObject(request);
                System.out.println("Response sent");
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
