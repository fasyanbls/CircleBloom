package com.example.circlebloom_branch;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class CircleBloomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
    }
}

