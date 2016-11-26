package com.example.chris.quizme;

/**
 * Created by Chris on 11/25/2016.
 */

public class ClientModel {

    private String username;

    public ClientModel(){

    }

    public ClientModel(String username){
        this.username = username;
    }

    public String getUsername(){
        return this.username;
    }

    public void setUsername(String username){
        this.username = username;
    }
}
