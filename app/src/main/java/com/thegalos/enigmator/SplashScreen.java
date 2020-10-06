package com.thegalos.enigmator;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });


        Thread splash_screen = new Thread() {
            public void run() {
                try {
                    sleep(2500);
                } catch(Exception e) {
                    e.printStackTrace();
                } finally {
                    Intent main_menu = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(main_menu);
                }
            }
        };
        splash_screen.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
