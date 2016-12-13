package com.example.chris.quizme;

import android.os.AsyncTask;
import android.os.CountDownTimer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Chris on 12/11/2016.
 */

public class GameService extends AsyncTask<Void, Void, Void>{
    //facilitates the game logic and keeps it out of the controller
    static final int NUM_QUESTIONS_PER_ROUND = 5;
    private static ArrayList<Question> questions = new ArrayList<Question>();
    static ClientController ctrl = new ClientController();
    public static Question currentQuestion;
    private static CountDownTimer clock;
    public static int score = 0, questionCount = 0;
    public boolean isRunning = false;

    protected void onPreExecute(){
        try {
            new InitQuestions().execute().get();        //wait until we have questions
        }catch(ExecutionException|InterruptedException e){
            e.printStackTrace();
        }finally {
            clock = new CountDownTimer(60000, 1000) {
                public void onTick(long millisUntilFinished) {
                    MainActivity.setTimerText((int) millisUntilFinished / 1000);
                    isRunning = true;
                }
                public void onFinish() {
                    System.out.println("Timer finished");
                    MainActivity.setTimerText(0);
                    clock.cancel();
                    if(questionCount < NUM_QUESTIONS_PER_ROUND) {
                        ctrl.updateQuestion(questions.get(questionCount));
                    }else{
                        ctrl.sendResults();
                        MainActivity.showNext(); //waiting
                        new WaitForResults().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    isRunning = false;
                }
            }.start();
            clock.onFinish();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {

        while(true) {
            if (!isRunning) {
                System.out.println("Starting the clock");
                clock.start();
                isRunning = true;
            }
        }
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
    }

    public class WaitForResults extends AsyncTask<Void, Void, Boolean>{

        private List<String> response;
        @Override
        protected Boolean doInBackground(Void... args) {
            try {
                response = Arrays.asList(ctrl.getStringArrayFromObjectArray(((Object[]) ctrl.in.readObject())));
                return response.get(0).equals("RESULTS");
            }catch(ClassNotFoundException | IOException e){
                return false;
            }
        }
        protected void onPostExecute(Boolean validLaunch){
            if(validLaunch) {
                List<String> namesAndScores = new ArrayList<String>();
                MainActivity.showNext();
                for(int i=1; i < response.size() - 1; i += 2){
                    namesAndScores.add(response.get(i) + "                                                                           "
                            + response.get(i+1));
                }
                MainActivity.showResults(namesAndScores);
            }
        }
    }


    public static int calculateScore(int questionDifficulty, int numSecondsLeft){
        return numSecondsLeft * questionDifficulty;
    }

    public static void processAnswer(String answer){
        if(checkAnswer(answer)){
            score += calculateScore(currentQuestion.getDifficulty(), MainActivity.getTimerText());
            MainActivity.showToast("High five!");
            clock.onFinish();
        }else{
            MainActivity.showToast("So close! Try again.");
        }
    }

    public static boolean checkAnswer(String playerAnswer){
        return (currentQuestion != null ? playerAnswer.toUpperCase().equals(currentQuestion.getAnswer().toUpperCase()) : false);
    }

    public static String getScore(){
        return Integer.toString(score);
    }
}
