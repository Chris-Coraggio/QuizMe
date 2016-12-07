package com.example.chris.quizme;
import android.text.TextUtils;
import android.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
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

    public boolean connectToServer(String ipAddress){ //returns whether or not connection was successful
       try {
            socket = new Socket(ipAddress, PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
           return true;
        }catch(IOException e){
            System.out.println(String.format(
                    "Failed to connect to server. Ensure it is running on %s:%s and try again",
                    ipAddress, PORT));
           return false;
        }
    }

    public boolean login(String username, String password){ //returns true if successful
        writeToOutstream(new String[]{"LOGIN", username, password});
        return validateResponse("LOGINSUCCESS");
    }

    public boolean register(String username, String password){ //returns true if successful
        writeToOutstream(new String[]{"REGISTER", username, password});
        return validateResponse("REGISTERSUCCESS");
    }

    public List<String> getAvailableGames(){
        writeToOutstream(new String[]{"GETAVAILABLEGAMES"});
        try {
            return Arrays.asList((String[])in.readObject());
        }catch(Exception e){
            return null;
        }
    }

    public boolean createNewGame(){
        writeToOutstream(new String[]{"STARTNEWGAME"});
        return validateResponse("NEWGAMESUCCESS");
    }

    public HashMap<String, String> refreshGamesInList(){
        try {
            writeToOutstream(new String[]{"GETAVAILABLEGAMES"});
            return (HashMap<String, String>) in.readObject();
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public boolean joinGame(String gameKey){
        writeToOutstream(new String[]{"JOINGAME"});
        return validateResponse("JOINGAMESUCCESS");
    }

    public boolean validateResponse(String successMessage){
        try {
            String[] response = (String[]) in.readObject();
            MainActivity.showToast(response[1]);
            if (response[0].equals(successMessage)) {
                return true;
            } else return false;
        }catch(Exception e){
            return false;
        }
    }

    public void writeToOutstream(Object[] message){
        try {
            if(message instanceof String[]){
                System.out.println(String.format("Sent to server: %s", TextUtils.join(" ", message)));
            }
            out.writeObject(message);
            out.flush();
        }catch(IOException e){
            System.out.println("Error writing to outstream");
        }
    }

    public void sleep(long millis){
        try{
            Thread.sleep(millis);
        }catch (InterruptedException e){
            System.out.println("Interrupted!");
        }
    }

    public String getUsername(){
        return model.getUsername();
    }
}
