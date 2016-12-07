import java.io.*;
import java.net.Socket;

/**
 * Created by Chris on 12/3/2016.
 */
public class User {

    private String username;
    private UserData userData;
    private Socket client;
    public ObjectOutputStream out;
    private ObjectInputStream in;
    private int score;

    public User(Socket client){
        this.client = client;
        try {
            out = new ObjectOutputStream(this.client.getOutputStream());
            in = new ObjectInputStream(this.client.getInputStream());
        } catch (Exception e) {
            System.out.println("Failed to set up client I/O");
        }
    }

    public Object readObject() throws IOException, ClassNotFoundException{
        if(this.in.available() > 0){
            return this.in.readObject();
        }else{
            return null;
        }
    }

    public void writeObject(Object object) throws IOException{
        out.writeObject(object);
    }

    public void updateUserData(UserData data){
        this.userData = data;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getUsername(){
        return this.username;
    }

    public void updateScore(int score){
        this.score = score;
    }

    public int getScore(){
        return this.score;
    }

    public void incrementNumWins(){
        this.userData.incrementNumWins();
    }

    public int getNumWins(){
        return this.userData.getNumWins();
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
