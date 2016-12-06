import java.io.*;
import java.net.Socket;

/**
 * Created by Chris on 12/3/2016.
 */
public class User {

    private String username;
    private String hashedPassword;
    private byte[] salt;
    private Socket client;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public User(Socket client){
        this.client = client;
        try {
            out = new ObjectOutputStream(this.client.getOutputStream());
            in = new ObjectInputStream(this.client.getInputStream());
        } catch (Exception e) {
            System.out.println("Failed to set up client I/O");
        }
    }

    public String[] readObject() throws ClassNotFoundException, IOException{
        String [] message;
        while(true) {
            message = (String[]) this.in.readObject();
            if (message != null) {
                return message;
            }
        }
    }

    public void writeObject(Object object) throws IOException{
        out.writeObject(object);
    }

    public String getUsername(){
        return this.username;
    }

    public void setPassword(String hashedPassword){
        this.hashedPassword = hashedPassword;
    }

    public void setSalt(byte[] salt){
        this.salt = salt;
    }

    public String getPassword(){
        return this.hashedPassword;
    }

    public byte[] getSalt(){
        return this.salt;
    }

    public void close(){
        try {
            out.close();
            in.close();
        }catch(Exception e){
            System.out.println("Resources failed to close :(");
        }
    }

}
