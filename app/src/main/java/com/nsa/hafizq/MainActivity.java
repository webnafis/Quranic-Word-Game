package com.nsa.hafizq;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;


import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends BaseActivity {
    private Handler navHandler = new Handler();
//    private ManageDatabase myDB;

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



        findViewById(R.id.bottom_nav_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(RecallFragment.class);
            }
        });
        findViewById(R.id.bottom_nav_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(PronounceFragment.class);
            }
        });
        findViewById(R.id.bottom_nav_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(ProfileFragment.class);
            }
        });
        findViewById(R.id.bottom_nav_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(ListFragment.class);
            }
        });
        findViewById(R.id.bottom_nav_pvp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(PVPFragment.class);
            }
        });


    }


    // 3. Stop it when the Activity is no longer visible
    @Override
    protected void onPause() {
        super.onPause();

//        navHandler.removeCallbacks(navRunnable);

        navHandler.removeCallbacksAndMessages(null);
    }

    // 4. Restart it when the user comes back
    @Override
    protected void onResume() {
        super.onResume();


        navHandler.removeCallbacks(navRunnable);
        navHandler.postDelayed(navRunnable, 1000);
    }
    @Override
    protected void onDestroy() {
        // 1. Clear all pending Runnables and messages from the Handler
        navHandler.removeCallbacksAndMessages(null);

        // 2. Clear the reference to the Runnable just to be extra safe
        navHandler.removeCallbacks(navRunnable);

        super.onDestroy();
    }
    private void start3DRotation(View view){
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotationY", 0f, 360f);
        animator.setDuration(1500);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    public void switchFragment( @NonNull Class<? extends androidx.fragment.app.Fragment> newFragmentClass) {
        androidx.fragment.app.Fragment currentFragment =
                getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);

        if (currentFragment != null && currentFragment.getClass().equals(newFragmentClass)) {
            // We are already looking at this fragment! Exit the method early.
            return;
        }
        getSupportFragmentManager().beginTransaction()
                // 1. Set the sliding animations
                .setCustomAnimations(
                        R.anim.slide_in_right,  // Animation for fragment entering
                        R.anim.slide_out_left,  // Animation for fragment exiting
                        0, 0                    // No animations for 'Back' (since we aren't using backstack)
                )
                // 2. Replace the current fragment
                .replace(R.id.fragmentContainerView, newFragmentClass, null)
                .setReorderingAllowed(true)
                // 3. DO NOT call .addToBackStack(null)
                // This ensures the fragment is destroyed/removed and 'Back' exits the Activity

                .commit();
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


}

