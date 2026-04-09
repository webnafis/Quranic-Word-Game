package com.nsa.hafizq;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.nsa.hafizq.database.ManageDatabase;
import com.nsa.hafizq.database.dto.WordGuessedInfo;
import com.nsa.hafizq.database.entity.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class GuessGameActivity extends BaseActivity {
    private static final int[] COLORS = {
            0xFF1A2A4A,  // 0  Deep Night
            0xFF0D3B6E,  // 1  Pre-Dawn Blue
            0xFF1B6CA8,  // 2  Dawn Break
            0xFF2E9ED4,  // 3  Morning Sky
            0xFF54C4E8,  // 4  Clear Morning
            0xFFD4A020,  // 5  Golden Noon
            0xFFE8781A,  // 6  Afternoon Amber
            0xFFD45A1A,  // 7  Sunset Orange
            0xFFB01850,  // 8  Dusk Red
            0xFF6A18B0,  // 9  Evening Violet
            0xFF2E1A8A,  // 10 Night Indigo
//            0xFF1A2A4A,  // 11 Deep Night (closes loop)
    };
    private static final int[] TEXT_COLORS = {
            0xFF7EB8F7,  // 0  Deep Night       → Soft Star Blue
            0xFF90CCFF,  // 1  Pre-Dawn Blue    → Pale Dawn
            0xFFD0EEFF,  // 2  Dawn Break       → Ice White
            0xFFE8F8FF,  // 3  Morning Sky      → Cloud White
            0xFF0A3D55,  // 4  Clear Morning    → Deep Teal
            0xFF3B2200,  // 5  Golden Noon      → Dark Amber
            0xFF2A1200,  // 6  Afternoon Amber  → Burnt Dark
            0xFFFFE0CC,  // 7  Sunset Orange    → Warm Cream
            0xFFFFB8D0,  // 8  Dusk Red         → Rose Mist
            0xFFE8CCFF,  // 9  Evening Violet   → Lavender Light
            0xFFC4B8FF,  // 10 Night Indigo     → Soft Indigo
//            0xFF7EB8F7,  // 11 Deep Night loop  → Soft Star Blue
    };
    private List<WordGuessedInfo> activeWords;// = new LinkedList<>();
    private Settings settings;
    private WordGuessedInfo chache = null;
    private ProgressBar progressBar;
    private Toast waitToast;
    private boolean isPartButtonWorking = false;
//    private ProgressStateManager stageManager;
    private static class WordModel {
        int id;
        String arabic, bangla;
        ArrayList<String> arParts;
        private boolean read;
        WordModel(int id, String ar, String bn, ArrayList<String> parts) {
            this.id = id; this.arabic = ar; this.bangla = bn;
            this.arParts = parts;
            read = false;
        }
        public boolean isRead(){
            return read;
        }
        public void readTrue(){
            read = true;
        }
    }
    private static class ArPartData{
        int index;
        String text;
        ArPartData(int index, String text){
            this.index = index;
            this.text = text;
        }
    }
    private int firstIdToFetchNextData;
    private int track = -1;
    private int progressMaxLength = 1; //update progress one time cause it is changing at first
    private boolean decreasing = false;
    private int score = 0;
    private int wordsLearned = 0;
    private long gameStartTime;
    private int lives = 5;
    private ImageButton[] lifeIcons;
    private int stage = 0;
    private int notTutorialTrack = -1;
    private int partsTrack = 0;
    private int completedBefore;
    private boolean isTutorial = true; // false;
    private Button nextBtn;
//    private boolean isPractice = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_guess_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        stageManager = new ProgressStateManager((ProgressBar)findViewById(R.id.progressBar_guess));
        progressBar = (ProgressBar)findViewById(R.id.progressBar_guess);
        lifeIcons = new ImageButton[]{
                findViewById(R.id.live5),
                findViewById(R.id.live4), // Note: Fixed your missing '4' from XML
                findViewById(R.id.live3),
                findViewById(R.id.live2),
                findViewById(R.id.live1)
        };
        nextBtn = findViewById(R.id.next);
        nextBtn.setEnabled(false);
        resetProgress();
        gameStartTime = SystemClock.elapsedRealtime();
        executeDB(()->{
            activeWords = db.wordDao().getGuessed_False_From_Start(2);
            settings = db.settingsDao().getSettings(GameName.GUESS.name());
            Integer temp = db.dailyTargetDao().getWordsByDate(GameName.GUESS.name(), ManageDatabase.getTodayDate());
            completedBefore = temp== null? 0: temp;

            if(activeWords.size() < 2){
                decreasing = true;
            }
        }, ()->{
            ((TextView)findViewById(R.id.hScore)).setText("Highest score: "+ settings.highestRecord);
            nextLayout();
//            next();
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next();
            }
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
                showDailyMissionDialog(completedBefore+ wordsLearned, settings.dailyTargetWords);
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showPauseMenu();
            }
        });
    }

    private void next(){
        track++;

//        if(track > 5){
//            track = 0;
////            isPractice = true;
//        }else
        if(track <= progressMaxLength){
//            FrameLayout layoutGuessQuize1 = findViewById(R.id.guess_container_1);
//            View newChildlayout = createQuizCard(activeWords.get(track).bangla, "blue");
//            changeLayout(layoutGuessQuize1, newChildlayout, 800);
//            resetToPractice();
        }else{

            if(track == progressMaxLength+1){
                isTutorial = !isTutorial;
                if(progressMaxLength<9){
                    ((TextView)findViewById(R.id.cardsCount)).setText(1+"/"+1 + " Cards");

                }else{
                    ((TextView)findViewById(R.id.cardsCount)).setText(1+"/"+2 + " Cards");

                }
            }else if(track == progressMaxLength+2){
                //increasing
                if(progressMaxLength< 9){
                    track = 0;
                    isTutorial = !isTutorial;
                    progressMaxLength++;
//                    executeDB(()->{
//                        db.wordDao().getGuessed_False_After_Id(getAfterId(), 2);
//                    });
                }else{
                    ((TextView)findViewById(R.id.cardsCount)).setText(2+"/"+2 + " Cards");

                }
            }else if(track > progressMaxLength+2){
                isTutorial = !isTutorial;
                track = 0;
//                executeDB(()->{
//                    db.wordDao().getGuessed_False_After_Id(getAfterId(), 2);
//                });
            }
        }

        if(isTutorial){
            ((TextView)findViewById(R.id.cardsCount)).setText((track+1)+"/"+(progressMaxLength+1) + " Cards");
        }


        FrameLayout layoutGuessQuize1 = findViewById(R.id.guess_container_1);
        View newChildlayout;
        if(isTutorial){
             newChildlayout = createQuizCard(activeWords.get(track).bangla, "blue");
        }else {
            notTutorialTrack = getRandomZeroUpto(activeWords.size());
             newChildlayout = createQuizCard(activeWords.get(notTutorialTrack).bangla, "blue");

        }

        changeLayout(layoutGuessQuize1, newChildlayout, 800);
        if(isTutorial && activeWords.get(track).guessed){
            nextBtn.setEnabled(false);
            FrameLayout layoutGuessQuize2 = findViewById(R.id.guess_container_2);
            View newChildlayout1 = createQuizCard(activeWords.get(track).arabic, "green");
            changeLayout(layoutGuessQuize2, newChildlayout1, 800);

            // practice again button
            FrameLayout layoutGuessQuize3 = findViewById(R.id.guess_container_3);
            View newChildlayout2 = createQuizCard("Correct! Click here to practice again.", "blue");
            newChildlayout2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetToPractice();
                }
            });
            changeLayout(layoutGuessQuize3, newChildlayout2, 800, ()->{
                nextBtn.setEnabled(true);
            });
        }else{
            resetToPractice();
        }


    }

    private void nextTest(){
        isTutorial = !isTutorial;
//        track++;
        if(isTutorial){
            track++;
        }
        if(track >= activeWords.size()){
            track = 0;
//            isPractice = true;
        }
//        else{
            //changeLayoutZoom(motherLayout, newChildLayout, 400);
//            FrameLayout motherLayout, View newChildLayout,
            FrameLayout layoutGuessQuize1 = findViewById(R.id.guess_container_1);
            View newChildlayout = createQuizCard(activeWords.get(track).bangla, "blue");
            changeLayout(layoutGuessQuize1, newChildlayout, 800);
            resetToPractice();



//        }



    }


    private void resetToPractice(){
        nextBtn.setEnabled(false);

        partsTrack = 0;

        FrameLayout layoutGuessQuize2 = findViewById(R.id.guess_container_2);
        View newChildTable1 = getLayoutInflater().inflate(R.layout.kewboard_container, null);
        changeLayout(layoutGuessQuize2, newChildTable1, 800);

        FrameLayout layoutGuessQuize3 = findViewById(R.id.guess_container_3);
        View newChildTable2 = getLayoutInflater().inflate(R.layout.kewboard_container, null);
        changeLayout(layoutGuessQuize3, newChildTable2, 800, ()->{
            // ✅ Better — get arParts once, reuse it
            ArrayList<String> partsCache = new ArrayList<>(Arrays.asList(activeWords.get(isTutorial ? track : notTutorialTrack).parts.split(",")));
//            int l = partsCache.size();
            ArrayList<ArPartData> parts = new ArrayList<>();
            for(int i = 0; i < partsCache.size() ; i++){
                ArPartData part = new ArPartData(i, partsCache.get(i));
                parts.add(part);
            }



            addButtonsSequentially((TableLayout) newChildTable2, parts,  400, isTutorial, ()->{
//              FrameLayout layoutGuessQuize3 = findViewById(R.id.guess_container_3);
//              View ChildTable2 = layoutGuessQuize3.getChildAt(0);
                if(isTutorial){
                    MaterialButton currBtn = findButtonByTag(newChildTable2, partsTrack);
                    applyThemeToButton(currBtn, "green");
                    currBtn.setEnabled(true);
                }

            });
        });

    }

    // ─── 1. FIND BUTTON BY TAG ─────────────────────────────────────────────────
    private MaterialButton findButtonByTag(View tableLayout, int tag) {
        TableLayout table = (TableLayout) tableLayout;

        for (int r = 0; r < table.getChildCount(); r++) {
            TableRow row = (TableRow) table.getChildAt(r);

            for (int b = 0; b < row.getChildCount(); b++) {
                View child = row.getChildAt(b);

                if (child instanceof MaterialButton &&
                        child.getTag() != null &&
                        (int) child.getTag() == tag) {
                    return (MaterialButton) child;
                }
            }
        }
        return null; // not found
    }


    // ─── 2. APPLY THEME TO BUTTON ──────────────────────────────────────────────
    private void applyThemeToButton(MaterialButton button, String theme) {
        if (button == null) return;

        switch (theme) {
            case "blue":
                button.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.ic_launcher_background)));
                button.setTextColor(
                        ContextCompat.getColor(this, R.color.text_color_light));
                button.setStrokeColor(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.light_blue_600)));
                break;
            case "red":
                button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2A0000")));
                button.setTextColor(Color.parseColor("#F6BABA"));
                button.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E50303")));
                break;
            case "green":
                button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#002A00")));
                button.setTextColor(Color.parseColor("#BAF6BB"));
                button.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#0BE503")));
                break;
            case "yellow":
                button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2A2300")));
                button.setTextColor(Color.parseColor("#F6EEBA"));
                button.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E5B403")));
                break;
        }
    }

