import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Chris on 12/3/2016.
 */
public class Game extends Thread {

    private ArrayList<User> participants = new ArrayList<User>();
    private ArrayList<Question> questions;
    final int NUM_QUESTIONS_PER_ROUND = 5;

    public Game(){}

    public void addParticipant(User u){
        System.out.println(String.format("Adding user %s to " + (participants.size() == 0 ? "new game" : this.getLeader().getUsername() + "'s game"), u.getUsername()));
        participants.add(u);
    }

    public User getLeader(){
        return participants.get(0);
    }

    public boolean findParticipant(User u){
        return participants.contains(u);
    }

    public String[] getParticipantUsernames(){
        String[] participantUsernames = new String[participants.size()];
        for(int i=0; i < participants.size(); i++){
            participantUsernames[i] = participants.get(i).getUsername();
        }
        return participantUsernames;
    }

    public void run(){

        initQuestions();
        for(User u: participants){
            Server.sendToClient(new String[]{"LAUNCHGAMESUCCESS", "May the odds be ever in your favor"}, u);
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
                System.out.println(questions);
                break;
            }
        }
    }

    public Question[] getQuestions(){
        //return (Question [])questions.toArray();
        //this does not work (ClassCastException), so trying another way...
        Question [] toReturn = new Question[questions.size()];
        for(int i=0; i < toReturn.length; i++){
            toReturn[i] = questions.get(i);
        }
        return toReturn;
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

    public void checkForResults(){
        if(allParticipantsHaveScores()){
            sendResultsToAllParticipants();
        }
    }

    public boolean allParticipantsHaveScores() {//ensure all participants have finished the game (have a score)
        boolean allUsersHaveScores = true;
        for (User u : participants) {
            if (u.getScore() == -1) {
                allUsersHaveScores = false;
            }
        }
        return allUsersHaveScores;
    }

    public void sendResultsToAllParticipants(){
        ArrayList<String> clientResultsMessage = new ArrayList<String>(); //user name, score in order
        clientResultsMessage.add("RESULTS");
        for (User user : orderByScore(participants)) {
            clientResultsMessage.add(user.getUsername());
            clientResultsMessage.add(Integer.toString(user.getScore()));
        }
        for (User u : participants) {
            Server.sendToClient(clientResultsMessage.toArray(), u);
            Server.updateDatabase(u);
        }
    }
}
