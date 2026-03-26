package com.nsa.hafizq;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecallGameActivity extends BaseActivity {
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private SoundPool soundPool;
    private int nextSound, correctSound;
    private  int wrongSound;
    private LinkedList<WordModel> activeWords = new LinkedList<>();
//    private MediaPlayer backgroundMusic;
//    private int currentScore = 0;
    private int lives = 5;
    private int timeLeft = 10; // Seconds
    private boolean timerRunning = false;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private boolean isPaused = false;
    private boolean sound;
    private boolean music;
    private int totalDailyMission;
    private int completedDailyMission = 0;
    private int highestScore;
//    private boolean isRecall = true;
    private int track = 0;
    private boolean isQuize = true;
    private int length = -1;
    private  int score = 0;
    private ImageButton[] lifeIcons;
    private TextToSpeech tts;
    private boolean isTtsReady = false;
    // Database
    private ManageDatabase myDB;

    // Word Model for helper
    private static class WordModel {
        int id;
        String arabic, bangla;
        WordModel(int id, String ar, String bn) {
            this.id = id; this.arabic = ar; this.bangla = bn;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recall_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_recall), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        backgroundMusic = MediaPlayer.create(this, R.raw.nasheed);
//        backgroundMusic.setLooping(true);
//        // (0.0 to 1.0 range)
//        backgroundMusic.setVolume(0.5f, 0.5f);
//        // Start playing
//        backgroundMusic.start();

        // Standard SoundPool Initialization
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(3) // Allow up to 3 sounds to play at the exact same time
                .setAudioAttributes(attrs)
                .build();


        // Load all three sounds from your res/raw folder
//        nextSound = soundPool.load(this, R.raw.next, 1);
//        correctSound = soundPool.load(this, R.raw.correct_10, 1);
        wrongSound = soundPool.load(this, R.raw.wrong_slap, 1);

        lifeIcons = new ImageButton[]{
                findViewById(R.id.live5),
                findViewById(R.id.live4), // Note: Fixed your missing '4' from XML
                findViewById(R.id.live3),
                findViewById(R.id.live2),
                findViewById(R.id.live1)
        };

        myDB = new ManageDatabase(this);
        databaseExecutor.execute(() -> {
            loadInitialWords(); // 1. Wait for DB

            // 2. Start TTS and pass the "Next Step" as a Runnable
            initTTS(() -> {
                // This ONLY runs once DB is done AND TTS is Success
                ((TextView)findViewById(R.id.hScore)).setText("Highest score: " + highestScore);
                nextQuestion();
            });
        });


        // Show first question


        findViewById(R.id.next).setOnClickListener(v -> {
//            soundPool.play(nextSound, 1, 1, 0, 0, 1);
            // Only increment when moving to the next card
            if (track < length) {
                track++;
            } else {
                track=0;// Logic for loading new words or switching layouts
            }
            nextQuestion();

        });

        findViewById(R.id.pause_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPauseMenu();
            }
        });

        findViewById(R.id.settings_dialog_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingsDialog();
            }
        });

        findViewById(R.id.daily_mission_dialog_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                int pastCompleted ;
                runOnUiThread(()->{
                    int pastCompleted = myDB.getDailyTargetCompletedCount();
                    showDailyMissionDialog(completedDailyMission+pastCompleted, totalDailyMission);
                });


            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showPauseMenu();
            }
        });
// couse adding on creation
//        findViewById(R.id.optionCard).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                checkAnswer(v);
//            }
//        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true; // Timer runnable will skip its next execution
//        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
//            backgroundMusic.pause();
//        }
        if (tts != null) {
            // Stop speaking immediately when the user leaves the screen
            tts.stop();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timerRunning) {
            isPaused = false;
//            timerHandler.postDelayed(timerRunnable, 1000); // Resume
        }
//        if (backgroundMusic != null) {
//            backgroundMusic.start();
//        }
        // If tts was destroyed or null, re-initialize it
        if (tts == null) {
            initTTS();
        }
    }

    @Override
    protected void onDestroy() {
        stopTimer();
        databaseExecutor.shutdown();
        super.onDestroy();
        // 5. Clean up memory
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        // VERY IMPORTANT: Release resources
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            isTtsReady = false;
        }

