package com.nsa.hafizq.database;

import android.content.Context;

import androidx.room.Database;
//import androidx.room.Room;
import androidx.room.Room;
import androidx.room.RoomDatabase;
//import androidx.room.RoomDatabase;

import com.nsa.hafizq.database.dao.DailyTargetDao;
import com.nsa.hafizq.database.dao.SettingsDao;
import com.nsa.hafizq.database.dao.WordDao;
import com.nsa.hafizq.database.entity.DailyTarget;
import com.nsa.hafizq.database.entity.Settings;
import com.nsa.hafizq.database.entity.Word;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Word.class, Settings.class, DailyTarget.class}, version = 1)
public abstract class ManageDatabase extends RoomDatabase {

    public abstract WordDao wordDao();
    public abstract SettingsDao settingsDao();
    public abstract DailyTargetDao dailyTargetDao();

    // ─── ONE static executor for entire app ────────────────────────
    public static final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    private static ManageDatabase instance;

    public static ManageDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    ManageDatabase.class,
                    "app.db"
                )
                .createFromAsset("app.db")
                .build();             // ← no fallbackToDestructiveMigration
            }
        return instance;
    }

    // ─── GET TODAY'S DATE STRING ───────────────────────────────────────
    public static String getTodayDate() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(new java.util.Date());
    }
}