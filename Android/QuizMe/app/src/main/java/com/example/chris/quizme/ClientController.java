package com.example.chris.quizme;
import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by Chris on 11/17/2016.
 */

public class ClientController {

    final int PORT = 50000;

    Socket socket;
    static ObjectOutputStream out;
    static ObjectInputStream in;

    ClientModel model = new ClientModel();

    public ClientController() {
    }

    public void connectToServer(String ipAddress) { //returns whether or not connection was successful
        new ServerConnection().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ipAddress);
    }

    public void login(String username, String password){ //returns true if successful
        new WriteToOutstream().execute(new String[]{"LOGIN", username, password});
        new ValidateResponse().execute("LOGINSUCCESS");
    }

    public void register(String username, String password){ //returns true if successful
        new WriteToOutstream().execute(new String[]{"REGISTER", username, password});
        new ValidateResponse().execute("REGISTERSUCCESS");
    }

    public void createNewGame(){
        new WriteToOutstream().execute(new String[]{"STARTNEWGAME"});
        new ValidateResponse().execute("NEWGAMESUCCESS");
    }
    
    public void joinGame(String leader){
        new WriteToOutstream().execute(new String[]{"JOINGAME", leader});
        new ValidateResponse().execute("JOINGAMESUCCESS");
    }

    public void refreshGamesInList(){
        new WriteToOutstream().execute(new String[]{"GETAVAILABLEGAMES"});
        new RefreshGamesInList().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void refreshPlayersInList(){
        new WriteToOutstream().execute(new String[]{"GETPLAYERS"});
        new RefreshPlayersInList().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void launchGame(){
        new WriteToOutstream().execute(new String[]{"LAUNCHGAME"});
        new ValidateResponse().execute("LAUNCHGAMESUCCESS");
    }

    public void updateQuestion(Question question){
        System.out.println("Got to controller");
        try {
            new UpdateQuestion().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, question);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void waitForGameLaunch(){
        new WaitForGameLaunch().execute();
    }

    public void submitAnswer(String answer){
        GameService.processAnswer(answer);
    }

    public void sendResults(){
        new WriteToOutstream().execute(new String[]{"SCORE", GameService.getScore()});
        GameService.questionCount = 0;
    }

    public void sendGameEnd(){
        new WriteToOutstream().execute(new String[]{"GAMEOVER"});
    }

    class ServerConnection extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... ipAddress) {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ipAddress[0], PORT), 10000);  //ten second timeout
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                System.out.println(String.format(
                        "Failed to connect to server. Ensure it is running on %s:%s and try again",
                        ipAddress[0], PORT));
            }
            return (out != null);
        }
        protected void onPostExecute(Boolean isValid){
            if(isValid){
                MainActivity.showNext();
            }
        }
    }

    public class ValidateResponse extends AsyncTask<String, Void, Boolean> {

        private String[] response;

        @Override
        protected Boolean doInBackground(String... successMessage) {
            try {
                response = getStringArrayFromObjectArray((Object[]) in.readObject());
                return response[0].equals(successMessage[0]);
            }catch(ClassNotFoundException|IOException e){
                e.printStackTrace();
                return false;
            }
        }
        protected void onPostExecute(Boolean isValid){
            MainActivity.showToast(response[1]);
            if(response[0].equals("REGISTERSUCCESS")){
                return;                 //if it's a new user, stay on login screen
            }
            if(isValid){
                MainActivity.showNext();
            }
            if(response[0].equals("NEWGAMESUCCESS")){
                MainActivity.showNext(); //if starting a game, skip the waiting screen
            }else if(response[0].equals("LAUNCHGAMESUCCESS")){
                new GameService().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); //start the game thread for leader
            }
        }
    }

    public static class WriteToOutstream extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... message) {
            try {
                if(message instanceof String[]){
                    System.out.println(String.format("Sent to server: %s", TextUtils.join(" ", message)));
                }
                out.writeObject(message);
                out.flush();
            }catch(IOException e){
                System.out.println("Error writing to outstream");
            }finally {
                return null;
            }
        }
    }

    public class WaitForGameLaunch extends AsyncTask<Void, Void, Boolean>{

        private String [] response;
        @Override
        protected Boolean doInBackground(Void... args) {
            try {
                response = getStringArrayFromObjectArray(((Object[]) in.readObject()));
                return response[0].equals("LAUNCHGAMESUCCESS");
            }catch(ClassNotFoundException | IOException e){
                return false;
            }
        }
        protected void onPostExecute(Boolean validLaunch){
            if(validLaunch) {
                MainActivity.showNext();
                MainActivity.showNext(); //skip leader waiting screen
                new GameService().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            if(response != null){
                MainActivity.showToast(response[1]);
            }
        }
    }

    public class RefreshGamesInList extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... args) {
            try {
                Object[] leaders = (Object[])in.readObject();
                return getStringArrayFromObjectArray(leaders);
            }catch(ClassNotFoundException|IOException e){
                e.printStackTrace();
                return null;
            }
        }
        protected void onPostExecute(String[] leaders){
            if(leaders != null) {
                MainActivity.updateGameList(leaders);
            }else{
                MainActivity.showToast("Still no games to show. Maybe you should start one!");
            }
        }
    }

    public class RefreshPlayersInList extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... args) {
            try {
                Object[] players = (Object[])in.readObject();
                return getStringArrayFromObjectArray(players);
            }catch(ClassNotFoundException|IOException e){
                e.printStackTrace();
                return null;
            }
        }
        protected void onPostExecute(String[] players){
            if(players != null) {
                MainActivity.updatePlayersList(players);
            }else{
                MainActivity.showToast("Nobody's here! Invite some friends.");
            }
        }
    }

    public class UpdateQuestion extends AsyncTask<Question, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Question... question) {
            Question q = question[0];
            if (q != null) {
                GameService.currentQuestion = q;
                GameService.questionCount++;
                return true;
            } else {
                MainActivity.showToast("Unable to display next question. Please try again.");
            }
            return false;
        }

        protected void onPostExecute(Boolean validQuestion){
            if(validQuestion) {
                Question question = GameService.currentQuestion;
                MainActivity.replaceQuestionOnScreen(question.getCategory(), question.getQuestion(), question.getScrambledAnswer());
            }
        }
    }

    public String getUsername(){
        return model.getUsername();
    }

    public static String[] getStringArrayFromObjectArray(Object[] objects){
        return Arrays.copyOf(objects, objects.length, String[].class);
    }
}