//    TableLayout table = findViewById(R.id.yourTableLayout);
//    // Add buttons
//    addButtonToTable(table, createQuizButton("صص", "blue"), 400);
//    addButtonToTable(table, createQuizButton("صص", "red"), 400);
//    addButtonToTable(table, createQuizButton("صص", "green"), 400);
//    // Remove last button
//    removeButtonFromTable(table, 400);


    private void updateStageCard(TextView stages, CardView cardView, int fromColorIndex, int toColorIndex, int stage) {
        // card punches in
        cardView.animate()
                .scaleX(1.05f).scaleY(1.05f)
                .setDuration(120)
                .withEndAction(() -> {
                    // color swap at peak of punch
                    ValueAnimator colorAnim = ValueAnimator.ofObject(
                            new ArgbEvaluator(),
                            COLORS[fromColorIndex],
                            COLORS[toColorIndex]);
                    colorAnim.setDuration(700);
                    colorAnim.setInterpolator(new DecelerateInterpolator());
                    colorAnim.addUpdateListener(a ->
                            cardView.setCardBackgroundColor((int) a.getAnimatedValue()));
                    colorAnim.start();

                    // settle back with overshoot
                    cardView.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(300)
                            .setInterpolator(new OvershootInterpolator(2f))
                            .start();
                }).start();

        // text: scale down → swap → scale up
        stages.animate()
                .scaleX(0f).scaleY(0f)
                .setDuration(150)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    stages.setText("Stage " + stage);
                    stages.setTextColor(TEXT_COLORS[toColorIndex]);
                    stages.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(250)
                            .setInterpolator(new OvershootInterpolator(2.5f))
                            .start();
                }).start();
    }
    private void resetProgress(){
        int colorInd = (stage % COLORS.length);
        int colorNextInd = ((stage+1) % COLORS.length);
        TextView stages = (TextView)findViewById(R.id.stages);
        CardView cardView = (CardView)findViewById(R.id.stages_container);
        updateStageCard(stages, cardView, colorInd, colorNextInd, stage);
        ((TextView)findViewById(R.id.hScore)).setTextColor(TEXT_COLORS[colorNextInd]);
        ((TextView)findViewById(R.id.totalWordCount)).setTextColor(TEXT_COLORS[colorNextInd]);
        ((TextView)findViewById(R.id.scoreCount)).setTextColor(TEXT_COLORS[colorNextInd]);
        ((TextView)findViewById(R.id.cardsCount)).setTextColor(TEXT_COLORS[colorNextInd]);
        ((CardView)findViewById(R.id.c1)).setCardBackgroundColor(COLORS[colorNextInd]);
        ((CardView)findViewById(R.id.c2)).setCardBackgroundColor(COLORS[colorNextInd]);
        ((CardView)findViewById(R.id.c3)).setCardBackgroundColor(COLORS[colorNextInd]);
        ((CardView)findViewById(R.id.c4)).setCardBackgroundColor(COLORS[colorNextInd]);


//        stages.setText("Stage "+ stage);
//        stages.setTextColor(TEXT_COLORS[colorNextInd]);
//        cardView.setCardBackgroundColor(COLORS[colorNextInd]);

        progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(COLORS[colorInd]));
        progressBar.setProgress(0, true);
        progressBar.setProgressTintList(ColorStateList.valueOf(COLORS[colorNextInd]));
    }

    @FunctionalInterface
    interface OnCompleteCallback<T> {
        void run(T result);
    }
    private  void checkAns(){
        FrameLayout targetCon = findViewById(R.id.guess_container_2);
        String ansAr = activeWords.get(notTutorialTrack).arabic;
        StringBuilder given = new StringBuilder();

        TableLayout table = (TableLayout) targetCon.getChildAt(0);
        // disabled parts buttons
        for (int l = 0; l < table.getChildCount(); l++) {
            TableRow rowIn = (TableRow) table.getChildAt(l);
            for (int m = 0; m < rowIn.getChildCount(); m++) {
                MaterialButton childIn = (MaterialButton)rowIn.getChildAt(m);
                childIn.setEnabled(false);
            }
        }

        for (int r = 0; r < table.getChildCount(); r++) {
            TableRow row = (TableRow) table.getChildAt(r);

            for (int b = 0; b < row.getChildCount(); b++) {
                View child = row.getChildAt(b);

                if (child instanceof MaterialButton ) {
//                    given.append(((MaterialButton) child).getText());
                    given.insert(0, ((MaterialButton) child).getText());

                }

            }
        }

        if(!(given.toString().equals(ansAr))){
            decreaseLife();
            showWaitToast(given.toString(), Toast.LENGTH_LONG);
            for (int l = 0; l < table.getChildCount(); l++) {
                TableRow rowIn = (TableRow) table.getChildAt(l);
                for (int m = 0; m < rowIn.getChildCount(); m++) {
                    MaterialButton childIn = (MaterialButton)rowIn.getChildAt(m);
//                            childIn.setEnabled(false);
                    childIn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2A0000")));
                    childIn.setTextColor(Color.parseColor("#F6BABA"));
                    childIn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E50303")));
                }
            }
            FrameLayout layoutGuessQuize3 = findViewById(R.id.guess_container_3);
            View newChildlayout2 = createQuizCard("Wrong Guess!", "red");
            changeLayout(layoutGuessQuize3, newChildlayout2, 800, ()->{
                nextBtn.setEnabled(true);

            });
            return;
        }
        if(progressMaxLength<  9){
            executeDB(()->{
                activeWords.addAll(db.wordDao().getGuessed_False_After_Id(getAfterId(), 2));
            });
        }else if(track == progressMaxLength+1){
            executeDB(()->{
                chache = db.wordDao().getGuessed_False_After_Id(getAfterId(), 1).getFirst();
            });
        }else if (track == progressMaxLength+2) {
            executeDB(() -> {
                if(chache  != null){
                    activeWords.add(chache);
                    chache =  null;
                }
                activeWords.addAll(db.wordDao().getGuessed_False_After_Id(getAfterId(), 1));

            });

        }

        score+=5;
        wordsLearned++;
        if(score> settings.highestRecord){
            settings.highestRecord = score;
//            findViewById(R.id.scoreCount);
//            findViewById(R.id.totalWordCount);
            ((TextView)findViewById(R.id.hScore)).setText("Highest score: "+ score);
            executeDB(()->{
                db.settingsDao().updateHighestRecord(GameName.GUESS.name(), score);
            });
        }
        ((TextView)findViewById(R.id.scoreCount)).setText("Score: "+ score);
        ((TextView)findViewById(R.id.totalWordCount)).setText("Words: "+ wordsLearned);
        executeDB(()->{
            int wordsFinished = db.settingsDao().getWordsFinished(GameName.GUESS.name());
            db.settingsDao().updateWordsFinished(GameName.GUESS.name(), wordsLearned+ wordsFinished);
        });

            int curr = progressBar.getProgress() + 2;
            progressBar.setProgress(curr, true);
            if(curr >= progressBar.getMax()){
                stage++;
                resetProgress();
            }


        FrameLayout layoutGuessQuize2 = findViewById(R.id.guess_container_2);
        View newChildlayout = createQuizCard(activeWords.get(notTutorialTrack).arabic, "green");
        changeLayout(layoutGuessQuize2, newChildlayout, 800);
        FrameLayout layoutGuessQuize3 = findViewById(R.id.guess_container_3);
        View newChildlayout2 = createQuizCard("Correct!", "green");
        changeLayout(layoutGuessQuize3, newChildlayout2, 800, ()->{
            nextBtn.setEnabled(true);

        });
        executeDB(()->{
            db.wordDao().setGuessed(activeWords.get(notTutorialTrack).serialId);

        });
        activeWords.remove(notTutorialTrack);
    }

    private void decreaseLife() {
        if (lives > 0) {
            // doublee sound problem solved i gueww now i have run to check
//            if(!sound){
//                soundPool.play(wrongSound, 1, 1, 0, 0, 1);
//            }

            int iconIndex = lives - 1;

            // 1. Visual feedback on the heart
            lifeIcons[iconIndex].animate()
                    .scaleX(1.5f).scaleY(1.5f).alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> lifeIcons[iconIndex].setVisibility(View.INVISIBLE));

            // 2. Shake the whole layout
            View mainLayout = findViewById(R.id.main_guess_activity);
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
    private int getAfterId(){
        if(activeWords.isEmpty()){
            return 0;
        }else{
            int s = activeWords.size();
            int result = activeWords.get(0).serialId;
            for(int i = 1; i< s; i++) {
                if (result < activeWords.get(i).serialId) {
                    result = activeWords.get(i).serialId;
                }
            }
            return result;
        }
    }

    private void moveButtonBetweenContainers(MaterialButton button, int duration, OnCompleteCallback<FrameLayout> onComplete) {

        // 1. find parent chain: button → TableRow → TableLayout → FrameLayout
        TableRow parentRow   = (TableRow)    button.getParent();
        TableLayout srcTable = (TableLayout) parentRow.getParent();
        FrameLayout srcContainer = (FrameLayout) srcTable.getParent();

        // 2. find target container by checking current parent id
        FrameLayout targetContainer;
        if (srcContainer.getId() == R.id.guess_container_2) {
            targetContainer = findViewById(R.id.guess_container_3);
            if(!(targetContainer.getChildAt(0) instanceof TableLayout)){
                FrameLayout layoutGuessQuize3 = findViewById(R.id.guess_container_3);
                View newChildTable = getLayoutInflater().inflate(R.layout.kewboard_container, null);
                changeLayout(layoutGuessQuize3, newChildTable, duration/3, ()->{
                    TableLayout targetTable = (TableLayout) targetContainer.getChildAt(0);

                    // 4. remove from source using prebuilt function
                    removeButtonFromTable(srcTable, button, duration/2, ()->{
                        addButtonToTable(targetTable, button, duration/2, ()->{
                            if (onComplete != null) onComplete.run(srcContainer);
                        });
                    });
                });

                return;
            }
        } else {
            targetContainer = findViewById(R.id.guess_container_2);
        }

        // 3. get target table from target container
        TableLayout targetTable = (TableLayout) targetContainer.getChildAt(0);

        // 4. remove from source using prebuilt function
        removeButtonFromTable(srcTable, button, duration/2, ()->{
            addButtonToTable(targetTable, button, duration/2, ()->{
                if (onComplete != null) onComplete.run(srcContainer);
            });
        });

    }

    private void removeButtonFromTable(TableLayout table, MaterialButton button, int duration, Runnable onComplete) {
        TableRow row1 = (TableRow) table.getChildAt(0);
        TableRow row2 = (TableRow) table.getChildAt(1);

        // find which row contains the button
        TableRow targetRow = null;
        if (containsChild(row1, button)) targetRow = row1;
        else if (containsChild(row2, button)) targetRow = row2;
        if (targetRow == null) return; // button not found in table

        final TableRow finalTargetRow = targetRow;

        // EXIT animation → then rebalance
        button.animate()
                .scaleX(1.08f).scaleY(1.08f)
                .setDuration(duration / 4)
                .withEndAction(() ->
                        button.animate()
                                .scaleX(0f).scaleY(0f)
                                .alpha(0f)
                                .setDuration(duration / 2)
                                .setInterpolator(new AccelerateInterpolator())
                                .withEndAction(() -> {
                                    finalTargetRow.removeView(button);
                                    rebalanceRows(row1, row2, duration); // ← fix gap after removal
                                    if (onComplete != null) onComplete.run();
                                })
                                .start()
                ).start();
    }
    // ─── REBALANCE: if row1 has space but row2 has elements, move them over ────
    private void rebalanceRows(TableRow row1, TableRow row2, int duration) {
        while (row1.getChildCount() < 6 && row2.getChildCount() > 0) {

            // take first element from row2
            View moving = row2.getChildAt(0);
            row2.removeView(moving);

            // reset animation state
            moving.setScaleX(0f);
            moving.setScaleY(0f);
            moving.setAlpha(0f);
            row1.addView(moving);

            // bounce into new position
            moving.animate()
                    .scaleX(1f).scaleY(1f)
                    .alpha(1f)
                    .setDuration(duration)
                    .setInterpolator(new OvershootInterpolator(2.5f))
                    .start();
        }
    }
    // ─── HELPER: check if a row contains a specific view ───────────────────────
    private boolean containsChild(TableRow row, View target) {
        for (int i = 0; i < row.getChildCount(); i++) {
            if (row.getChildAt(i) == target) return true;
        }
        return false;
    }
    // ─── 1. CREATE BUTTON WITH THEME ───────────────────────────────────────────
    private MaterialButton createQuizButton(String text, String theme, int index) {
        MaterialButton button = (MaterialButton) getLayoutInflater()
                .inflate(R.layout.quize_button, null);

        button.setText(text);
        button.setTag(index);
//        if(partsTrack == index){
//            theme = "green";
//        }
        switch (theme) {
            case "blue":
                button.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.ic_launcher_background)));
                button.setTextColor(
                        ContextCompat.getColor(this, R.color.text_color_light));
                button.setStrokeColor(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.light_blue_600)));
                break;
            case "red":
                button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2A0000")));
                button.setTextColor(Color.parseColor("#F6BABA"));
                button.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E50303")));
                break;
            case "green":
                button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#002A00")));
                button.setTextColor(Color.parseColor("#BAF6BB"));
                button.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#0BE503")));
                break;
            case "yellow":
                button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2A2300")));
                button.setTextColor(Color.parseColor("#F6EEBA"));
                button.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E5B403")));
                break;
        }

        // layout params so it fits correctly inside TableRow
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                0, TableRow.LayoutParams.MATCH_PARENT, 1f);
        button.setLayoutParams(params);

        return button;
    }

    // ─── 2. ADD BUTTON (row1 first → row2 if row1 full) ────────────────────────
    private void addButtonToTable(TableLayout table, MaterialButton button, int duration, Runnable onComplete) {
        TableRow row1 = (TableRow) table.getChildAt(0);
        TableRow row2 = (TableRow) table.getChildAt(1);

        TableRow targetRow = (row1.getChildCount() < 6) ? row1 : row2;
        if (targetRow.getChildCount() >= 6) return; // both rows full

        // start invisible and tiny
        button.setScaleX(0f);
        button.setScaleY(0f);
        button.setAlpha(0f);
//        targetRow.addView(button);
        targetRow.addView(button, 0);
        // ENTER: bounce zoom in
        button.animate()
                .scaleX(1f).scaleY(1f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new OvershootInterpolator(2.5f))
                .withEndAction(()->{
                    if (onComplete != null) onComplete.run();
                })
                .start();
    }

    private int getRandomZeroUpto(int num) {
        Random rand = new Random();
        // nextInt(2) generates a random integer between 0 (inclusive) and upto that num but not the num
        return rand.nextInt(num);
    }

