package com.nsa.hafizq.database.dao;

import android.icu.text.SimpleDateFormat;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.nsa.hafizq.database.entity.DailyTarget;

import java.util.Date;
import java.util.List;
import java.util.Locale;

@Dao
public interface DailyTargetDao {

    @Insert
    void insert(DailyTarget dailyTarget);

    @Query("SELECT * FROM DAILY_TARGET WHERE GAME_NAME = :gameName AND DATE_DAY = :dateDay")
    DailyTarget getTodayTarget(String gameName, String dateDay);

    @Query("SELECT TOTAL_WORDS_LEARNED FROM DAILY_TARGET " +
            "WHERE GAME_NAME = :gameName AND DATE_DAY = :dateDay")
    Integer getWordsByDate(String gameName, String dateDay);

    @Update
    void update(DailyTarget dailyTarget);

    @Query("SELECT * FROM DAILY_TARGET WHERE GAME_NAME = :gameName ORDER BY SERIAL_ID DESC")
    List<DailyTarget> getAllByGame(String gameName);

    @Transaction
    default void upsertDailyStats(String gameName, String dateDay, long time, int words, int score) {
        DailyTarget existing = getTodayTarget(gameName, dateDay);
        if (existing == null) {
            DailyTarget newItem = new DailyTarget();
            newItem.gameName = gameName;
            newItem.dateDay = dateDay;
            newItem.startTimeMs = System.currentTimeMillis();
            newItem.totalTimePlayedMs = time;
            newItem.totalWordsLearned = words;
            newItem.totalScoreGained = score;
            insert(newItem);
        } else {
            existing.totalTimePlayedMs += time;
            existing.totalWordsLearned += words;
            existing.totalScoreGained += score;
            update(existing);
        }
    }

    @Query("SELECT * FROM DAILY_TARGET " +
            "WHERE GAME_NAME = :gameName " +
            "AND DATE_DAY >= date('now', '-' || :daysBack || ' days', 'localtime') " +
            "ORDER BY DATE_DAY ASC")
    List<DailyTarget> getProgressByDays(String gameName, int daysBack);
}