package com.nsa.hafizq.database.entity;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.nsa.hafizq.GameName;

@Entity(tableName = "SETTINGS_AND_USER")
public class Settings {

    @PrimaryKey
    @NonNull // Keep this, but ensure the DB column is set to NOT NULL in your SQL tool
    @ColumnInfo(name = "GAME_NAME")
    public String gameName = "RECALL";

    // Use lowercase 'boolean' or 'int' to help Room match integer affinities
    @ColumnInfo(name = "SOUND_ON", defaultValue = "1")
    public Boolean soundOn;

    @ColumnInfo(name = "MUSIC_ON", defaultValue = "1")
    public Boolean musicOn;

    @ColumnInfo(name = "SOUND_VOLUMN", defaultValue = "100")
    public Integer soundVolumn;

    @ColumnInfo(name = "MUSIC_VOLUMN", defaultValue = "100")
    public Integer musicVolumn;

    @ColumnInfo(name = "WORDS_FINISHED", defaultValue = "0")
    public Integer wordsFinished;

    @ColumnInfo(name = "HIGHEST_RECORD", defaultValue = "0")
    public Integer highestRecord;

    @ColumnInfo(name = "DAILY_TARGET_WORDS", defaultValue = "10")
    public Integer dailyTargetWords;
}