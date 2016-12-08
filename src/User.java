import java.io.*;
import java.net.Socket;

/**
 * Created by Chris on 12/3/2016.
 */
public class User {

    private String username;
    private UserData userData;
    public Socket client;
    public ObjectOutputStream out;
    public ObjectInputStream in;
    private int score;
    private int questionCount;

    public User(Socket client){
        this.client = client;
        this.score = -1;
        try {
            out = new ObjectOutputStream(this.client.getOutputStream());
            in = new ObjectInputStream(this.client.getInputStream());
        } catch (Exception e) {
            System.out.println("Failed to set up client I/O");
        }
        new ListenForObject(this).start();
    }

    public void writeObject(Object object) throws IOException{
        out.writeObject(object);
    }

    public void updateUserData(UserData data){
        this.userData = data;
    }

    public UserData getUserData(){
        return this.userData;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getUsername(){
        return this.username;
    }

    public void updateScore(int score){
        this.score = score;
        System.out.println(String.format("UPDATING SCORE: %s, %s", this.getUsername(), Integer.toString(this.getScore
                ())));
    }

    public int getScore(){
        return this.score;
    }

    public int getQuestionCount(){
        return this.questionCount;
    }

    public void resetQuestionCount(){
        this.questionCount = 0;
    }

    public void incrementQuestionCount(){
        this.questionCount++;
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

class ListenForObject extends Thread{
    public User user;

    public ListenForObject(User u){
        this.user = u;
    }

    public void run(){
        while(true) {
            try {
                Object[] message = (Object[]) user.in.readObject();
                Server.processInput(message, user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