//    duration takes either Toast.LENGTH_SHORT (2s) or Toast.LENGTH_LONG (3.5s) — those are the only two values Android's Toast supports natively.
    private void showWaitToast(String message, int duration) {
        if (waitToast != null) waitToast.cancel();

        View view = getLayoutInflater().inflate(R.layout.toast_wait, null);
        ((TextView) view.findViewById(R.id.toast_text)).setText(message);

        view.setTranslationY(40f);
        view.setAlpha(0f);
        view.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(350)
                .setInterpolator(new OvershootInterpolator(1.8f))
                .start();

        waitToast = new Toast(this);
        waitToast.setDuration(duration);
        waitToast.setView(view);
        waitToast.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL, 0, 180);
        waitToast.show();
    }

    private void hideWaitToast() {
        if (waitToast != null) {
            waitToast.cancel();
            waitToast = null;
        }
    }

    private void addButtonsSequentially(TableLayout table, ArrayList<ArPartData> parts,  int duration, boolean isTutotrial, Runnable lastFunction) {
        if ( parts.isEmpty()){

            if (lastFunction != null) lastFunction.run();
            return; // all buttons added
        }


        int index = getRandomZeroUpto(parts.size());
        MaterialButton button = createQuizButton(parts.get(index).text, "blue", parts.get(index).index);
        parts.remove(index);
        button.setEnabled(!isTutotrial);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPartButtonWorking){
                    // add a toast here
                    showWaitToast("Whoa, speed racer! Give the letters a moment to settle.", Toast.LENGTH_SHORT);
                    return;
                }
                hideWaitToast();
                isPartButtonWorking = true;
                if(isTutotrial){
                    moveButtonBetweenContainers((MaterialButton) v, 400, (srcContainer)->{
                        applyThemeToButton((MaterialButton) v, "blue");
                        ((MaterialButton) v).setEnabled(false);
                        partsTrack++;
                        MaterialButton nxtBtn = findButtonByTag(table, partsTrack);
                        if(nxtBtn != null){
                            applyThemeToButton(nxtBtn, "green");
                            nxtBtn.setEnabled(true);
                            isPartButtonWorking = false;

                        }else{
                            // empty table
                            if(!activeWords.get(track).guessed){
                                // using guessed as is read//// playing double role in ui, and in db
                                activeWords.get(track).guessed = true;
                            }
                            FrameLayout layoutGuessQuize2 = findViewById(R.id.guess_container_2);
                            View newChildlayout1 = createQuizCard(activeWords.get(track).arabic, "green");
                            changeLayout(layoutGuessQuize2, newChildlayout1, 800);

                            // practice again button
                            FrameLayout layoutGuessQuize3 = findViewById(R.id.guess_container_3);
                            View newChildlayout2 = createQuizCard("Correct! Click here to practice again.", "blue");
                            newChildlayout2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    resetToPractice();
                                }
                            });
                            changeLayout(layoutGuessQuize3, newChildlayout2, 800,()->{
                                nextBtn.setEnabled(true);
                                isPartButtonWorking = false;
                            });

                        }

                    });
                }else{
                    moveButtonBetweenContainers((MaterialButton) v, 400, (FrameLayout srcContainer)->{
                        // is empty when sending

                        if((srcContainer.getId() == R.id.guess_container_3) && ((TableRow)( ((TableLayout)srcContainer.getChildAt(0)).getChildAt(0))).getChildCount() == 0){
                            FrameLayout layoutGuessQuize3 = findViewById(R.id.guess_container_3);
                            View newChildlayout2 = createQuizCard("Sure? Click here to check it.", "blue");
                            newChildlayout2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    checkAns();
                                }
                            });
                            layoutGuessQuize3.removeAllViews();
                            // take time 400milisec
                            changeLayout(layoutGuessQuize3, newChildlayout2, 800, ()->{
                                isPartButtonWorking = false;
                            });
                        }else{
                            isPartButtonWorking = false;
                        }
                    });
                }

            }
        });
        // override addButtonToTable to have a callback
        button.setScaleX(0f);
        button.setScaleY(0f);
        button.setAlpha(0f);

        TableRow row1 = (TableRow) table.getChildAt(0);
        TableRow row2 = (TableRow) table.getChildAt(1);
        TableRow targetRow = (row1.getChildCount() < 6) ? row1 : row2;
        if (targetRow.getChildCount() >= 6) return; // both rows full
