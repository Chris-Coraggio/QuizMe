package com.example.chris.quizme;

import android.content.Context;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final ClientController ctrl = new ClientController();
    private static ViewFlipper flipper;
    private static Context context;
    private static ListView gameList, playersList, resultsList;
    private static String selectedGame = "";
    private static TextView timer;
    private static TextView category, question, scrambledAnswer, congratsMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        flipper =  (ViewFlipper) findViewById(R.id.ViewFlipper);
        new AnimationUtils();
        flipper.setAnimation(AnimationUtils.makeInAnimation
                (getBaseContext(), true));

        //IF YOU EVER WANT TO FORCE THINGS TO RUN ON THE MAIN THREAD
        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

        context = getApplicationContext();

        Button connect = (Button) findViewById(R.id.connect_button);
        connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //do this when connect button is clicked
                ctrl.connectToServer(((EditText) findViewById(R.id.ip_address_field)).getText().toString());
            }
        });


        Button login = (Button) findViewById(R.id.login_button);
        login.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //do this when login button is clicked
                String username = ((EditText) findViewById(R.id.username_field)).getText().toString();
                String password = ((EditText) findViewById(R.id.password_field)).getText().toString();
                ctrl.login(username, password);
            }
        });

        Button register = (Button) findViewById(R.id.register_button);
        register.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //do this when register button is clicked
                String username = ((EditText) findViewById(R.id.username_field)).getText().toString();
                String password = ((EditText) findViewById(R.id.password_field)).getText().toString();
                ctrl.register(username, password);

        }});

        gameList = (ListView) findViewById(R.id.games_list);
        playersList = (ListView) findViewById(R.id.players_list);

        Button refreshGames = (Button) findViewById(R.id.refresh_games_button);
        refreshGames.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
            //do this when create new game button is clicked
            ctrl.refreshGamesInList();
            }
        });

        Button createGame = (Button) findViewById(R.id.create_game_button);
        createGame.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
            //do this when create new game button is clicked
            ctrl.createNewGame();
            }
        });

        Button joinGame = (Button) findViewById(R.id.join_game_button);
        joinGame.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
            //do this when create join game button is clicked
                ctrl.joinGame(selectedGame);
                ctrl.waitForGameLaunch();
            }
        });

        Button refreshPlayers = (Button) findViewById(R.id.refresh_players_button);
        refreshPlayers.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                //do this when create join game button is clicked
                ctrl.refreshPlayersInList();
            }
        });

        Button launchGame = (Button) findViewById(R.id.launch_game_button);
        launchGame.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //do this whenever start game button is clicked (by leader)
                ctrl.launchGame();
            }
        });

        timer = (TextView) findViewById(R.id.question_timer);
        category = (TextView) findViewById(R.id.category_body);
        question = (TextView) findViewById(R.id.questions_body);
        scrambledAnswer = (TextView) findViewById(R.id.unscramble_body);

        Button submitAnswer = (Button) findViewById(R.id.answer_button);
        submitAnswer.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //do this whenever start game button is clicked (by leader)
                ctrl.submitAnswer(((TextView)findViewById(R.id.response)).getText().toString());
            }
        });

        congratsMessage = (TextView) findViewById(R.id.congrats_label);

        resultsList = (ListView) findViewById(R.id.results_list);

        Button playAgain = (Button) findViewById(R.id.play_again_button);
        playAgain.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                for(int i = 0; i < 5; i++){
                    flipper.showPrevious();     //kick back to game selection gui
                }
                ctrl.sendGameEnd();
            }
        });

    }
    public static void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showNext(){
        flipper.showNext();
    }

    public static void updateGameList(String[] leaders){
        gameList.setAdapter(new ArrayAdapter<String>(
                context,
                android.R.layout.simple_list_item_single_choice,
                leaders){
                    public View getView(int position, View convertView, ViewGroup parent) {
                                    View view = super.getView(position, convertView, parent);
                                    TextView text = (TextView) view.findViewById(android.R.id.text1);
                                    text.setTextColor(Color.BLACK);
                                    return view;
                                }
        }
        );
        gameList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        gameList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                view.setSelected(true);
                if(parent.getItemAtPosition(position) != null){
                    selectedGame = parent.getItemAtPosition(position).toString();
                }
            }
        });
    }

    public static void updatePlayersList(String[] players){
        playersList.setAdapter(new ArrayAdapter<String>(
                context,
                android.R.layout.simple_list_item_1,
                players){
                                   public View getView(int position, View convertView, ViewGroup parent) {
                                       View view = super.getView(position, convertView, parent);
                                       TextView text = (TextView) view.findViewById(android.R.id.text1);
                                       text.setTextColor(Color.BLACK);
                                       return view;
                                   }
        }
        );
    }

    public static void setTimerText(int secondsLeft){
        timer.setText(Integer.toString(secondsLeft));
    }

    public static int getTimerText(){
        return Integer.parseInt(timer.getText().toString());
    }

    public static void replaceQuestionOnScreen(String categoryField, String questionField, String scrambledAnswerField){
        category.setText(categoryField);
        question.setText(questionField);
        scrambledAnswer.setText(scrambledAnswerField);
    }

    public static void showResults(List<String> namesAndScores){
        resultsList.setAdapter(new ArrayAdapter<String>(
                context,
                android.R.layout.simple_list_item_1,
                ClientController.getStringArrayFromObjectArray(namesAndScores.toArray())){
                   @Override
                   public View getView(int position, View convertView, ViewGroup parent) {
                       View view = super.getView(position, convertView, parent);
                       TextView text = (TextView) view.findViewById(android.R.id.text1);
                       text.setTextColor(Color.BLACK);
                       return view;
                   }
        }
        );
        congratsMessage.setText("Congratulations, " +
                namesAndScores.get(0).trim().substring(0, namesAndScores.get(0).indexOf(" ")) + "!");
    }
}
