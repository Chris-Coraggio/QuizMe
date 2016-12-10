package com.example.chris.quizme;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.ArrayAdapter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Chris on 11/17/2016.
 */

public class ClientController {

    final int PORT = 50000;

    Socket socket;
    ObjectOutputStream out;
    ObjectInputStream in;

    ClientModel model = new ClientModel();

    public ClientController() {}

    public void connectToServer(String ipAddress) { //returns whether or not connection was successful
        new ServerConnection().execute(ipAddress);
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
        new RefreshGamesInList().execute();
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
                response = (String[]) in.readObject();
                return response[0].equals(successMessage[0]);
            }catch(Exception e){
                e.printStackTrace();
                return false;
            }
        }
        protected void onPostExecute(Boolean isValid){
            MainActivity.showToast(response[1]);
            if(isValid){
                MainActivity.showNext();
            }
        }
    }

    public class WriteToOutstream extends AsyncTask<Object, Void, Void> {

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

    public class RefreshGamesInList extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... args) {
            try {
                Object[] leaders = (Object[])in.readObject();
                return Arrays.copyOf(leaders, leaders.length, String[].class);
            }catch(Exception e){
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

    public String getUsername(){
        return model.getUsername();
    }
}