//        targetRow.addView(button);
        targetRow.addView(button, 0);

        button.animate()
                .scaleX(1f).scaleY(1f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new OvershootInterpolator(2.5f))
                .withEndAction(() ->
                        addButtonsSequentially(table, parts, duration, isTutotrial, lastFunction) // ← next button
                )
                .start();
    }
    private View createQuizCard(String word, String theme) {
        View view = getLayoutInflater().inflate(R.layout.guess_card, null);
        MaterialCardView card = (MaterialCardView) view;
        TextView tvWord = view.findViewById(R.id.tvWord);

        switch (theme) {
            case "blue":
                card.setCardBackgroundColor(Color.parseColor("#0D1631"));
                card.setStrokeColor(ContextCompat.getColor(this, R.color.light_blue_600));
                tvWord.setTextColor(ContextCompat.getColor(this, R.color.text_color_light));
                break;
            case "red":
                card.setCardBackgroundColor(Color.parseColor("#1F0000"));
                card.setStrokeColor(Color.parseColor("#E50303"));
                tvWord.setTextColor(Color.parseColor("#F6BABA"));
                break;
            case "green":
                card.setCardBackgroundColor(Color.parseColor("#001F00"));
                card.setStrokeColor(Color.parseColor("#0BE503"));
                tvWord.setTextColor(Color.parseColor("#BAF6BB"));
                break;
            case "yellow":
                card.setCardBackgroundColor(Color.parseColor("#1F1800"));
                card.setStrokeColor(Color.parseColor("#E5B403"));
                tvWord.setTextColor(Color.parseColor("#F6EEBA"));
                break;
        }

        tvWord.setText(word);
        return view;
    }
    private void nextLayout() {

        FrameLayout layoutGuessQuize = findViewById(R.id.guess_quize_layout);
        View newChildlayout = getLayoutInflater().inflate(R.layout.guess_layout, null);

        changeLayout(layoutGuessQuize, newChildlayout, -90f, 1000f, 600);
    }

    private void changeLayout(FrameLayout motherLayout, View newChildLayout, float rotY,  float translX , int duration){
        for (int i = 0; i < motherLayout.getChildCount(); i++) {
            View childLayout = motherLayout.getChildAt(i);
            childLayout.animate().rotationY(rotY).alpha(0f).setDuration(duration/2).withEndAction(() -> motherLayout.removeView(childLayout));
        }
        // Initial position: Off-screen to the right
        newChildLayout.setTranslationX(translX);
        motherLayout.addView(newChildLayout);
        // 4. Animate NEW cards in (Slide from right)
        newChildLayout.animate().translationX(0f).setDuration(duration/2).withEndAction(()->{
            next();
        }).setInterpolator(new DecelerateInterpolator()).start();
    }

    //// instead of changeLayout(...)
    //changeLayoutZoom(motherLayout, newChildLayout, 400);

    private void changeLayout(FrameLayout motherLayout, View newChildLayout, int duration) {

        int childCount = motherLayout.getChildCount();

        if (childCount == 0) {
            // Nothing to remove, just add directly
            newChildLayout.setScaleX(0f);
            newChildLayout.setScaleY(0f);
            newChildLayout.setAlpha(0f);
            motherLayout.addView(newChildLayout);
            newChildLayout.animate()
                    .scaleX(1f).scaleY(1f).alpha(1f)
                    .setDuration(duration/2)
                    .setInterpolator(new OvershootInterpolator(2.5f))
                    .start();
            return;
        }

        // Count how many exit animations need to finish
        int[] remaining = {childCount};

        for (int i = 0; i < childCount; i++) {
            View childLayout = motherLayout.getChildAt(i);

            childLayout.animate()
                    .scaleX(1.08f).scaleY(1.08f)
                    .setDuration(duration / 6)
                    .withEndAction(() ->
                            childLayout.animate()
                                    .scaleX(0f).scaleY(0f)
                                    .alpha(0f)
                                    .setDuration(duration / 3)
                                    .setInterpolator(new AccelerateInterpolator())
                                    .withEndAction(() -> {
                                        motherLayout.removeView(childLayout);
                                        remaining[0]--;

                                        // ✅ Only add new view after ALL old views are removed
                                        if (remaining[0] == 0) {
                                            newChildLayout.setScaleX(0f);
                                            newChildLayout.setScaleY(0f);
                                            newChildLayout.setAlpha(0f);
                                            motherLayout.addView(newChildLayout);
                                            newChildLayout.animate()
                                                    .scaleX(1f).scaleY(1f).alpha(1f)
                                                    .setDuration(duration/2)
                                                    .setInterpolator(new OvershootInterpolator(2.5f))
                                                    .start();
                                        }
                                    })
                                    .start()
                    ).start();
        }
    }