//        if (backgroundMusic != null) {
//            backgroundMusic.stop();
//            backgroundMusic.release();
//            backgroundMusic = null;
//        }
    }
    private void startTimer() {
        stopTimer();
        timeLeft = 10;
        timerRunning = true;
        isPaused = false;

        TextView tvTimer = findViewById(R.id.timerText);
        tvTimer.setText(timeLeft + "s");

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                // If the activity is finishing or paused, stop execution
                if (isFinishing() || isDestroyed() || !timerRunning) return;

                // If isPaused is true (dialog open), just wait and check again in 1sec
                if (isPaused) {
                    timerHandler.postDelayed(this, 1000);
                    return;
                }

                timeLeft--;

                runOnUiThread(() -> {
                    if (tvTimer != null) tvTimer.setText(timeLeft + "s");

                    // Optional: Turn red when time is low
                    if (timeLeft <= 3) {
                        if (tvTimer != null) tvTimer.setTextColor(Color.parseColor("#9D0000"));
                    } else {
                        if (tvTimer != null) tvTimer.setTextColor(Color.parseColor("#02081B"));
                    }
                });

                // Optional: Update a timer TextView here
                // tvTimer.setText(String.valueOf(timeLeft));

                if (timeLeft <= 0) {
                    // 1. Penalty
                    decreaseLife();

                    // 2. Give another chance if they still have lives
                    if (lives > 0) {
                        timeLeft = 10; // Reset seconds

                        if (tvTimer != null) tvTimer.setText(timeLeft + "s");

                        // Optional: Turn red when time is low
                        if (tvTimer != null) tvTimer.setTextColor(Color.parseColor("#02081B"));

                        // Shake the card or show a "Time's Up" message here
                        timerHandler.postDelayed(this, 1000);
                    } else {
                        stopTimer();
                    }
                } else {
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void stopTimer() {

        timerRunning = false;
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        TextView timerTextCon =  findViewById(R.id.timerText);
        timerTextCon.setText("\u221E");
        timerTextCon.setTextColor(Color.parseColor("#02081B"));

    }
    private void decreaseLife() {
        if (lives > 0) {
            // doublee sound problem solved i gueww now i have run to check
            if(!sound){
                soundPool.play(wrongSound, 1, 1, 0, 0, 1);
            }

            int iconIndex = lives - 1;

            // 1. Visual feedback on the heart
            lifeIcons[iconIndex].animate()
                    .scaleX(1.5f).scaleY(1.5f).alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> lifeIcons[iconIndex].setVisibility(View.INVISIBLE));

            // 2. Shake the whole layout
            View mainLayout = findViewById(R.id.main_recall);
            mainLayout.animate().translationX(20f).setDuration(100).withEndAction(() ->
                    mainLayout.animate().translationX(-20f).setDuration(100).withEndAction(() ->
                            mainLayout.animate().translationX(0f).setDuration(100)
                    )
            );

            lives--;

            if (lives <= 0) {
                handleGameOver();
            }
        }
    }

    private void handleGameOver() {
        // 1. Stop interactions
        findViewById(R.id.next).setEnabled(false);
        isPaused = true; // Prevents any background timers from firing

        // 2. Save progress to Database (Async)
        databaseExecutor.execute(() -> {
            // Save the score if it's a new record
            if (score > highestScore) {
                myDB.updateHighestRecord(score);
            }
            int         completedDaily = myDB.getDailyTargetCompletedCount();
            // Save the daily mission progress
            myDB.updateDailyTargetCompletedCount(completedDailyMission+ completedDaily);

            // Save the last word ID so they can resume learning later
            if (!activeWords.isEmpty()) {
                myDB.updateLastStartingWordId(activeWords.get(0).id);
            }

            // 3. Show UI on Main Thread
            runOnUiThread(() -> {
                showGameResult();
            });
        });
    }
    private void handleGameOverForRestart() {
        // 1. Stop interactions
        findViewById(R.id.next).setEnabled(false);
        isPaused = true; // Prevents any background timers from firing

        // 2. Save progress to Database (Async)
        databaseExecutor.execute(() -> {
            // Save the score if it's a new record
            if (score > highestScore) {
                myDB.updateHighestRecord(score);
            }
            int completedDaily = myDB.getDailyTargetCompletedCount();
            // Save the daily mission progress
            myDB.updateDailyTargetCompletedCount(completedDailyMission+ completedDaily);

            // Save the last word ID so they can resume learning later
            if (!activeWords.isEmpty()) {
                myDB.updateLastStartingWordId(activeWords.get(0).id);
            }

        });
    }
    private void loadInitialWords() {
        myDB = new ManageDatabase(this);

        // 1. Get the last ID the user was at from SETTINGS table
        int startId = myDB.getLastStartingWordId();

        // 2. Fetch the first 4 words to start the game
        Cursor cursor = myDB.getFourWordsFromId(startId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(ManageDatabase.COLUMN_1_SEL_ID));
                String ar = cursor.getString(cursor.getColumnIndexOrThrow(ManageDatabase.COLUMN_2_ARABIC));
                String bn = cursor.getString(cursor.getColumnIndexOrThrow(ManageDatabase.COLUMN_3_BANGLA));

                activeWords.add(new WordModel(id, ar, bn));
            } while (cursor.moveToNext());
            cursor.close();
        }

        sound = myDB.isSoundOn();
        music = myDB.isMusicOn();
        totalDailyMission = myDB.getDailyTarget();
