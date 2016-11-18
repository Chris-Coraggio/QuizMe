package com.example.chris.quizme;

import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Chris on 11/17/2016.
 */

public class ClientController {

    final String IP = "localhost";
    final int PORT = 50000;

    Socket socket;
    PrintWriter out;
    BufferedReader in;

    public ClientController(){
       // try {
            //socket = new Socket(IP, PORT);
            //out = new PrintWriter(socket.getOutputStream(), true);
            //in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
       // }catch(IOException e){
         //   System.out.println(String.format(
         //           "Failed to connect to server. Ensure it is running on %s:%s and try again",
         //           IP, PORT));
      //  }
    }

    public void login(String username, String password){ //returns true if successful
        //out.println(new String[]{"LOGIN", username, password});
    }

    public void register(String username, String password){
        //out.println(new String[]{"REGISTER", username, password});
    }
}
