package com.nsa.hafizq;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class GuessGameActivity extends BaseActivity {
    private LinkedList<GuessGameActivity.WordModel> activeWords = new LinkedList<>();
    private static class WordModel {
        int id;
        String arabic, bangla;
        ArrayList<String> arParts;
        WordModel(int id, String ar, String bn, ArrayList<String> parts) {
            this.id = id; this.arabic = ar; this.bangla = bn;
            this.arParts = parts;
        }
    }
    private int firstIdToFetchNextData;
    private int track = 0;
    private int partsTrack = 0;
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

        loadSampleData();
        nextLayout();
        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextTest();
            }
        });
    }

    private void next(){
        track++;

        if(track > 5){
            track = 0;
//            isPractice = true;
        }else{
            //changeLayoutZoom(motherLayout, newChildLayout, 400);
//            FrameLayout motherLayout, View newChildLayout,
            FrameLayout layoutGuessQuize1 = findViewById(R.id.guess_container_1);
            View newChildlayout = createQuizCard(activeWords.get(track).bangla, "blue");
            changeLayout(layoutGuessQuize1, newChildlayout, 600);
            resetToPractice();



        }



    }

    private void nextTest(){
        track++;

        if(track >= activeWords.size()){
            track = 0;
//            isPractice = true;
        }
//        else{
            //changeLayoutZoom(motherLayout, newChildLayout, 400);
//            FrameLayout motherLayout, View newChildLayout,
            FrameLayout layoutGuessQuize1 = findViewById(R.id.guess_container_1);
            View newChildlayout = createQuizCard(activeWords.get(track).bangla, "blue");
            changeLayout(layoutGuessQuize1, newChildlayout, 600);
            resetToPractice();



//        }



    }

    private void loadSampleData() {
        activeWords.clear();
        int id = 0;

        // joining all parts must equal the original arabic string exactly
        activeWords.add(new WordModel(id++, "أُمَّةً أُمَم",        "সম্প্রদায়",                new ArrayList<>(Arrays.asList("أُمَّ",   "ةً ",    "أُمَم"))));
        activeWords.add(new WordModel(id++, "قَوْم",                "জাতি",                    new ArrayList<>(Arrays.asList("قَوْ",   "م"))));
        activeWords.add(new WordModel(id++, "فَرِيقٌ طَائِفَةٌ",   "দল",                      new ArrayList<>(Arrays.asList("فَرِي",  "قٌ ",    "طَائِ",  "فَةٌ"))));
        activeWords.add(new WordModel(id++, "فِئَةٌ",               "দল, গোষ্ঠী",              new ArrayList<>(Arrays.asList("فِئَ",   "ةٌ"))));
        activeWords.add(new WordModel(id++, "مَعْشَرُ",             "দল, সম্প্রদায়, সমাজ",    new ArrayList<>(Arrays.asList("مَعْ",   "شَرُ"))));
        activeWords.add(new WordModel(id++, "إِنْسَانُ",            "মানুষ",                   new ArrayList<>(Arrays.asList("إِنْ",   "سَا",    "نُ"))));
        activeWords.add(new WordModel(id++, "ناس",                  "মানুষ, লোকজন",            new ArrayList<>(Arrays.asList("نا",     "س"))));
        activeWords.add(new WordModel(id++, "ذكر (ذكور)",           "পুরুষ",                   new ArrayList<>(Arrays.asList("ذكر ",   "(ذكو",   "ر)"))));
        activeWords.add(new WordModel(id++, "أُنْثَى (إِنَاتُ)",   "মহিলা",                   new ArrayList<>(Arrays.asList("أُنْ",   "ثَى ",   "(إِنَا", "تُ)"))));
        activeWords.add(new WordModel(id++, "زوج (أزواج)",          "স্বামী",                  new ArrayList<>(Arrays.asList("زوج ",   "(أزوا",  "ج)"))));
        activeWords.add(new WordModel(id++, "عَبْدُ عِبَادُ)",     "বান্দা",                  new ArrayList<>(Arrays.asList("عَبْ",   "دُ ",    "عِبَا",  "دُ)"))));
        activeWords.add(new WordModel(id++, "عَدُوٌّ أَعْدَاءُ)",  "শত্রু",                   new ArrayList<>(Arrays.asList("عَدُ",   "وٌّ ",   "أَعْدَا","ءُ)"))));
        activeWords.add(new WordModel(id++, "كفار",                 "অবিশ্বাসী",               new ArrayList<>(Arrays.asList("كفا",    "ر"))));
        activeWords.add(new WordModel(id++, "مُجْرِم",              "অপরাধী",                  new ArrayList<>(Arrays.asList("مُجْ",   "رِم"))));
        activeWords.add(new WordModel(id++, "ملا",                  "নেতৃস্থানীয়, অধিনায়ক",  new ArrayList<>(Arrays.asList("مل",     "ا"))));
        activeWords.add(new WordModel(id++, "وَلِيٌّ أَوْلِيَاءُ)","অভিভাবক",                new ArrayList<>(Arrays.asList("وَلِ",   "يٌّ ",   "أَوْلِيَا","ءُ)"))));
        activeWords.add(new WordModel(id++, "خَلِيفَةً",            "প্রতিনিধি, স্থলাভিষিক্ত",new ArrayList<>(Arrays.asList("خَلِي",  "فَةً"))));
    }
    private void resetToPractice(){
        partsTrack = 0;

        FrameLayout layoutGuessQuize2 = findViewById(R.id.guess_container_2);
        View newChildTable1 = getLayoutInflater().inflate(R.layout.kewboard_container, null);
        changeLayout(layoutGuessQuize2, newChildTable1, 600);

        FrameLayout layoutGuessQuize3 = findViewById(R.id.guess_container_3);
        View newChildTable2 = getLayoutInflater().inflate(R.layout.kewboard_container, null);
        changeLayout(layoutGuessQuize3, newChildTable2, 600, ()->{
            // ✅ Better — get arParts once, reuse it
            ArrayList<String> parts = activeWords.get(track).arParts;


            addButtonsSequentially((TableLayout) newChildTable2, parts, 0, 400, true, ()->{
//                    FrameLayout layoutGuessQuize3 = findViewById(R.id.guess_container_3);
//                    View ChildTable2 = layoutGuessQuize3.getChildAt(0);
                MaterialButton currBtn = findButtonByTag(newChildTable2, partsTrack);
                applyThemeToButton(currBtn, "green");
                currBtn.setEnabled(true);
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

    private void moveButtonBetweenContainers(MaterialButton button, int duration) {

        // 1. find parent chain: button → TableRow → TableLayout → FrameLayout
        TableRow parentRow   = (TableRow)    button.getParent();
        TableLayout srcTable = (TableLayout) parentRow.getParent();
        FrameLayout srcContainer = (FrameLayout) srcTable.getParent();

        // 2. find target container by checking current parent id
        FrameLayout targetContainer;
        if (srcContainer.getId() == R.id.guess_container_2) {
            targetContainer = findViewById(R.id.guess_container_3);
        } else {
            targetContainer = findViewById(R.id.guess_container_2);
        }

        // 3. get target table from target container
        TableLayout targetTable = (TableLayout) targetContainer.getChildAt(0);

        // 4. remove from source using prebuilt function
        removeButtonFromTable(srcTable, button, duration, ()->{
            addButtonToTable(targetTable, button, duration);
        });

    }

    private void moveButtonBetweenContainers(MaterialButton button, int duration, Runnable onComplete) {

        // 1. find parent chain: button → TableRow → TableLayout → FrameLayout
        TableRow parentRow   = (TableRow)    button.getParent();
        TableLayout srcTable = (TableLayout) parentRow.getParent();
        FrameLayout srcContainer = (FrameLayout) srcTable.getParent();

        // 2. find target container by checking current parent id
        FrameLayout targetContainer;
        if (srcContainer.getId() == R.id.guess_container_2) {
            targetContainer = findViewById(R.id.guess_container_3);
        } else {
            targetContainer = findViewById(R.id.guess_container_2);
        }

        // 3. get target table from target container
        TableLayout targetTable = (TableLayout) targetContainer.getChildAt(0);

        // 4. remove from source using prebuilt function
        removeButtonFromTable(srcTable, button, duration, ()->{
            addButtonToTable(targetTable, button, duration, ()->{
                if (onComplete != null) onComplete.run();
            });
        });

    }
    private void removeButtonFromTable(TableLayout table, MaterialButton button, int duration) {
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
                                })
                                .start()
                ).start();
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
    private void addButtonToTable(TableLayout table, MaterialButton button, int duration) {
        TableRow row1 = (TableRow) table.getChildAt(0);
        TableRow row2 = (TableRow) table.getChildAt(1);

        TableRow targetRow = (row1.getChildCount() < 6) ? row1 : row2;
        if (targetRow.getChildCount() >= 6) return; // both rows full

        // start invisible and tiny
        button.setScaleX(0f);
        button.setScaleY(0f);
        button.setAlpha(0f);
        targetRow.addView(button);

        // ENTER: bounce zoom in
        button.animate()
                .scaleX(1f).scaleY(1f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new OvershootInterpolator(2.5f))
                .start();
    }
    private void addButtonToTable(TableLayout table, MaterialButton button, int duration, Runnable onComplete) {
        TableRow row1 = (TableRow) table.getChildAt(0);
        TableRow row2 = (TableRow) table.getChildAt(1);

        TableRow targetRow = (row1.getChildCount() < 6) ? row1 : row2;
        if (targetRow.getChildCount() >= 6) return; // both rows full

        // start invisible and tiny
        button.setScaleX(0f);
        button.setScaleY(0f);
        button.setAlpha(0f);
        targetRow.addView(button);

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

    private void addButtonsSequentially(TableLayout table, ArrayList<String> parts, int index, int duration, boolean isTutotrial, Runnable lastFunction) {
        if (index >= parts.size()){

            if (lastFunction != null) lastFunction.run();
            return; // all buttons added
        }

        MaterialButton button = createQuizButton(parts.get(index), "blue", index);
        button.setEnabled(!isTutotrial);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isTutotrial){
                    moveButtonBetweenContainers((MaterialButton) v, 400, ()->{
                        applyThemeToButton((MaterialButton) v, "blue");
                        ((MaterialButton) v).setEnabled(false);
                        partsTrack++;
                        MaterialButton nxtBtn = findButtonByTag(table, partsTrack);
                        if(nxtBtn != null){
                            applyThemeToButton(nxtBtn, "green");
                            nxtBtn.setEnabled(true);

                        }else{
                            // empty table
                            FrameLayout layoutGuessQuize2 = findViewById(R.id.guess_container_2);
                            View newChildlayout1 = createQuizCard(activeWords.get(track).arabic, "green");
                            changeLayout(layoutGuessQuize2, newChildlayout1, 600);

                            // practice again button
                            FrameLayout layoutGuessQuize3 = findViewById(R.id.guess_container_3);
                            View newChildlayout2 = createQuizCard("Correct! Click here to practice again.", "blue");
                            newChildlayout2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    resetToPractice();
                                }
                            });
                            changeLayout(layoutGuessQuize3, newChildlayout2, 600);

                        }

                    });
                }else{
                    moveButtonBetweenContainers((MaterialButton) v, 400, null);
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
        targetRow.addView(button);

        button.animate()
                .scaleX(1f).scaleY(1f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new OvershootInterpolator(2.5f))
                .withEndAction(() ->
                        addButtonsSequentially(table, parts, index + 1, duration, isTutotrial, lastFunction) // ← next button
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
            childLayout.animate().rotationY(rotY).alpha(0f).setDuration(duration).withEndAction(() -> motherLayout.removeView(childLayout));
        }
        // Initial position: Off-screen to the right
        newChildLayout.setTranslationX(translX);
        motherLayout.addView(newChildLayout);
        // 4. Animate NEW cards in (Slide from right)
        newChildLayout.animate().translationX(0f).setDuration(duration).setInterpolator(new DecelerateInterpolator()).start();
    }

    //// instead of changeLayout(...)
    //changeLayoutZoom(motherLayout, newChildLayout, 400);
    private void changeLayout(FrameLayout motherLayout, View newChildLayout, int duration) {

        // EXIT: current views zoom in slightly → then zoom out to vanish
        for (int i = 0; i < motherLayout.getChildCount(); i++) {
            View childLayout = motherLayout.getChildAt(i);

            childLayout.animate()
                    .scaleX(1.08f).scaleY(1.08f)   // zoom in a little
                    .setDuration(duration / 4)
                    .withEndAction(() ->
                            childLayout.animate()
                                    .scaleX(0f).scaleY(0f)  // zoom out to nothing
                                    .alpha(0f)
                                    .setDuration(duration / 2)
                                    .setInterpolator(new AccelerateInterpolator())
                                    .withEndAction(() -> motherLayout.removeView(childLayout))
                                    .start()
                    ).start();
        }

        // ENTER: start tiny → overshoot big → settle to normal (bounce)
        newChildLayout.setScaleX(0f);
        newChildLayout.setScaleY(0f);
        newChildLayout.setAlpha(0f);
        motherLayout.addView(newChildLayout);

        newChildLayout.animate()
                .scaleX(1f).scaleY(1f)
                .alpha(1f)
                .setDuration(duration)
                .setStartDelay(duration / 3)        // wait for exit to start
                .setInterpolator(new OvershootInterpolator(2.5f)) // controls bounce strength
                .start();
    }

// without callback — pass null
//    changeLayout(motherLayout, newView, 400, null);
//
//    // with callback — runs after animation finishes
//    changeLayout(motherLayout, newView, 400, () -> {
//        // do something after animation
//        loadNextQuestion();
//        updateScore();
//    });
    private void changeLayout(FrameLayout motherLayout, View newChildLayout, int duration, Runnable onComplete) {

        // EXIT: current views zoom in slightly → then zoom out to vanish
        for (int i = 0; i < motherLayout.getChildCount(); i++) {
            View childLayout = motherLayout.getChildAt(i);

            childLayout.animate()
                    .scaleX(1.08f).scaleY(1.08f)
                    .setDuration(duration / 4)
                    .withEndAction(() ->
                            childLayout.animate()
                                    .scaleX(0f).scaleY(0f)
                                    .alpha(0f)
                                    .setDuration(duration / 2)
                                    .setInterpolator(new AccelerateInterpolator())
                                    .withEndAction(() -> motherLayout.removeView(childLayout))
                                    .start()
                    ).start();
        }

        // ENTER: start tiny → overshoot big → settle to normal (bounce)
        newChildLayout.setScaleX(0f);
        newChildLayout.setScaleY(0f);
        newChildLayout.setAlpha(0f);
        motherLayout.addView(newChildLayout);

        newChildLayout.animate()
                .scaleX(1f).scaleY(1f)
                .alpha(1f)
                .setDuration(duration)
                .setStartDelay(duration / 3)
                .setInterpolator(new OvershootInterpolator(2.5f))
                .withEndAction(() -> {
                    if (onComplete != null) onComplete.run(); // ← runs after everything
                })
                .start();
    }

}