//        completedDailyMission = myDB.getDailyTargetCompletedCount();
        highestScore = myDB.getHighestRecord();

    }

    private void handleEndGame(){
        findViewById(R.id.next).setEnabled(false);
        isPaused = true; // Prevents any background timers from firing

        // 2. Save progress to Database (Async)
        databaseExecutor.execute(() -> {
            // Save the score if it's a new record
            if (score > highestScore) {
                myDB.updateHighestRecord(score);
            }
            int         completedDaily = myDB.getDailyTargetCompletedCount();
            // Save the daily mission progress
            myDB.updateDailyTargetCompletedCount(completedDailyMission+ completedDaily);

            // Save the last word ID so they can resume learning later

                myDB.updateLastStartingWordId(1);


            // 3. Show UI on Main Thread
            runOnUiThread(() -> {
                loadEndGameDialog();
            });
        });
    }

    private void loadEndGameDialog(){
        View v = getLayoutInflater().inflate(R.layout.dialogue_result, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this, R.style.FullScreenDialogTheme).setView(v).setCancelable(false).create();

        // Bind Views
        MaterialCardView mainCard = v.findViewById(R.id.mainCard);
        TextView title = v.findViewById(R.id.txtResultTitle);
        MaterialButton btnPlay = v.findViewById(R.id.btnPlayAgain);
        TextView txtResultMessage = v.findViewById(R.id.txtResultMessage);

        // 1. Logic for Daily Task
        TextView valDaily = v.findViewById(R.id.valDaily);
        databaseExecutor.execute(() -> {
            int dailyCompleted = myDB.getDailyTargetCompletedCount();

            runOnUiThread(()->{
                if (dailyCompleted >= totalDailyMission) {

                    valDaily.setTextColor(Color.parseColor("#4CAF50"));
                }
                valDaily.setText(dailyCompleted + "/" + totalDailyMission);
            });
        });

        // 2. Determine Theme
        String themeColor = "#FFD700"; // GOLD (New Record)
            title.setText("ALHAMDULILLAH! \uD83C\uDFC6");
            txtResultMessage.setText("Unbelievable! You've completed the MISSION!");


        // Apply Theme Colors
        mainCard.setStrokeColor(ColorStateList.valueOf(Color.parseColor(themeColor)));
        title.setTextColor(Color.parseColor(themeColor));
        btnPlay.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(themeColor)));

        // Set other values
        ((TextView)v.findViewById(R.id.valScore)).setText(String.valueOf(score));
        ((TextView)v.findViewById(R.id.valWords)).setText(String.valueOf(completedDailyMission));
        ((TextView)v.findViewById(R.id.valBest)).setText(String.valueOf(highestScore));

        v.findViewById(R.id.btnPlayAgain).setVisibility(View.GONE);
        v.findViewById(R.id.btnGoHome).setOnClickListener(view -> { dialog.dismiss(); finish(); });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        }

        dialog.show();