// Want stronger bounce?
// Increase OvershootInterpolator value → 3.5f
// Want faster exit?
// Decrease duration / 2 to duration / 3
// Want overlap between exit and enter?
// Decrease setStartDelay
// Want no overlap?
// Change delay to full duration
private void changeLayout(FrameLayout motherLayout, View newChildLayout, int duration, Runnable onComplete) {

    int childCount = motherLayout.getChildCount();

    if (childCount == 0) {
        newChildLayout.setScaleX(0f);
        newChildLayout.setScaleY(0f);
        newChildLayout.setAlpha(0f);
        motherLayout.addView(newChildLayout);
        newChildLayout.animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(duration/2)
                .setInterpolator(new OvershootInterpolator(2.5f))
                .withEndAction(() -> {
                    if (onComplete != null) onComplete.run();
                })
                .start();
        return;
    }

    int[] remaining = {childCount};

    for (int i = 0; i < childCount; i++) {
        View childLayout = motherLayout.getChildAt(i);

        childLayout.animate()
                .scaleX(1.08f).scaleY(1.08f)
                .setDuration(duration / 6)
                .withEndAction(() ->
                        childLayout.animate()
                                .scaleX(0f).scaleY(0f)
                                .alpha(0f)
                                .setDuration(duration / 3)
                                .setInterpolator(new AccelerateInterpolator())
                                .withEndAction(() -> {
                                    motherLayout.removeView(childLayout);
                                    remaining[0]--;

                                    // ✅ Only add new view after ALL old views are removed
                                    if (remaining[0] == 0) {
                                        newChildLayout.setScaleX(0f);
                                        newChildLayout.setScaleY(0f);
                                        newChildLayout.setAlpha(0f);
                                        motherLayout.addView(newChildLayout);
                                        newChildLayout.animate()
                                                .scaleX(1f).scaleY(1f).alpha(1f)
                                                .setDuration(duration/2)
                                                .setInterpolator(new OvershootInterpolator(2.5f))
                                                .withEndAction(() -> {
                                                    if (onComplete != null) onComplete.run();
                                                })
                                                .start();
                                    }
                                })
                                .start()
                ).start();
    }
}


    private void showSettingsDialog() {
//        isPaused = true;
        View dialogView = getLayoutInflater().inflate(R.layout.settings_dialog, null);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this, R.style.CustomDialogTheme)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        MaterialSwitch switchSound = dialogView.findViewById(R.id.switchSound);
        MaterialSwitch switchMusic = dialogView.findViewById(R.id.switchMusic);

