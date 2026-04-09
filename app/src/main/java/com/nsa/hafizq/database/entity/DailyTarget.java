package com.nsa.hafizq.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DAILY_TARGET")
public class DailyTarget {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "SERIAL_ID")
    public Integer serialId;

    @ColumnInfo(name = "GAME_NAME")
    public String gameName;

    @ColumnInfo(name = "DATE_DAY")
    public String dateDay;

    @ColumnInfo(name = "START_TIME_MS")
    public Long startTimeMs;

    @ColumnInfo(name = "TOTAL_TIME_PLAYED_MS")
    public Long totalTimePlayedMs;

    @ColumnInfo(name = "TOTAL_WORDS_LEARNED")
    public Integer totalWordsLearned;

    @ColumnInfo(name = "TOTAL_SCORE_GAINED")
    public Integer totalScoreGained;
}