//        if (dialog.getWindow() != null) {
//            dialog.getWindow().setLayout(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.MATCH_PARENT
//            );
//        }
    }

    private void nextQuestion() {
        if (tts != null) {
            tts.stop();
        }
        //track
        //db update
        //layout change
        //
//        if(activeWords.size() <=5){}

        if(track == 0 ){
//            track = 0;
            isQuize = !isQuize;
            nextLayout();

        if(!isQuize){
            databaseExecutor.execute(() -> {
                // 1. Fetch from DB (Background Thread)
                Cursor cursor = myDB.getNextWordAfter(activeWords.getLast().id);

                WordModel fetchedWord = null;
                if (cursor != null && cursor.moveToFirst()) {
                    fetchedWord = new WordModel(
                            cursor.getInt(cursor.getColumnIndexOrThrow(ManageDatabase.COLUMN_1_SEL_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(ManageDatabase.COLUMN_2_ARABIC)),
                            cursor.getString(cursor.getColumnIndexOrThrow(ManageDatabase.COLUMN_3_BANGLA))
                    );
                    cursor.close();
                }

                // 2. Add to LinkedList (Back on UI Thread)
                final WordModel result = fetchedWord;

                if (result != null) {
                    // This adds the new word to the end of your LinkedList
                    activeWords.addLast(result);
                }else{
                    handleEndGame();
                }
                // If you want to see the update immediately:
                // nextQuestion();

            });

            if(length == 9){
                runOnUiThread(()->{
                    activeWords.removeFirst();

                    databaseExecutor.execute(() -> {
                        myDB.updateLastStartingWordId(activeWords.getFirst().id);
                    });
                });

            }

            if(length<9){
                length += 1 ;
            }
        }




        }

        if(isQuize){

            //change ui per click meaning add new quize
            FrameLayout layoutBangla1 = findViewById(R.id.containerBangla1);
            FrameLayout layoutArabic1 = findViewById(R.id.containerArabic1);
            FrameLayout layoutArabic2 = findViewById(R.id.containerArabic2);
            FrameLayout layoutArabic3 = findViewById(R.id.containerArabic3);
            FrameLayout layoutArabic4 = findViewById(R.id.containerArabic4);
            for (int i = 0; i < layoutBangla1.getChildCount(); i++) {
                View oldBangla1 = layoutBangla1.getChildAt(i);
                View oldArabic1 = layoutArabic1.getChildAt(i);
                View oldArabic2 = layoutArabic2.getChildAt(i);
                View oldArabic3 = layoutArabic3.getChildAt(i);
                View oldArabic4 = layoutArabic4.getChildAt(i);


                oldBangla1.animate().rotationY(90f).alpha(0f).setDuration(500).withEndAction(() -> layoutBangla1.removeView(oldBangla1));
                oldArabic1.animate().rotationY(-90f).alpha(0f).setDuration(500).withEndAction(() -> layoutArabic1.removeView(oldArabic1));
                oldArabic2.animate().rotationY(90f).alpha(0f).setDuration(500).withEndAction(() -> layoutArabic2.removeView(oldArabic2));
                oldArabic3.animate().rotationY(-90f).alpha(0f).setDuration(500).withEndAction(() -> layoutArabic3.removeView(oldArabic3));
                oldArabic4.animate().rotationY(90f).alpha(0f).setDuration(500).withEndAction(() -> layoutArabic4.removeView(oldArabic4));


            }

            View newBangla1 = getLayoutInflater().inflate(R.layout.quize_question_card, null);
            View newArabic1 = getLayoutInflater().inflate(R.layout.item_quize_option, null);
            View newArabic2 = getLayoutInflater().inflate(R.layout.item_quize_option, null);
            View newArabic3 = getLayoutInflater().inflate(R.layout.item_quize_option, null);
            View newArabic4 = getLayoutInflater().inflate(R.layout.item_quize_option, null);
            newArabic1.setOnClickListener(v -> checkAnswer(v));
            newArabic2.setOnClickListener(v -> checkAnswer(v));
            newArabic3.setOnClickListener(v -> checkAnswer(v));
            newArabic4.setOnClickListener(v -> checkAnswer(v));
//            WordModel data = activeWords.get(track);

            ((TextView)newBangla1.findViewById(R.id.tvWord)).setText(activeWords.get(track).bangla);
            ArrayList<StringBuilder> arr = new ArrayList<>();
            ArrayList<StringBuilder> arr2 = new ArrayList<>();
            for(int i = track; i< track+4; i++){
                arr.add( new StringBuilder(activeWords.get(i).arabic));
            }
            for(int i = 4; i>0; i--){
                int it = getRandomZeroUpto(i);
                arr2.add(arr.get(it));
                arr.remove(it);
            }
            ((TextView)newArabic1.findViewById(R.id.tvOptionArabic)).setText(arr2.get(0));
            ((TextView)newArabic2.findViewById(R.id.tvOptionArabic)).setText(arr2.get(1));
            ((TextView)newArabic3.findViewById(R.id.tvOptionArabic)).setText(arr2.get(2));
            ((TextView)newArabic4.findViewById(R.id.tvOptionArabic)).setText(arr2.get(3));


            newBangla1.setTranslationX(-1000f);
            newArabic1.setTranslationX(1000f);
            newArabic2.setTranslationX(-1000f);
            newArabic3.setTranslationX(1000f);
            newArabic4.setTranslationX(-1000f);

            // Set 3D perspective
            newBangla1.setCameraDistance(8000 * getResources().getDisplayMetrics().density);
            newArabic1.setCameraDistance(8000 * getResources().getDisplayMetrics().density);
            newArabic2.setCameraDistance(8000 * getResources().getDisplayMetrics().density);
            newArabic3.setCameraDistance(8000 * getResources().getDisplayMetrics().density);
            newArabic4.setCameraDistance(8000 * getResources().getDisplayMetrics().density);


            layoutBangla1.addView(newBangla1);
            layoutArabic1.addView(newArabic1);
            layoutArabic2.addView(newArabic2);
            layoutArabic3.addView(newArabic3);
            layoutArabic4.addView(newArabic4);

            // 4. Animate NEW cards in (Slide from right)
            newBangla1.animate().translationX(0f).setDuration(500).setInterpolator(new DecelerateInterpolator()).start();
            newArabic1.animate().translationX(0f).setDuration(500).setInterpolator(new DecelerateInterpolator()).start();
            newArabic2.animate().translationX(0f).setDuration(500).setInterpolator(new DecelerateInterpolator()).start();
            newArabic3.animate().translationX(0f).setDuration(500).setInterpolator(new DecelerateInterpolator()).start();
            newArabic4.animate().translationX(0f).setDuration(500).setInterpolator(new DecelerateInterpolator()).withEndAction(() -> {
               // generateAndSpeak(activeWords.get(track).bangla, "bn", false);
                startTimer();
            }).start();

            findViewById(R.id.next).setEnabled(false);
//            if (track < (int)length) {6
//                track++;
//            } else {
//                track=0;// Logic for loading new words or switching layouts
//            }
//            if(track < length){
//
//            }else{
//
//
//
//            }

        }else{
//            stopTimer();
            WordModel data = activeWords.get(track);
            FrameLayout layoutBangla = findViewById(R.id.containerBangla);
            FrameLayout layoutArabic = findViewById(R.id.containerArabic);

            // 2. Animate OLD cards out (Rotate 90 and Fade)
            for (int i = 0; i < layoutBangla.getChildCount(); i++) {
                View oldBangla = layoutBangla.getChildAt(i);
                View oldArabic = layoutArabic.getChildAt(i);

                oldBangla.animate().rotationY(90f).alpha(0f).setDuration(500).withEndAction(() -> layoutBangla.removeView(oldBangla));
                oldArabic.animate().rotationY(-90f).alpha(0f).setDuration(500).withEndAction(() -> layoutArabic.removeView(oldArabic));
            }

            // 3. Create NEW cards
            View newBangla = getLayoutInflater().inflate(R.layout.item_quiz_card, null);
            View newArabic = getLayoutInflater().inflate(R.layout.item_quiz_card, null);
            newBangla.findViewById(R.id.speak_text_card).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    generateAndSpeak(data.bangla, "bn", false);
                }
            });
            newArabic.findViewById(R.id.speak_text_card).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                     generateAndSpeak(data.arabic, "ar", false);
                }
            });

            // Set Text
            ((TextView)newBangla.findViewById(R.id.tvWord)).setText(data.bangla);
            ((TextView)newArabic.findViewById(R.id.tvWord)).setText(data.arabic);
            ((TextView)newArabic.findViewById(R.id.tvWord)).setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);

            // Initial position: Off-screen to the right
            newBangla.setTranslationX(-1000f);
            newArabic.setTranslationX(1000f);

            // Set 3D perspective
            newBangla.setCameraDistance(8000 * getResources().getDisplayMetrics().density);
            newArabic.setCameraDistance(8000 * getResources().getDisplayMetrics().density);

            layoutBangla.addView(newBangla);
            layoutArabic.addView(newArabic);

            // 4. Animate NEW cards in (Slide from right)
            newBangla.animate().translationX(0f).setDuration(500).setInterpolator(new DecelerateInterpolator()).start();
            newArabic.animate().translationX(0f).setDuration(500).setInterpolator(new DecelerateInterpolator()).withEndAction(() -> {
                // 1. Speak Bangla immediately (clears any old audio)
//                generateAndSpeak(data.bangla, "bn", false);

                // 2. Queue Arabic (waits for Bangla to finish)
//                generateAndSpeak(data.arabic, "ar", true);
            }).start();



        }

        ((TextView)findViewById(R.id.cardsCount)).setText((track+1)+"/"+(length+1)+" Cards");


    }

    public void checkAnswer(View ansCard){
        if( 9 == track && ((TextView)ansCard.findViewById(R.id.tvOptionArabic)).getText().toString().equals(activeWords.get(track).arabic)){
            generateAndSpeak(activeWords.get(track).arabic, "ar", false);
//            soundPool.play(correctSound, 1, 1, 0, 0, 1);
            ansCard.findViewById(R.id.optionBackground).setBackgroundResource(R.drawable.bg_option_gold_grad);
            completedDailyMission++;
            ((TextView)findViewById(R.id.totalWordCount)).setText("Words: "+completedDailyMission);
            score++;
            ((TextView)findViewById(R.id.scoreCount)).setText("Score: "+score);

            if(score>highestScore){
                highestScore++;
                ((TextView)findViewById(R.id.hScore)).setText("Highest score: "+highestScore);
                databaseExecutor.execute(() -> {
                    // Save the score if it's a new record
                        myDB.updateHighestRecord(score);
                });
            }

            View card1 = ((FrameLayout)findViewById(R.id.containerArabic1)).getChildAt(0);//.setEnabled(false);
            View card2 = ((FrameLayout)findViewById(R.id.containerArabic2)).getChildAt(0);//.setEnabled(false);
            View card3 = ((FrameLayout)findViewById(R.id.containerArabic3)).getChildAt(0);//.setEnabled(false);
            View card4 = ((FrameLayout)findViewById(R.id.containerArabic4)).getChildAt(0);//.setEnabled(false);
            card1.setEnabled(false);
            card2.setEnabled(false);
            card3.setEnabled(false);
            card4.setEnabled(false);
            card1.setClickable(false);
            card2.setClickable(false);
            card3.setClickable(false);
            card4.setClickable(false);


            findViewById(R.id.next).setEnabled(true);
            stopTimer();
        }else if(((TextView)ansCard.findViewById(R.id.tvOptionArabic)).getText().toString().equals(activeWords.get(track).arabic)){
            generateAndSpeak(activeWords.get(track).arabic, "ar", false);
//            soundPool.play(correctSound, 1, 1, 0, 0, 1);
            ansCard.findViewById(R.id.optionBackground).setBackgroundResource(R.drawable.bg_option_green_grad);
            score++;
            ((TextView)findViewById(R.id.scoreCount)).setText("Score: "+score);
            if(score>highestScore){
                highestScore++;
                ((TextView)findViewById(R.id.hScore)).setText("Highest score: "+highestScore);
                databaseExecutor.execute(() -> {
                    // Save the score if it's a new record
                    myDB.updateHighestRecord(score);
                });
            }
            View card1 = ((FrameLayout)findViewById(R.id.containerArabic1)).getChildAt(0);//.setEnabled(false);
            View card2 = ((FrameLayout)findViewById(R.id.containerArabic2)).getChildAt(0);//.setEnabled(false);
            View card3 = ((FrameLayout)findViewById(R.id.containerArabic3)).getChildAt(0);//.setEnabled(false);
            View card4 = ((FrameLayout)findViewById(R.id.containerArabic4)).getChildAt(0);//.setEnabled(false);
            card1.setEnabled(false);
            card2.setEnabled(false);
            card3.setEnabled(false);
            card4.setEnabled(false);
            card1.setClickable(false);
            card2.setClickable(false);
            card3.setClickable(false);
            card4.setClickable(false);
            findViewById(R.id.next).setEnabled(true);
            stopTimer();
        }else{
            //generateAndSpeak(((TextView)ansCard.findViewById(R.id.tvOptionArabic)).getText().toString(), "ar", false);
            ansCard.findViewById(R.id.optionBackground).setBackgroundResource(R.drawable.bg_option_red_grad);
            ansCard.setEnabled(false);
            ansCard.setClickable(false);
            decreaseLife();
            startTimer();
        }
    }

    public int getRandomZeroUpto(int num) {
        Random rand = new Random();
        // nextInt(2) generates a random integer between 0 (inclusive) and upto that num but not the num
        return rand.nextInt(num);
    }

    private void nextLayout() {
        FrameLayout layoutRecallQuize = findViewById(R.id.recall_quize_layout);
        View newChildlayout;
        if ( !isQuize ){
             newChildlayout = getLayoutInflater().inflate(R.layout.recall_layout, null);
//             isRecall= !isRecall;
        }else{
             newChildlayout = getLayoutInflater().inflate(R.layout.quize_layout, null);
//            isRecall= !isRecall;
        }
        changeLayout(layoutRecallQuize, newChildlayout, -90f, 1000f, 600);
    }

    private void changeLayout(FrameLayout motherLayout, View newChildLayout, float rotY,  float translX , int duration){
        for (int i = 0; i < motherLayout.getChildCount(); i++) {
            View childLayout = motherLayout.getChildAt(i);
            childLayout.animate().rotationY(rotY).alpha(0f).setDuration(duration).withEndAction(() -> motherLayout.removeView(childLayout));
        }
        // Initial position: Off-screen to the right
        newChildLayout.setTranslationX(translX);
        motherLayout.addView(newChildLayout);
        // 4. Animate NEW cards in (Slide from right)
        newChildLayout.animate().translationX(0f).setDuration(duration).setInterpolator(new DecelerateInterpolator()).start();
    }
    // Initialize this in your onCreate or onViewCreated
    private void initTTS() {
        tts = new TextToSpeech(RecallGameActivity.this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true;
            } else {
                Log.e("TTS", "Initialization failed");
            }
        });
    }
    private void initTTS(Runnable onReady) {
        tts = new TextToSpeech(RecallGameActivity.this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true;
//                tts.setSpeechRate(0.8f);
                // NOW the engine is truly ready. Tell the UI to start!
                runOnUiThread(onReady);
            }else{
                Log.e("TTS", "Initialization failed");
            }
        });
    }

    public void generateAndSpeak(String text, String langCode, boolean isQueued) {
        if(!sound){
            return;
        }
        if (!isTtsReady || tts == null) {
            Toast.makeText(RecallGameActivity.this, "TTS not ready yet", Toast.LENGTH_SHORT).show();
            return;
        }

        Locale locale;
        switch (langCode.toLowerCase()) {
            case "bn":
                locale = new Locale("bn", "BD");
                break;
            case "ar":
                locale = new Locale("ar");
                break;
            case "en":
            default:
                locale = Locale.US;
                break;
        }

        int result = tts.setLanguage(locale);

        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {

            Toast.makeText(RecallGameActivity.this, "Language data missing. Please download it in Android Settings.", Toast.LENGTH_LONG).show();
            // This means the user needs to download the voice pack
            Intent installIntent = new Intent();
            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            startActivity(installIntent);
        } else {
            // QUEUE_FLUSH stops any current speech and starts the new one immediately
            // Use QUEUE_ADD if isQueued is true, otherwise use QUEUE_FLUSH
            int queueMode = isQueued ? TextToSpeech.QUEUE_ADD : TextToSpeech.QUEUE_FLUSH;

            tts.speak(text, queueMode, null, "ID_" + text.hashCode());
        }
    }

    private void showPauseMenu() {
        // 1. Inflate the custom layout
        isPaused = true;
        View dialogView = getLayoutInflater().inflate(R.layout.dialogue_pause, null);

        // 2. Build the dialog
        AlertDialog dialog = new MaterialAlertDialogBuilder(this, R.style.CustomDialogTheme)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // 3. Setup Button Clicks
        dialogView.findViewById(R.id.btnResume).setOnClickListener(v -> {

            isPaused = false;
//            timerHandler.postDelayed(timerRunnable, 1000);
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnRestart).setOnClickListener(v -> {
            stopTimer();
            dialog.dismiss();
            handleGameOverForRestart();
            recreate(); // Restarts the activity
        });

        dialogView.findViewById(R.id.btnQuit).setOnClickListener(v -> {
            stopTimer();
            dialog.dismiss();
            handleGameOver(); // Call your results method here
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        dialog.show();
    }

    private void showGameResult() {
        View v = getLayoutInflater().inflate(R.layout.dialogue_result, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this, R.style.FullScreenDialogTheme).setView(v).setCancelable(false).create();

        // Bind Views
        MaterialCardView mainCard = v.findViewById(R.id.mainCard);
        TextView title = v.findViewById(R.id.txtResultTitle);
        MaterialButton btnPlay = v.findViewById(R.id.btnPlayAgain);
        TextView txtResultMessage = v.findViewById(R.id.txtResultMessage);

        // 1. Logic for Daily Task
        TextView valDaily = v.findViewById(R.id.valDaily);
        databaseExecutor.execute(() -> {
                    int completedDaily = myDB.getDailyTargetCompletedCount();

                    runOnUiThread(()->{
                        if (completedDaily >= totalDailyMission) {

                            valDaily.setTextColor(Color.parseColor("#4CAF50"));
                        }
                        valDaily.setText(completedDaily + "/" + totalDailyMission);
                    });
                });

        // 2. Determine Theme
        String themeColor;
        if (score > highestScore) {// highest score
            themeColor = "#FFD700"; // GOLD (New Record)
            title.setText("ALHAMDULILLAH! \uD83C\uDFC6");
            txtResultMessage.setText("Unbelievable! You've reached a new peak.");
        } else if (completedDailyMission > 0) {
            themeColor = "#2E7D32"; // GREEN (Good Progress)
            title.setText("MASHALLAH! ✨");
            txtResultMessage.setText("You mastered " + completedDailyMission + " words in this session.");
        } else {
            themeColor = "#C62828"; // RED (Failed/Zero)
            title.setText("TRY AGAIN! \uD83D\uDCAA");
            txtResultMessage.setText("Don't give up! Practice makes perfect.");
        }

        // Apply Theme Colors
        mainCard.setStrokeColor(ColorStateList.valueOf(Color.parseColor(themeColor)));
        title.setTextColor(Color.parseColor(themeColor));
        btnPlay.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(themeColor)));

        // Set other values
        ((TextView)v.findViewById(R.id.valScore)).setText(String.valueOf(score));
        ((TextView)v.findViewById(R.id.valWords)).setText(String.valueOf(completedDailyMission));
        ((TextView)v.findViewById(R.id.valBest)).setText(String.valueOf(highestScore));

        v.findViewById(R.id.btnPlayAgain).setOnClickListener(view -> { dialog.dismiss(); recreate(); });
        v.findViewById(R.id.btnGoHome).setOnClickListener(view -> { dialog.dismiss(); finish(); });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        }

        dialog.show();

//        if (dialog.getWindow() != null) {
//            dialog.getWindow().setLayout(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.MATCH_PARENT
//            );
//        }
    }

    private void showSettingsDialog() {
        isPaused = true;
        View dialogView = getLayoutInflater().inflate(R.layout.settings_dialog, null);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this, R.style.CustomDialogTheme)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        MaterialSwitch switchSound = dialogView.findViewById(R.id.switchSound);
        MaterialSwitch switchMusic = dialogView.findViewById(R.id.switchMusic);

