package com.nsa.hafizq;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ManageDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "MAIN_QURANIC_WORD_GAME_DATABASE_OFFLINE";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME_WORDS = "ARABIC_BANGLA_WORDS";
    public static final String COLUMN_1_SEL_ID = "SERIAL_ID";
    public static final String COLUMN_2_ARABIC = "ARABIC";
    public static final String COLUMN_3_BANGLA = "BANGLA";
    public static final String COLUMN_4_WRONG = "WRONG_NUMBER";
    public static final String COLUMN_5_CORRECT = "CORRECT_NUMBER";



    public static final String TABLE_NAME_SETTINGS = "SETTINGS";
    public static final String COLUMN_1_LAST_STARTING_WORD_ID = "LAST_STARTING_WORD_ID";
    public static final String COLUMN_2_SOUND_ON = "SOUND_ON";
    public static final String COLUMN_3_MUSIC_ON = "MUSIC_ON";
    public static final String COLUMN_4_DAILY_TARGET = "DAILY_TARGET";
    public static final String COLUMN_5_DAILY_TARGET_COMPLETED_WORDS_COUNT = "DAILY_TARGET_COMPLETED_WORDS_COUNT";




    public static final String TABLE_NAME_USER = "USER";
    public static final String COLUMN_1_WORDS_FINISHED = "WORDS_FINISHED";
    public static final String COLUMN_2_TOTAL_WORDS = "TOTAL_WORDS";
    public static final String COLUMN_3_HIGHEST_RECORD = "HIGHEST_RECORD";


    private static final String CREATE_TABLE_WORDS = "CREATE TABLE " + TABLE_NAME_WORDS + " (" +
            COLUMN_1_SEL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_2_ARABIC + " TEXT, " +
            COLUMN_3_BANGLA + " TEXT, " +
            COLUMN_4_WRONG + " INTEGER, " +
            COLUMN_5_CORRECT + " INTEGER);";
    private static final String CREATE_TABLE_SETTINGS = "CREATE TABLE " + TABLE_NAME_SETTINGS + " (" +
            COLUMN_1_LAST_STARTING_WORD_ID + " INTEGER, "+
            COLUMN_2_SOUND_ON + " BOOLEAN, " +
            COLUMN_3_MUSIC_ON + " BOOLEAN, " +
            COLUMN_4_DAILY_TARGET + " INTEGER, " +
            COLUMN_5_DAILY_TARGET_COMPLETED_WORDS_COUNT + " INTEGER);";


    private static final String CREATE_TABLE_USER = "CREATE TABLE " + TABLE_NAME_USER + " (" +
            COLUMN_1_WORDS_FINISHED + " INTEGER, " +
            COLUMN_2_TOTAL_WORDS + " INTEGER, " +
            COLUMN_3_HIGHEST_RECORD + " INTEGER);";

    private Context context;

    public ManageDatabase(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context; // Save context to access assets later
//        boolean dev = false;
//        if(dev){
//            context.deleteDatabase(DATABASE_NAME);
//
//        }
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_WORDS);
        db.execSQL(CREATE_TABLE_SETTINGS);
        db.execSQL(CREATE_TABLE_USER);

        // Initialize one row for Settings and User
        db.execSQL("INSERT INTO " + TABLE_NAME_SETTINGS + " VALUES (1, 1, 1, 10, 0)");
        db.execSQL("INSERT INTO " + TABLE_NAME_USER + " VALUES (0, 0, 0)");

        // Insert the JSON data
        insertInitialWordsFromAssets(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_WORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_SETTINGS);
        //for development
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_USER);
        onCreate(db);
    }

    private void insertInitialWordsFromAssets(SQLiteDatabase db) {
        String jsonString = null;
        try {
            // 1. Read the file from Assets
            java.io.InputStream is = context.getAssets().open("words.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, "UTF-8");

            // 2. Parse and Insert
            org.json.JSONArray jsonArray = new org.json.JSONArray(jsonString);

            db.beginTransaction();
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    org.json.JSONObject obj = jsonArray.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_2_ARABIC, obj.getString("arabic"));
                    values.put(COLUMN_3_BANGLA, obj.getString("bangla"));
                    values.put(COLUMN_4_WRONG, 0);
                    values.put(COLUMN_5_CORRECT, 0);

                    db.insert(TABLE_NAME_WORDS, null, values);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (java.io.IOException | org.json.JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean insertRowDataWords(String arabic, String bangla, int wrong, int correct){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_2_ARABIC, arabic);
        contentValues.put(COLUMN_3_BANGLA, bangla);
        contentValues.put(COLUMN_4_WRONG, wrong);
        contentValues.put(COLUMN_5_CORRECT, correct);

        long result = db.insert(TABLE_NAME_WORDS, null, contentValues);
        db.close();
        return result !=-1;

    }

    public Cursor getAllDataWords(){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME_WORDS, null);
        return cursor;
    }

    public boolean updateRowDataWord(String id, String arabic, String bangla, int wrong, int correct) {
        //if want to be flexible than take contentValues as parameter

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_2_ARABIC, arabic);
        contentValues.put(COLUMN_3_BANGLA, bangla);
        contentValues.put(COLUMN_4_WRONG, wrong);
        contentValues.put(COLUMN_5_CORRECT, correct);

        int rowsAffected = db.update(TABLE_NAME_WORDS, contentValues, COLUMN_1_SEL_ID+" =? ",   new String[] { id });
        db.close();
        return rowsAffected > 0 ;


    }
    public Integer deleteRowDataWord(String id ){
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_NAME_WORDS, COLUMN_1_SEL_ID+" =? ", new String[]{id});
        return  rowsDeleted;
    }

    // --- WORDS TABLE OPERATIONS ---

    /**
     * Gets 5 words starting from a specific ID
     */
    public Cursor getFourWordsFromId(int startId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME_WORDS +
                " WHERE " + COLUMN_1_SEL_ID + " >= ?" +
                " LIMIT 4", new String[]{String.valueOf(startId)});
    }

    /**
     * Get a single word's data by ID
     */
    public Cursor getWordById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME_WORDS +
                " WHERE " + COLUMN_1_SEL_ID + " = ?", new String[]{String.valueOf(id)});
    }

