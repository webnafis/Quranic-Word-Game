package com.nsa.hafizq;

import android.app.Application;
import com.nsa.hafizq.database.ManageDatabase;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ManageDatabase.getInstance(this); // ← initialize once on app start
    }

    @Override
    public void onTerminate() {
        ManageDatabase.databaseExecutor.shutdown(); // ← shutdown when app dies
        super.onTerminate();
    }
}