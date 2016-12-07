import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Chris on 12/3/2016.
 */
public class Game extends Thread {

    private int questionCount = 0, numQuestionsServed = 0;
    private String gameKey;
    private ArrayList<User> participants = new ArrayList<User>();
    private ArrayList<Question> questions;

    public Game(String key){
        this.gameKey = key;
    }

    public void addParticipant(User u){
        System.out.println(String.format("Adding user %s to game %s", u.getUsername(), this.gameKey));
        participants.add(u);
    }

    public String getGameKey(){
        return this.gameKey;
    }

    public User getLeader(){
        return participants.get(0);
    }

    public boolean findParticipant(User u){
        return participants.contains(u);
    }

    public void run(){
        for(User u: participants){
            Server.sendToClient(new String[]{"LAUNCHGAMESUCCESS"}, u);
        }
        while(true){
            if(numQuestionsServed == participants.size() * questions.size()){
                ArrayList<Object> clientResultsMessage = new ArrayList<Object>(); //user name, score in order
                clientResultsMessage.add("RESULTS");
                for(User user: orderByScore(participants)){
                    clientResultsMessage.add(user.getUsername());
                    clientResultsMessage.add(user.getScore());
                }
                for(User u: participants){
                    Server.sendToClient(clientResultsMessage.toArray(), u);
                }
            }
        }
    }

    public void initQuestions(){
        questions = new ArrayList<Question>();
        ScrapeFromWeb sfw = new ScrapeFromWeb();
        for(int i=0; i < 5; i++){
            try {
                questions.add(sfw.getRandomQuestion());
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public Question getNextQuestion(){
        if(numQuestionsServed > participants.size()) questionCount++;
        numQuestionsServed++;
        return questions.get(questionCount);
    }

    public ArrayList<User> orderByScore(ArrayList<User> users){
        ArrayList<User> copyOfUsers = new ArrayList<User>(users);
        Collections.sort(copyOfUsers, new Comparator<User>() {
            @Override
            public int compare(User user2, User user1){
                return(user1.getScore() - user2.getScore());
            }
        });
        copyOfUsers.get(0).incrementNumWins();
        return copyOfUsers;
    }

}
