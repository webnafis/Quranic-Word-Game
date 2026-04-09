package com.nsa.hafizq.database.dto; // Or any package you prefer

import androidx.room.ColumnInfo;

public class WordPronouncedInfo {
    @ColumnInfo(name = "SERIAL_ID")
    public int serialId;

    @ColumnInfo(name = "ARABIC")
    public String arabic;

    @ColumnInfo(name = "BANGLA")
    public String bangla;

    @ColumnInfo(name = "PRONOUNCED")
    public boolean pronounced;

    @ColumnInfo(name = "AYAH")
    public String relatedAyah;

    @ColumnInfo(name = "AYAH_AUDIO_INFO")
    public String ayahAudioInfo;
}