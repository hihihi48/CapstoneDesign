package com.example.weatherinfo;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //로딩화면 시작.
        Loadingstart();
    }
    private void Loadingstart(){
        Handler handler=new Handler();
        handler.postDelayed(new Runnable(){
            public void run(){
                SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
                String signup = sharedPreferences.getString("signup","");
                Intent intent;
                if(signup.equals("OK")) {
                    intent = new Intent(getApplicationContext(), MainActivity.class);
                }
                else{
                    intent = new Intent(getApplicationContext(), SignupActivity.class);
                }
                startActivity(intent);
                finish();
            }
        },2000);
    }
}