//        // 1. Load existing settings
//        SharedPreferences prefs = getSharedPreferences("GameSettings", MODE_PRIVATE);
        switchSound.setChecked(settings.soundOn );
        switchMusic.setChecked(settings.musicOn );
        SeekBar sound = ((SeekBar)dialogView.findViewById(R.id.seekBarSound));
        SeekBar music = ((SeekBar)dialogView.findViewById(R.id.seekBarMusic));
        sound.setMax(100);
        music.setMax(100);
        sound.setProgress(settings.soundVolumn);
        music.setProgress(settings.musicVolumn);

        // 2. Listen for changes
        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
//           prefs.edit().putBoolean("sound_on", isChecked).apply();
            settings.soundOn = isChecked;
            // Add code here to mute/unmute your SoundPool
        });

        switchMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            prefs.edit().putBoolean("music_on", isChecked).apply();
            settings.musicOn = isChecked;
            // Add code here to pause/play your MediaPlayer
        });

        sound.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                settings.soundVolumn = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        music.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                settings.musicVolumn = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
// 1. Find the Dropdown inside the dialogView
        dialogView.findViewById(R.id.btnCloseSettings).setOnClickListener(v -> {
            executeDB(()->{
                db.settingsDao().updateAllAudioSettings(GameName.GUESS.name(), settings.soundOn, settings.musicOn, settings.soundVolumn, settings.musicVolumn);
            });
            dialog.dismiss();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        dialog.show();
    }
    private void showPauseMenu() {
        // 1. Inflate the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialogue_pause, null);
        // 2. Build the dialog
        AlertDialog dialog = new MaterialAlertDialogBuilder(this, R.style.CustomDialogTheme)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // 3. Setup Button Clicks
        dialogView.findViewById(R.id.btnResume).setOnClickListener(v -> {

//            isPaused = false;
//            timerHandler.postDelayed(timerRunnable, 1000);
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnRestart).setOnClickListener(v -> {
//            stopTimer();
            dialog.dismiss();
            handleGameOverForRestart();
            recreate(); // Restarts the activity
        });

        dialogView.findViewById(R.id.btnQuit).setOnClickListener(v -> {
//            stopTimer();
            dialog.dismiss();
            handleGameOver(); // Call your results method here
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        dialog.show();
    }

    private void handleGameOverForRestart() {
        nextBtn.setEnabled(false);
//        int totalW;
        executeDB(()->{
//            gameStartTime
            long totalTime = SystemClock.elapsedRealtime()- gameStartTime;
            db.dailyTargetDao().upsertDailyStats(GameName.GUESS.name(), ManageDatabase.getTodayDate(), totalTime, wordsLearned, score);
//            int totalW = db.dailyTargetDao().getWordsByDate(GameName.GUESS.name(), ManageDatabase.getTodayDate());
        });
    }

    private void handleGameOver() {
        nextBtn.setEnabled(false);
//        int totalW;
        executeDB(()->{
//            gameStartTime
            long totalTime = SystemClock.elapsedRealtime()- gameStartTime;
            db.dailyTargetDao().upsertDailyStats(GameName.GUESS.name(), ManageDatabase.getTodayDate(), totalTime, wordsLearned, score);
//            int totalW = db.dailyTargetDao().getWordsByDate(GameName.GUESS.name(), ManageDatabase.getTodayDate());
//            int totalW = db.dailyTargetDao().getWordsByDate(GameName.GUESS.name(), ManageDatabase.getTodayDate());

        });
        showGameResult();

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


        // 2. Determine Theme
        String themeColor;
        if (score == settings.highestRecord) {// highest score
            themeColor = "#FFD700"; // GOLD (New Record)
            title.setText("ALHAMDULILLAH! \uD83C\uDFC6");
            txtResultMessage.setText("Unbelievable! You've reached a new peak.");
        } else if (wordsLearned > 0) {
            themeColor = "#2E7D32"; // GREEN (Good Progress)
            title.setText("MASHALLAH! ✨");
            txtResultMessage.setText("You mastered " + wordsLearned + " words in this session.");
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
        ((TextView)v.findViewById(R.id.valWords)).setText(String.valueOf(wordsLearned));
        ((TextView)v.findViewById(R.id.valBest)).setText(String.valueOf(settings.highestRecord));

        v.findViewById(R.id.btnPlayAgain).setOnClickListener(view -> { dialog.dismiss(); recreate(); });
        v.findViewById(R.id.btnGoHome).setOnClickListener(view -> { dialog.dismiss(); finish(); });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        }

        if (completedBefore+ wordsLearned >= settings.dailyTargetWords) {

            valDaily.setTextColor(Color.parseColor("#4CAF50"));
        }
        valDaily.setText(completedBefore+ wordsLearned + "/" + settings.dailyTargetWords);
        dialog.show();



//        if (dialog.getWindow() != null) {
//            dialog.getWindow().setLayout(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.MATCH_PARENT
//            );
//        }
    }

    private void showDailyMissionDialog(int completedCount, int totalGoal) {
//        isPaused = true;
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
//            isPaused = false;
//            timerHandler.postDelayed(timerRunnable, 1000);
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        dialog.show();
    }
}


