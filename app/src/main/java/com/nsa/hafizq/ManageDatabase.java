package com.nsa.hafizq;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;

public class ManageDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "MAIN_QURANIC_WORD_GAME_DATABASE_OFFLINE";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME_WORDS = "ARABIC_BANGLA_WORDS";
    public static final String COLUMN_1_SEL_ID = "SERIAL_ID";
    public static final String COLUMN_2_ARABIC = "ARABIC";
    public static final String COLUMN_3_BANGLA = "BANGLA";
    public static final String COLUMN_4_RECALLED = "RECALLED";
    public static final String COLUMN_5_GUESSED = "GUESSED";
    public static final String COLUMN_6_PRONOUNCED = "PRONOUNCED";
    public static final String COLUMN_7_LISTENED = "LISTENED";
    public static final String COLUMN_8_ARABIC_PARTS = "PARTS";
    public static final String COLUMN_9_AYAH_AUDIO = "AYAH_AUDIO_INFO";
    public static final String COLUMN_10_RELATED_AYAH = "AYAH";



    public enum Answer {
        RECALL, GUESS, PRONOUNCE, LISTEN
    }
    public static final String TABLE_NAME_SETTINGS_USER = "SETTINGS_AND_USER";
    public static final String COLUMN_1_GAME_NAME_SETTINGS = "GAME_NAME";
    public static final String COLUMN_2_SOUND_ON = "SOUND_ON";
    public static final String COLUMN_3_MUSIC_ON = "MUSIC_ON";
    public static final String COLUMN_4_SOUND_VOLUMN = "SOUND_VOLUMN";
    public static final String COLUMN_5_MUSIC_VOLUMN = "MUSIC_VOLUMN";
    public static final String COLUMN_6_WORDS_FINISHED = "WORDS_FINISHED";
//    public static final String COLUMN_7_TOTAL_WORDS = "TOTAL_WORDS";
    public static final String COLUMN_7_HIGHEST_RECORD = "HIGHEST_RECORD";
    public static final String COLUMN_8_DAILY_TARGET_WORDS = "DAILY_TARGET_WORDS";



    // ─── TABLE: DAILY TARGET ───────────────────────────────────────────
    public static final String TABLE_NAME_DAILY_TARGET = "DAILY_TARGET";
    public static final String COLUMN_DT_1_ID            = "SERIAL_ID";
    public static final String COLUMN_DT_2_GAME_NAME     = "GAME_NAME";
    public static final String COLUMN_DT_3_DATE_DAY      = "DATE_DAY";           // e.g. "2025-04-07" (UTC)
    public static final String COLUMN_DT_4_START_TIME_MS = "START_TIME_MS";      // UTC epoch milliseconds
    public static final String COLUMN_DT_5_TOTAL_TIME_MS = "TOTAL_TIME_PLAYED_MS";
    public static final String COLUMN_DT_6_WORDS_LEARNED = "TOTAL_WORDS_LEARNED";
    public static final String COLUMN_DT_7_SCORE_GAINED  = "TOTAL_SCORE_GAINED";


    private static final String CREATE_TABLE_WORDS = "CREATE TABLE " + TABLE_NAME_WORDS + " (" +
            COLUMN_1_SEL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_2_ARABIC + " TEXT, " +
            COLUMN_3_BANGLA + " TEXT, " +
            COLUMN_4_RECALLED + " BOOLEAN, " +
            COLUMN_5_GUESSED + " BOOLEAN, " +
            COLUMN_6_PRONOUNCED + " BOOLEAN, " +
            COLUMN_7_LISTENED + " BOOLEAN, " +
            COLUMN_8_ARABIC_PARTS + " TEXT, " +
            COLUMN_9_AYAH_AUDIO + " TEXT, " +
            COLUMN_10_RELATED_AYAH + " TEXT);";
    // ─── TABLE: SETTINGS & USER ────────────────────────────────────────
    private static final String CREATE_TABLE_SETTINGS_USERS =
            "CREATE TABLE " + TABLE_NAME_SETTINGS_USER + " (" +
                    COLUMN_1_GAME_NAME_SETTINGS + " TEXT PRIMARY KEY, "       + // ← PRIMARY KEY here
                    COLUMN_2_SOUND_ON           + " BOOLEAN DEFAULT 1, "      +
                    COLUMN_3_MUSIC_ON           + " BOOLEAN DEFAULT 1, "      +
                    COLUMN_4_SOUND_VOLUMN       + " INTEGER DEFAULT 100 CHECK(" + COLUMN_4_SOUND_VOLUMN + " BETWEEN 0 AND 100), " + // ← max 100
                    COLUMN_5_MUSIC_VOLUMN       + " INTEGER DEFAULT 100 CHECK(" + COLUMN_5_MUSIC_VOLUMN + " BETWEEN 0 AND 100), " + // ← max 100
                    COLUMN_6_WORDS_FINISHED     + " INTEGER DEFAULT 0, "      +
