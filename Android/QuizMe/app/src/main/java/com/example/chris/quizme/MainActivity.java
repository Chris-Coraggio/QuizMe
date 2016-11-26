package com.example.chris.quizme;

import android.content.Context;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class MainActivity extends AppCompatActivity {

    private final ClientController ctrl = new ClientController();
    private ViewFlipper flipper;
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        flipper =  (ViewFlipper) findViewById(R.id.ViewFlipper);
        new AnimationUtils();
        flipper.setAnimation(AnimationUtils.makeInAnimation
                (getBaseContext(), true));

        //THIS IS LIKELY A BAD IDEA TODO: add a separate thread to handle networking
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        context = getApplicationContext();

        Button connect = (Button) findViewById(R.id.connect_button);
        connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //do this when connect button is clicked
                boolean connectionSuccessful =
                        ctrl.connectToServer(((EditText) findViewById(R.id.ip_address_field)).getText().toString());
                if (!connectionSuccessful) {
                    showToast("Unable to connect, please try again");
                } else {
                    flipper.showNext();
                }
            }
        });


        Button login = (Button) findViewById(R.id.login_button);
        login.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //do this when login button is clicked
                String username = ((EditText) findViewById(R.id.username_field)).getText().toString();
                String password = ((EditText) findViewById(R.id.password_field)).getText().toString();
                try {
                    if(ctrl.login(username, password)) {
                        flipper.showNext();
                    }
                }catch (Exception e){
                    showToast("Error occured while logging in. Please try again");
                }
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

        Button createGame = (Button) findViewById(R.id.new_game_button);
        createGame.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                //do this when create new game button is clicked
                showToast("testing");
                //ctrl.createNewGame();
            }
        });

        Button joinGame = (Button) findViewById(R.id.join_button);
        joinGame.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                //do this when create join game button is clicked

            }
        });
    }
    public static void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
