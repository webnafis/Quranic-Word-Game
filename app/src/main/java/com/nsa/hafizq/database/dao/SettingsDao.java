package com.nsa.hafizq.database.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Update;

import com.nsa.hafizq.database.entity.Settings;

@Dao
public interface SettingsDao {

    @Query("SELECT * FROM SETTINGS_AND_USER WHERE GAME_NAME = :gameName")
    Settings getSettings(String gameName);

    @Update
    void updateSettings(Settings settings);

    // ─── WORDS FINISHED ────────────────────────────────────────────
    @Query("SELECT WORDS_FINISHED FROM SETTINGS_AND_USER WHERE GAME_NAME = :gameName")
    int getWordsFinished(String gameName);

    @Query("UPDATE SETTINGS_AND_USER SET WORDS_FINISHED = :value WHERE GAME_NAME = :gameName")
    void updateWordsFinished(String gameName, int value);

    // ─── HIGHEST RECORD ────────────────────────────────────────────
    @Query("SELECT HIGHEST_RECORD FROM SETTINGS_AND_USER WHERE GAME_NAME = :gameName")
    int getHighestRecord(String gameName);

    @Query("UPDATE SETTINGS_AND_USER SET HIGHEST_RECORD = :value WHERE GAME_NAME = :gameName")
    void updateHighestRecord(String gameName, int value);

    // ─── DAILY TARGET WORDS ────────────────────────────────────────
    @Query("SELECT DAILY_TARGET_WORDS FROM SETTINGS_AND_USER WHERE GAME_NAME = :gameName")
    int getDailyTargetWords(String gameName);

    @Query("UPDATE SETTINGS_AND_USER SET DAILY_TARGET_WORDS = :value WHERE GAME_NAME = :gameName")
    void updateDailyTargetWords(String gameName, int value);

    // ─── UPDATE MULTIPLE ───────────────────────────────────────────
    @Query("UPDATE SETTINGS_AND_USER SET SOUND_ON = :soundOn, MUSIC_ON = :musicOn WHERE GAME_NAME = :gameName")
    void updateSoundAndMusic(String gameName, boolean soundOn, boolean musicOn);

    @Query("UPDATE SETTINGS_AND_USER SET SOUND_VOLUMN = :soundVol, MUSIC_VOLUMN = :musicVol WHERE GAME_NAME = :gameName")
    void updateBothVolumns(String gameName, int soundVol, int musicVol);

    @Query("UPDATE SETTINGS_AND_USER SET SOUND_ON = :soundOn, MUSIC_ON = :musicOn, SOUND_VOLUMN = :soundVol, MUSIC_VOLUMN = :musicVol WHERE GAME_NAME = :gameName")
    void updateAllAudioSettings(String gameName, boolean soundOn, boolean musicOn, int soundVol, int musicVol);

}