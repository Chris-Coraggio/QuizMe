package com.example.chris.quizme;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ViewSwitcher;

public class MainActivity extends AppCompatActivity {

    private final ClientController ctrl = new ClientController();
    private ViewSwitcher switcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        switcher =  (ViewSwitcher) findViewById(R.id.ViewSwitcher);

        Button login = (Button) findViewById(R.id.login_button);
        login.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //do this when login button is clicked
                String username = ((EditText) findViewById(R.id.username_field)).getText().toString();
                String password = ((EditText) findViewById(R.id.password_field)).getText().toString();
                ctrl.login(username, password);

                new AnimationUtils();
                switcher.setAnimation(AnimationUtils.makeInAnimation
                        (getBaseContext(), true));
                switcher.showNext();
            }
        });

        Button register = (Button) findViewById(R.id.register_button);
        register.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //do this when register button is clicked
                String username = ((EditText) findViewById(R.id.username_field)).getText().toString();
                String password = ((EditText) findViewById(R.id.password_field)).getText().toString();
                ctrl.register(username, password);
            }
        });
    }
}
