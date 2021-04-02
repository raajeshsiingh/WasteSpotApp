package com.example.wastespotapp;

import android.app.Application;

import com.parse.Parse;

public class App extends Application {

    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("7ANVYE46zaiLQ7xijCqlBmCRs1MiqY5uxtmvQV7f")
                .clientKey("sQ4AVsG7tmUmvuMc3ZHmHNSo5cOtYaQVXDSWQ4lA")
                .server("https://parseapi.back4app.com/")
                .build()
        );
    }
}
