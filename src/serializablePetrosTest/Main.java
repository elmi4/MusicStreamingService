package serializablePetrosTest;

import media.MusicFile;

import java.io.*;

/**
 * NOTES ON SERIALIZATION
 *
 * -Serialization είναι η διαδικασία μετατροπής ενός αντικειμένου με τα δεδομένα του σε μια ακολουθία απο bytes
 * -Η μετατροπή γίνεται με την μέθοδο writeObject() ενός ObjectOutputStream αντικειμένου και το αποτέλεσμα
 * αποθηκεύεται σε ένα αρχείο (convention ειναι να έχει κατάληξη .ser)
 * -Η μετατροπή σε μορφή αντικειμένου ξανά, γίνεται με τη μέθοδο readObject() της κλάσης ObjectInputStream
 *
 * -Ο λόγος που χρησιμοποιείται η διαδικασία είναι
 * 1)για να αποθηκευτούν τα δεδομένα των αντικειμένων (persist the state of an object)
 * 2)για να μεταδοθούν δεδομένα μέσω network
 *
 * Δείτε σχετικό pdf για χρήση Serialization με sockets
 * */
public class Main
{
    private final String FILE_DIRECTORY = "files/";

    public static void main(String[] args)
    {
        new Main().run();
    }

    public void run()
    {
        MusicFile mf = new MusicFile("The Poet And The Pendulum", "NightWish",
                "Dark Passion Play","Symphonic Metal", new byte[]{0,0,1,1,0,0,1});

        serializeObject(mf);
        System.out.println(deserializeObject());
    }

    public void serializeObject(final MusicFile mf)
    {
        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_DIRECTORY+"musicfile.ser"))) {
            out.writeObject(mf);
            out.close();
            System.out.print("Serialized data is saved in musicfile.ser\n");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public MusicFile deserializeObject()
    {
        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE_DIRECTORY+"musicfile.ser"))) {
            return (MusicFile) in.readObject();
        } catch (IOException i) {
            i.printStackTrace();
            return null;
        } catch (ClassNotFoundException c) {
            System.out.println("MusicFile class not found");
            c.printStackTrace();
            return null;
        }
    }

}

