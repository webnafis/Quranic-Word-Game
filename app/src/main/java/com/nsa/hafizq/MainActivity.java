package com.nsa.hafizq;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;


import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends BaseActivity {
    private Handler navHandler = new Handler();
    private AnimatorSet pulseSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        apply3DDepth(findViewById(R.id.bottom_nav_1));
        apply3DDepth(findViewById(R.id.bottom_nav_2));
        apply3DDepth(findViewById(R.id.bottom_nav_3));
        apply3DDepth(findViewById(R.id.bottom_nav_4));
        triggerNavbarWave();
        navHandler.postDelayed(navRunnable, 6000);
        startPlayButtonAnimation();


        findViewById(R.id.btn_play_recall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, RecallGameActivity.class);
                startActivity(i);
            }
        });
    }

    // 3. Stop it when the Activity is no longer visible
    @Override
    protected void onPause() {
        super.onPause();
        if (pulseSet != null) {
            pulseSet.cancel(); // Stops the animation immediately
        }
//        navHandler.removeCallbacks(navRunnable);

        navHandler.removeCallbacksAndMessages(null);
    }

    // 4. Restart it when the user comes back
    @Override
    protected void onResume() {
        super.onResume();
        if (pulseSet != null) {
            pulseSet.start();
        } else {
            startPlayButtonAnimation();
        }

        navHandler.removeCallbacks(navRunnable);
        navHandler.postDelayed(navRunnable, 1000);
    }

    private void start3DRotation(View view){
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotationY", 0f, 360f);
        animator.setDuration(1500);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

//    private void setupAutoRotate(){
//        final Handler handler = new Handler();
//        final int delay = 6000;
//
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                triggerNavbarWave();
//
//                handler.postDelayed(this, delay);
//            }
//        }, delay);
//    }
    private Runnable navRunnable = new Runnable() {
        @Override
        public void run() {
            triggerNavbarWave();
            navHandler.postDelayed(this, 6000);

        }
    };

    private void triggerNavbarWave() {
        int[] navIds = {R.id.bottom_nav_1, R.id.bottom_nav_2, R.id.bottom_nav_3, R.id.bottom_nav_4};

        for (int i = 0; i < navIds.length; i++) {
            final View v = findViewById(navIds[i]);
            if (v != null) {
                // Delay each button by 150ms to create a 'wave'
                navHandler.postDelayed(() -> start3DRotation(v), i * 500);
            }
        }
    }

    private  void apply3DDepth(View view){
        float scale = getResources().getDisplayMetrics().density;

        view.setCameraDistance(8000*scale);
    }

    private void startPlayButtonAnimation(){
        View playButton = findViewById(R.id.btn_play_recall);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(playButton, "scaleX", 1f, 1.5f, 1f);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(playButton, "scaleY", 1f, 1.5f, 1f);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        ObjectAnimator elevation = ObjectAnimator.ofFloat(playButton, "translationZ", 0f, 1000f, 0f);
        elevation.setRepeatCount(ObjectAnimator.INFINITE);

        pulseSet = new AnimatorSet();
        pulseSet.playTogether(scaleX, scaleY, elevation);
        pulseSet.setDuration(3000);
        pulseSet.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseSet.start();

    }
}

