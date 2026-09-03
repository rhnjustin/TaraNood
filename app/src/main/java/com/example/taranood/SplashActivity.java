package com.example.taranood;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Transition to MainActivity after 1000 milliseconds (1 second)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            
            // Apply smooth fade transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            
            // Close SplashActivity so it's not in the back stack
            finish();
        }, 1000);
    }
}
