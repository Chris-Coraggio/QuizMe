package com.example.chris.quizme;

import android.os.AsyncTask;
import android.os.CountDownTimer;

/**
 * Created by Chris on 12/11/2016.
 */

public class GameService extends AsyncTask<Void, Void, Void>{
    //facilitates the game logic and keeps it out of the controller
    final int NUM_QUESTIONS_PER_ROUND = 5;
    ClientController ctrl = new ClientController();
    private static Question currentQuestion;
    private static CountDownTimer clock;
    private static int score = 0, questionCount = 0;
    boolean isRunning = false;

    public GameService(){
        clock = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                MainActivity.setTimerText((int)millisUntilFinished / 1000);
                isRunning = true;
            }
            public void onFinish() {
                MainActivity.setTimerText(0);
                isRunning = false;
            }
        };
    }

    @Override
    protected Void doInBackground(Void... params) {
        while(questionCount < NUM_QUESTIONS_PER_ROUND){
            if(!isRunning){
                getNextQuestion();
                clock.start();
                questionCount++;
            }else{
                Thread.yield(); //let other threads go first
            }
        }
        return null;
    }

    public static int calculateScore(int questionDifficulty, int numSecondsLeft){
        return numSecondsLeft * questionDifficulty;
    }

    public static void stopClock(){
        clock.cancel();
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

    public void getNextQuestion(){
        new ClientController.WriteToOutstream().execute(new String[]{"GETNEXTQUESTION"});
        new GetNextQuestion().execute();
    }

    public class GetNextQuestion extends AsyncTask<Void, Question, Question> {

        @Override
        protected Question doInBackground(Void... args) {
            try {
                Object[] question = (Object[])ctrl.in.readObject();
                return (Question)question[1];
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }
        protected void onPostExecute(Question q){
            if(q != null) {
                MainActivity.replaceQuestionOnScreen(q.getCategory(), q.getQuestion(), q.getScrambledAnswer());
                currentQuestion = q;
            }else{
                MainActivity.showToast("Unable to display next question. Please try again.");
            }
        }
    }
}