// --- SETTINGS TABLE OPERATIONS ---

    public int getLastStartingWordId() {
        return getIntFromSettings(COLUMN_1_LAST_STARTING_WORD_ID);
    }

    public void updateLastStartingWordId(int id) {
        updateSettingsValue(COLUMN_1_LAST_STARTING_WORD_ID, id);
    }

    // Sound (Store as 1 for true, 0 for false)
    public boolean isSoundOn() {
        return getIntFromSettings(COLUMN_2_SOUND_ON) == 1;
    }

    public void updateSoundOn(boolean isOn) {
        updateSettingsValue(COLUMN_2_SOUND_ON, isOn ? 1 : 0);
    }
    public boolean isMusicOn() {
        return getIntFromSettings(COLUMN_3_MUSIC_ON) == 1;
    }

    public void updateMusicOn(boolean isOn) {
        updateSettingsValue(COLUMN_3_MUSIC_ON, isOn ? 1 : 0);
    }

    public int getDailyTarget() {
        return getIntFromSettings(COLUMN_4_DAILY_TARGET);
    }

    public int getDailyTargetCompletedCount() {
        return getIntFromSettings(COLUMN_5_DAILY_TARGET_COMPLETED_WORDS_COUNT);
    }

    public void updateDailyTargetCompletedCount(int count) {
        updateSettingsValue(COLUMN_5_DAILY_TARGET_COMPLETED_WORDS_COUNT, count);
    }

// --- USER TABLE OPERATIONS ---

    public void updateWordsFinished(int count) {
        updateUserValue(COLUMN_1_WORDS_FINISHED, count);
    }

    public void updateTotalWords(int total) {
        updateUserValue(COLUMN_2_TOTAL_WORDS, total);
    }

    public void updateHighestRecord(int record) {
        updateUserValue(COLUMN_3_HIGHEST_RECORD, record);
    }
    public int getHighestRecord() {
        return getIntFromUser(COLUMN_3_HIGHEST_RECORD);
    }
    public Cursor getNextWordAfter(int currentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Query: Select all where ID > currentId, order by ID ascending, limit 1
        return db.rawQuery("SELECT * FROM " + TABLE_NAME_WORDS +
                        " WHERE " + COLUMN_1_SEL_ID + " > ?" +
                        " ORDER BY " + COLUMN_1_SEL_ID + " ASC LIMIT 1",
                new String[]{String.valueOf(currentId)});
    }

// --- HELPER UTILITIES (To keep code clean) ---

    private void updateSettingsValue(String column, int value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(column, value);
        // Note: This assumes you only ever have ONE row in Settings
        db.update(TABLE_NAME_SETTINGS, cv, null, null);
    }

    private void updateUserValue(String column, int value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(column, value);
        db.update(TABLE_NAME_USER, cv, null, null);
    }

    private int getIntFromSettings(String column) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + column + " FROM " + TABLE_NAME_SETTINGS, null);
        int value = 0;
        if (cursor.moveToFirst()) {
            value = cursor.getInt(0);
        }
        cursor.close();
        return value;
    }

    private int getIntFromUser(String column) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + column + " FROM " + TABLE_NAME_USER, null);
        int value = 0;
        if (cursor.moveToFirst()) {
            value = cursor.getInt(0);
        }
        cursor.close();
        return value;
    }

}
