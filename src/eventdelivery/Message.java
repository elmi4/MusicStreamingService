package eventdelivery;
import java.io.Serializable;

public class Message implements Serializable {
    String a,collation;
    public Message(String a) {
        this.a = a;
    }
    public String getA() {
        return a;
    }

    public void setB(String b){
        this.collation = this.a + b;
    }

    public String getCollation(){
        return collation;
    }
}
