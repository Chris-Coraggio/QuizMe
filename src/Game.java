import java.util.ArrayList;

/**
 * Created by Chris on 12/3/2016.
 */
public class Game extends Thread {

    private int gameNumber;
    private ArrayList<User> participants = new ArrayList<User>();

    public Game(int index){
        this.gameNumber = index;
    }

    public void addParticipant(User u){
        System.out.println(String.format("Adding user %s to game %d", u.getUsername(), this.gameNumber));
        participants.add(u);
    }

    public String getGameNumber(){
        return Integer.toString(this.gameNumber);
    }

    public User getLeader(){
        return participants.get(0);
    }
}
