import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Chris on 12/3/2016.
 */
public class Game extends Thread {

    private int questionCount = 0, numTimesQuestionServed = 0;
    private String gameKey;
    private ArrayList<User> participants = new ArrayList<User>();
    private ArrayList<Question> questions;
    final int NUM_QUESTIONS_PER_ROUND = 5;

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

        initQuestions();
        for(User u: participants){
            Server.sendToClient(new String[]{"LAUNCHGAMESUCCESS"}, u);
        }
        while(true){
            System.out.println(questionCount + "\t" + numTimesQuestionServed);
            if(questionCount == questions.size() - 1 && numTimesQuestionServed == participants.size()){
                //ensure all participants have finished the game (have a score)
                System.out.println("CHECKING FOR SCORES");
                boolean allUsersHaveScores = true;
                for(User u: participants){
                    if(u.getScore() == -1){
                        allUsersHaveScores = false;
                    }
                }

                //wait 2 seconds so we aren't constantly pinging participants
                try {
                    Thread.sleep(2000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }

                //send results
                if(allUsersHaveScores) {
                    ArrayList<String> clientResultsMessage = new ArrayList<String>(); //user name, score in order
                    clientResultsMessage.add("RESULTS");
                    for (User user : orderByScore(participants)) {
                        clientResultsMessage.add(user.getUsername());
                        clientResultsMessage.add(Integer.toString(user.getScore()));
                    }
                    for (User u : participants) {
                        Server.sendToClient(clientResultsMessage.toArray(), u);
                    }
                    break;
                }
            }
        }
    }

    public void initQuestions(){
        questions = new ArrayList<Question>();
        ScrapeFromWeb sfw = new ScrapeFromWeb();

        //ensure we have the correct number of questions per round
        while(true){
            try {
                questions.add(sfw.getRandomQuestion());
            }catch(Exception e){
                e.printStackTrace();
            }
            if(questions.size() == NUM_QUESTIONS_PER_ROUND){
                break;
            }
        }
    }

    public Question getNextQuestion(){
        //TODO: what if a user asks for two questions in a row
        if(numTimesQuestionServed == participants.size()){
            questionCount++;
            numTimesQuestionServed = 0;
        }
        numTimesQuestionServed++;
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
