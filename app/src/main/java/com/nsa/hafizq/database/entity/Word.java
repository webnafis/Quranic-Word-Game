package com.nsa.hafizq.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ARABIC_BANGLA_WORDS")
public class Word {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "SERIAL_ID")
    public Integer serialId;

    @ColumnInfo(name = "ARABIC")
    public String arabic;

    @ColumnInfo(name = "BANGLA")
    public String bangla;

    @ColumnInfo(name = "RECALLED")
    public Boolean recalled;

    @ColumnInfo(name = "GUESSED")
    public Boolean guessed;

    @ColumnInfo(name = "PRONOUNCED")
    public Boolean pronounced;

    @ColumnInfo(name = "LISTENED")
    public Boolean listened;

    @ColumnInfo(name = "PARTS")
    public String parts;

    @ColumnInfo(name = "AYAH_AUDIO_INFO")
    public String ayahAudioInfo;

    @ColumnInfo(name = "AYAH")
    public String relatedAyah;
}