package com.nsa.hafizq;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsControllerCompat;

import com.nsa.hafizq.database.ManageDatabase;

public class BaseActivity extends AppCompatActivity {

    protected ManageDatabase db;
    private static final String DB_TAG = "DATABASE_ERROR";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = ManageDatabase.getInstance(this);
        EdgeToEdge.enable(this);

        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
            // Also make the bottom navigation icons white
            controller.setAppearanceLightNavigationBars(false);
        }

        // Lock to portrait before setting content view
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    // ─── Helper to run DB task in background ───────────────────────
// Use a consistent Tag for your database logs

    protected void executeDB(Runnable task) {
        ManageDatabase.databaseExecutor.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                // Log the error with the stack trace
                Log.e(DB_TAG, "Error executing background DB task: " + e.getMessage(), e);
            }
        });
    }

    // ─── Helper to run DB task and update UI after ──────────────────
    protected void executeDB(Runnable backgroundTask, Runnable uiTask) {
        ManageDatabase.databaseExecutor.execute(() -> {
            try {
                backgroundTask.run();

                // Only run the UI task if the background task succeeded
                runOnUiThread(() -> {
                    try {
                        uiTask.run();
                    } catch (Exception e) {
                        Log.e(DB_TAG, "Error executing UI task after DB operation: " + e.getMessage(), e);
                    }
                });

            } catch (Exception e) {
                // Log the background error
                Log.e(DB_TAG, "Error executing background DB task (with UI callback): " + e.getMessage(), e);

                // Optional: Run an error-specific UI task here if needed (like showing a Toast)
            }
        });
    }

    @Override
    protected void onDestroy() {
        // ← nothing DB related here — executor lives in MyApp
        super.onDestroy();
    }
}