//        // 1. Load existing settings
//        SharedPreferences prefs = getSharedPreferences("GameSettings", MODE_PRIVATE);
        switchSound.setChecked(sound);
        switchMusic.setChecked(music);

        // 2. Listen for changes
        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
//           prefs.edit().putBoolean("sound_on", isChecked).apply();
            sound = isChecked;
            databaseExecutor.execute(() -> {
                // 1. Do the work (The "Await" part)
                myDB.updateSoundOn(sound);
            });

            // Add code here to mute/unmute your SoundPool
        });

        switchMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            prefs.edit().putBoolean("music_on", isChecked).apply();
            music = isChecked;
            databaseExecutor.execute(() -> {
                // 1. Do the work (The "Await" part)
                myDB.updateMusicOn(music);
            });
            // Add code here to pause/play your MediaPlayer
        });
// 1. Find the Dropdown inside the dialogView
        AutoCompleteTextView dropdown = dialogView.findViewById(R.id.dropdownLearnSound);

// 2. Define your options
        String[] options = {"Always", "New Words", "Off"};

// 3. Create the Adapter (Context must be 'this' or 'RecallGameActivity.this')
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                RecallGameActivity.this,
                android.R.layout.simple_list_item_1,
                options
        );

// 4. Attach the adapter to the dropdown
        dropdown.setAdapter(adapter);