//                    COLUMN_7_TOTAL_WORDS        + " INTEGER DEFAULT 0, "      +
                    COLUMN_7_HIGHEST_RECORD     + " INTEGER DEFAULT 0, "      +
                    COLUMN_8_DAILY_TARGET_WORDS + " INTEGER DEFAULT 10);";
            // SOUND , MUSIC, GAME NAME,


    // ─── CREATE TABLE ──────────────────────────────────────────────────
    private static final String CREATE_TABLE_DAILY_TARGET =
            "CREATE TABLE " + TABLE_NAME_DAILY_TARGET + " (" +
                    COLUMN_DT_1_ID            + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DT_2_GAME_NAME     + " TEXT, "    +
                    COLUMN_DT_3_DATE_DAY      + " TEXT, "    +   // store as "YYYY-MM-DD" UTC
                    COLUMN_DT_4_START_TIME_MS + " INTEGER, " +   // UTC epoch ms
                    COLUMN_DT_5_TOTAL_TIME_MS + " INTEGER, " +   // accumulated ms
                    COLUMN_DT_6_WORDS_LEARNED + " INTEGER, " +
                    COLUMN_DT_7_SCORE_GAINED  + " INTEGER);";




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
        db.execSQL(CREATE_TABLE_SETTINGS_USERS);
        db.execSQL(CREATE_TABLE_DAILY_TARGET);

        // Initialize Settings and User
        // ─── Default rows for all 4 games ─────────────────────────────────
        // GAME_NAME, SOUND_ON, MUSIC_ON, SOUND_VOL, MUSIC_VOL, WORDS_FINISHED, TOTAL_WORDS, HIGHEST_RECORD, DAILY_TARGET
//        db.execSQL("INSERT INTO " + TABLE_NAME_SETTINGS_USER + " VALUES ('RECALL',    1, 1, 100, 100, 0, 0, 0, 10)");
//        db.execSQL("INSERT INTO " + TABLE_NAME_SETTINGS_USER + " VALUES ('GUESS',     1, 1, 100, 100, 0, 0, 0, 10)");
//        db.execSQL("INSERT INTO " + TABLE_NAME_SETTINGS_USER + " VALUES ('PRONOUNCE', 1, 1, 100, 100, 0, 0, 0, 10)");
//        db.execSQL("INSERT INTO " + TABLE_NAME_SETTINGS_USER + " VALUES ('LISTEN',    1, 1, 100, 100, 0, 0, 0, 10)");
        // Insert the JSON data
        insertInitialWordsFromAssets(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        if (oldVersion < 2) {
//            // only migrate what changed in version 2
//            db.execSQL("ALTER TABLE " + TABLE_NAME_WORDS + " ADD COLUMN new_col TEXT");
//        }
//        onCreate(db);
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
                    // get parts array and join as comma-separated string
                    JSONArray partsArray = obj.getJSONArray("parts");
                    StringBuilder parts = new StringBuilder();
                    for (int j = 0; j < partsArray.length(); j++) {
                        if (j > 0) parts.append(",");
                        parts.append(partsArray.getString(j));
                    }
                    values.put(COLUMN_4_RECALLED,      false);  // default false
                    values.put(COLUMN_5_GUESSED,       false);
                    values.put(COLUMN_6_PRONOUNCED,    false);
                    values.put(COLUMN_7_LISTENED,      false);
                    values.put(COLUMN_8_ARABIC_PARTS, parts.toString()); // stored as "اصْ,طَرَ,خَ"
                    values.put(COLUMN_9_AYAH_AUDIO,  obj.optString("ayah_audio_info", ""));
                    values.put(COLUMN_10_RELATED_AYAH,  obj.optString("related_ayah", "")); // empty if not present in JSON


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

    public int getTotalWordCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        // Using count(*) is the fastest way to get the total number of rows
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME_WORDS, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public boolean insertRowDataWords(String arabic, String bangla, int wrong, int correct){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_2_ARABIC, arabic);
        contentValues.put(COLUMN_3_BANGLA, bangla);
//        contentValues.put(COLUMN_4_WRONG, wrong);
//        contentValues.put(COLUMN_5_CORRECT, correct);

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
//        contentValues.put(COLUMN_4_WRONG, wrong);
//        contentValues.put(COLUMN_5_CORRECT, correct);

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
     * Gets 4 words starting from a specific ID
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
//        return getIntFromSettings(COLUMN_1_LAST_STARTING_WORD_ID);

    return 0;
    }

    public void updateLastStartingWordId(int id) {
//        updateSettingsValue(COLUMN_1_LAST_STARTING_WORD_ID, id);
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
      //  return getIntFromSettings(COLUMN_4_DAILY_TARGET);
        return 0;
    }

    public int getDailyTargetCompletedCount() {
      //  return getIntFromSettings(COLUMN_5_DAILY_TARGET_COMPLETED_WORDS_COUNT);
        return 0;
    }

    public void updateDailyTargetCompletedCount(int count) {
      //  updateSettingsValue(COLUMN_5_DAILY_TARGET_COMPLETED_WORDS_COUNT, count);
    }

// --- USER TABLE OPERATIONS ---

    public void updateWordsFinished(int count) {
       // updateUserValue(COLUMN_1_WORDS_FINISHED, count);
    }

    public void updateTotalWords(int total) {
       // updateUserValue(COLUMN_2_TOTAL_WORDS, total);
    }

    public void updateHighestRecord(int record) {
       // updateUserValue(COLUMN_3_HIGHEST_RECORD, record);
    }
    public int getHighestRecord() {
       // return getIntFromUser(COLUMN_3_HIGHEST_RECORD);
        return 0;
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
      //  db.update(TABLE_NAME_SETTINGS, cv, null, null);
    }

    private void updateUserValue(String column, int value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(column, value);
       // db.update(TABLE_NAME_USER, cv, null, null);
    }

    private int getIntFromSettings(String column) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + column + " FROM " , null);
        int value = 0;
        if (cursor.moveToFirst()) {
            value = cursor.getInt(0);
        }
        cursor.close();
        return value;
    }

    private int getIntFromUser(String column) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + column + " FROM " , null);
        int value = 0;
        if (cursor.moveToFirst()) {
            value = cursor.getInt(0);
        }
        cursor.close();
        return value;
    }

}
