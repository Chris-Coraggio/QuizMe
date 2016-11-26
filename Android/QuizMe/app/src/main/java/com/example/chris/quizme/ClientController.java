package com.example.chris.quizme;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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

    public boolean login(String username, String password) throws IOException, ClassNotFoundException{ //returns true if successful
        out.writeObject(new String[]{"LOGIN", username, password});
        out.flush();

        String[] response = (String [])in.readObject();
        MainActivity.showToast(response[1]);
        if(response[0].equals("LOGINSUCCESS")){
            model.setUsername(response[1]);
            return true;
        }else{
            return false;
        }
    }

    public boolean register(String username, String password) throws IOException, ClassNotFoundException{ //returns true if successful
        out.writeObject(new String[]{"REGISTER", username, password});
        out.flush();

        String[] response = (String [])in.readObject();
        MainActivity.showToast(response[1]);
        if(response[0].equals("REGISTERSUCCESS")){
            return true;
        }
        else return false;
    }

    public void createNewGame() throws IOException{
        out.writeObject(new String[]{"NEWGAME", model.getUsername()});
    }

    public void joinGame(String hostUsername){

    }

    public void sleep(long millis){
        try{
            Thread.sleep(millis);
        }catch (InterruptedException e){
            System.out.println("Interrupted!");
        }
    }
}
