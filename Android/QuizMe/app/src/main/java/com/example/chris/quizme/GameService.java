package com.example.chris.quizme;

import android.os.AsyncTask;
import android.os.CountDownTimer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Chris on 12/11/2016.
 */

public class GameService extends AsyncTask<Void, Void, Void>{
    //facilitates the game logic and keeps it out of the controller
    final int NUM_QUESTIONS_PER_ROUND = 5;
    private ArrayList<Question> questions = new ArrayList<Question>();
    static ClientController ctrl = new ClientController();
    public static Question currentQuestion;
    private static CountDownTimer clock;
    public static int score = 0, questionCount = 0;
    public boolean isRunning = false;

    protected void onPreExecute(){
        try {
            new InitQuestions().execute().get();
        }catch(ExecutionException|InterruptedException e){
            e.printStackTrace();
        }finally {
            clock = new CountDownTimer(60000, 1000) {
                public void onTick(long millisUntilFinished) {
                    System.out.println("Setting timer text");
                    MainActivity.setTimerText((int) millisUntilFinished / 1000);
                    isRunning = true;
                }

                public void onFinish() {
                    clock.cancel();
                    System.out.println("Timer finished");
                    MainActivity.setTimerText(0);
                    isRunning = false;
                    ctrl.updateQuestion(questions.get(questionCount)); //TODO questions is not initializing
                    questionCount++;
                }
            }.start();
            clock.onFinish();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {

            if(!isRunning){
                System.out.println("Starting the clock");
                clock.start();
            }
            try{
                System.out.println("Sleepy time");
                Thread.sleep(1000);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        return null;
    }

    public class InitQuestions extends AsyncTask<Void, Void, Void>{

        protected void onPreExecute(){
            try {
                new ClientController.WriteToOutstream().execute(new String[]{"GETQUESTIONS"}).get();
            }catch (InterruptedException|ExecutionException e) {
                e.printStackTrace();
            }
        }
        protected Void doInBackground(Void... params){
            String[] response = new String[0];
            try {
                response = ClientController.getStringArrayFromObjectArray((Object[]) ctrl.in.readObject());
            } catch (ClassNotFoundException|IOException e) {
                e.printStackTrace();
            }
            response = response[1].split("---");
            for(int i = 0; i < response.length; i+=4) {
                questions.add(new Question(response[i], response[i+1], Integer.parseInt(response[i+2]), response[i+3]));
            }
            return null;
        }
        protected Void onPostExecute(){
            try{
                Thread.sleep(2000);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            return null;
        }
    }

    public static int calculateScore(int questionDifficulty, int numSecondsLeft){
        return numSecondsLeft * questionDifficulty;
    }

    public static void stopClock(){
        clock.onFinish();
    }

    public static void processAnswer(String answer){
        if(checkAnswer(answer)){
            stopClock();
            score += calculateScore(currentQuestion.getDifficulty(), MainActivity.getTimerText());
        }else{
            MainActivity.showToast("So close! Try again.");
        }
    }

    public static boolean checkAnswer(String playerAnswer){
        return (currentQuestion != null ? playerAnswer.toUpperCase().equals(currentQuestion.getAnswer().toUpperCase()) : false);
    }
}
