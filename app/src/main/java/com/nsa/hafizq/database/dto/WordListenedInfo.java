package com.nsa.hafizq.database.dto;

import androidx.room.ColumnInfo;

// For the Listened function
public class WordListenedInfo {
    @ColumnInfo(name = "SERIAL_ID") public int serialId;
    @ColumnInfo(name = "ARABIC") public String arabic;
    @ColumnInfo(name = "BANGLA") public String bangla;
    @ColumnInfo(name = "AYAH") public String relatedAyah;
    @ColumnInfo(name = "AYAH_AUDIO_INFO") public String ayahAudioInfo;
    @ColumnInfo(name = "LISTENED") public boolean listened; // Extra
}