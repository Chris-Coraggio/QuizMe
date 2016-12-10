package com.example.chris.quizme;

import android.content.Context;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private final ClientController ctrl = new ClientController();
    private static ViewFlipper flipper;
    private static Context context;
    private static ListView gameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        flipper =  (ViewFlipper) findViewById(R.id.ViewFlipper);
        new AnimationUtils();
        flipper.setAnimation(AnimationUtils.makeInAnimation
                (getBaseContext(), true));

        //THIS IS LIKELY A BAD IDEA TODO: add a separate thread to handle networking
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
                try{
                    ctrl.register(username, password);
                }catch(Exception e){
                    showToast("Registration failed. Please try again.");
                }
        }});

        gameList = (ListView) findViewById(R.id.games_list);

        Button refresh = (Button) findViewById(R.id.refresh_button);
        refresh.setOnClickListener(new View.OnClickListener(){
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
            ctrl.joinGame((String)gameList.getSelectedItem());
            }
        });
    }
    public static void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showNext(){
        flipper.showNext();
    }

    public static void updateGameList(String[] leaders){
        gameList.setAdapter(new ArrayAdapter<String>(
                context,
                android.R.layout.simple_list_item_1,
                leaders)
        );
        gameList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        gameList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                view.setSelected(true);
                if(parent.getItemAtPosition(position) != null){
                    //System.out.println(parent.getItemAtPosition(position).toString());
                }
            }
        });
    }
}