// 5. Set the initial text (from your database/variable)
// For example: dropdown.setText(learnSoundMode, false);

// 6. Handle the selection
        dropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedMode = (String) parent.getItemAtPosition(position);

            // Save to your variable/database
            // learnSoundMode = selectedMode;
//            databaseExecutor.execute(() -> {
////                myDB.updateLearnSoundSetting(selectedMode);
//            });
        });
        dialogView.findViewById(R.id.btnCloseSettings).setOnClickListener(v -> {
            dialog.dismiss();
            isPaused = false;
//            timerHandler.postDelayed(timerRunnable, 1000);

        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        dialog.show();
    }

    private void showDailyMissionDialog(int completedCount, int totalGoal) {
        isPaused = true;
        View v = getLayoutInflater().inflate(R.layout.daily_mission_dialog, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this, R.style.CustomDialogTheme).setView(v).setCancelable(false).create();

        // Calculations
        int remaining = Math.max(0, totalGoal - completedCount);
        int percentage = (int) (((float) completedCount / totalGoal) * 100);

        // Bind Views
        ProgressBar pb = v.findViewById(R.id.missionProgress);
        TextView txtRatio = v.findViewById(R.id.txtMissionRatio);
        TextView txtPercent = v.findViewById(R.id.txtMissionPercent);
        TextView txtCompleted = v.findViewById(R.id.txtCompleted);
        TextView txtRemaining = v.findViewById(R.id.txtLeft);

        // Set Data
        pb.setProgress(percentage);
        txtRatio.setText(completedCount + "/" + totalGoal);
        txtPercent.setText(percentage + "%");
        txtCompleted.setText("Completed: " + completedCount + " words");
        txtRemaining.setText("Remaining: " + remaining + " words");

        v.findViewById(R.id.btnBackMission).setOnClickListener(view -> {
            dialog.dismiss();
            isPaused = false;
//            timerHandler.postDelayed(timerRunnable, 1000);
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        dialog.show();
    }
}