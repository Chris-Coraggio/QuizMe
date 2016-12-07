package com.example.chris.quizme;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Chris on 11/25/2016.
 */

public class ClientModel {

    private String username;
    private String currentGameKey;

    public ClientModel(){

    }

    public ClientModel(String username){
        this.username = username;
    }

    public String getUsername(){
        return this.username;
    }

    public void setCurrentGameKey(String key){
        this.currentGameKey = key;
    }

    public String getCurrentGameKey(){
        return this.currentGameKey;
    }

    public void setUsername(String username){
        this.username = username;
    }
}
