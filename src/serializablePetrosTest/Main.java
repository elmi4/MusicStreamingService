package serializablePetrosTest;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
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

    public static void main(String[] args) throws InvalidDataException, IOException, UnsupportedTagException {
        new Main().run();
    }

    public void run() throws InvalidDataException, IOException, UnsupportedTagException {
        Mp3File mp3file = new Mp3File("files/Tracks/3-Doors-Down-Be-Like-That.mp3");
        MusicFile mf = new MusicFile(mp3file);

        serializeObject(mf);
        System.out.println(deserializeObject());
    }

    public void readSongs() throws InvalidDataException, IOException, UnsupportedTagException {
        File songsFolder = new File ("files/Tracks");
        File[] songs = songsFolder.listFiles();
        for (File song : songs) {
            if (song != null) {
                Mp3File mp3file = new Mp3File(song);
                MusicFile musicFile = new MusicFile(mp3file);
                //to complete when needed
            }
        }
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

