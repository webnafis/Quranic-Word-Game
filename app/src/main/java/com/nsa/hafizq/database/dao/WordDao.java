package com.nsa.hafizq.database.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Update;

import com.nsa.hafizq.database.dto.WordGuessedInfo;
import com.nsa.hafizq.database.dto.WordListenedInfo;
import com.nsa.hafizq.database.dto.WordPronouncedInfo;
import com.nsa.hafizq.database.dto.WordRecalledInfo;
import com.nsa.hafizq.database.entity.Word;

import java.util.List;

@Dao
public interface WordDao {
    @Query("SELECT * FROM ARABIC_BANGLA_WORDS LIMIT :pageSize OFFSET (:pageNumber - 1) * :pageSize")
    List<Word> getWordsByPage(int pageNumber, int pageSize);
    @Query("SELECT (COUNT(*) + :pageSize - 1) / :pageSize FROM ARABIC_BANGLA_WORDS")
    int getWordPageCountCustom(int pageSize);

    @Query("SELECT * FROM ARABIC_BANGLA_WORDS")
    List<Word> getAllWords();

    @Query("SELECT * FROM ARABIC_BANGLA_WORDS WHERE SERIAL_ID = :id")
    Word getWordById(int id);

    // ── GET false words from the BEGINNING (limited amount) ─────────
    @Query("SELECT SERIAL_ID, ARABIC, BANGLA, PRONOUNCED, AYAH, AYAH_AUDIO_INFO " +
            "FROM ARABIC_BANGLA_WORDS " +
            "WHERE PRONOUNCED = 0 " +
            "ORDER BY SERIAL_ID ASC LIMIT :amount")
    List<WordPronouncedInfo> getPronounced_False_From_Start(int amount);

    // 1. Listened Logic
    @Query("SELECT SERIAL_ID, ARABIC, BANGLA, AYAH, AYAH_AUDIO_INFO, LISTENED " +
            "FROM ARABIC_BANGLA_WORDS WHERE LISTENED = 0 " +
            "ORDER BY SERIAL_ID ASC LIMIT :amount")
    List<WordListenedInfo> getListened_False_From_Start(int amount);

    // 2. Guessed Logic (Includes PARTS)
    @Query("SELECT SERIAL_ID, ARABIC, BANGLA, AYAH, AYAH_AUDIO_INFO, GUESSED, PARTS " +
            "FROM ARABIC_BANGLA_WORDS WHERE GUESSED = 0 " +
            "ORDER BY SERIAL_ID ASC LIMIT :amount")
    List<WordGuessedInfo> getGuessed_False_From_Start(int amount);

    // 3. Recalled Logic
    @Query("SELECT SERIAL_ID, ARABIC, BANGLA, AYAH, AYAH_AUDIO_INFO, RECALLED " +
            "FROM ARABIC_BANGLA_WORDS WHERE RECALLED = 0 " +
            "ORDER BY SERIAL_ID ASC LIMIT :amount")
    List<WordRecalledInfo> getRecalled_False_From_Start(int amount);


    // ── GET false words AFTER a particular SERIAL_ID (limited amount)
    @Query("SELECT SERIAL_ID, ARABIC, BANGLA, PRONOUNCED, AYAH, AYAH_AUDIO_INFO " +
            "FROM ARABIC_BANGLA_WORDS " +
            "WHERE PRONOUNCED = 0 AND SERIAL_ID > :afterId " +
            "ORDER BY SERIAL_ID ASC LIMIT :amount")
    List<WordPronouncedInfo> getPronounced_False_After_Id(int afterId, int amount);

    // ─── LISTENED (AFTER ID) ──────────────────────────────────────────
    @Query("SELECT SERIAL_ID, ARABIC, BANGLA, AYAH, AYAH_AUDIO_INFO, LISTENED " +
            "FROM ARABIC_BANGLA_WORDS " +
            "WHERE LISTENED = 0 AND SERIAL_ID > :afterId " +
            "ORDER BY SERIAL_ID ASC LIMIT :amount")
    List<WordListenedInfo> getListened_False_After_Id(int afterId, int amount);

    // ─── GUESSED (AFTER ID + PARTS) ───────────────────────────────────
    @Query("SELECT SERIAL_ID, ARABIC, BANGLA, AYAH, AYAH_AUDIO_INFO, GUESSED, PARTS " +
            "FROM ARABIC_BANGLA_WORDS " +
            "WHERE GUESSED = 0 AND SERIAL_ID > :afterId " +
            "ORDER BY SERIAL_ID ASC LIMIT :amount")
    List<WordGuessedInfo> getGuessed_False_After_Id(int afterId, int amount);

    // ─── RECALLED (AFTER ID) ──────────────────────────────────────────
    @Query("SELECT SERIAL_ID, ARABIC, BANGLA, AYAH, AYAH_AUDIO_INFO, RECALLED " +
            "FROM ARABIC_BANGLA_WORDS " +
            "WHERE RECALLED = 0 AND SERIAL_ID > :afterId " +
            "ORDER BY SERIAL_ID ASC LIMIT :amount")
    List<WordRecalledInfo> getRecalled_False_After_Id(int afterId, int amount);
    @Query("SELECT COUNT(*) FROM ARABIC_BANGLA_WORDS")
    int getTotalWordCount();

    // ── PRONOUNCED ──────────────────────────────────────────────────
    @Query("SELECT COUNT(*) FROM ARABIC_BANGLA_WORDS WHERE PRONOUNCED = 0")
    int getPronouncedFalseCount();

    @Query("SELECT COUNT(*) FROM ARABIC_BANGLA_WORDS WHERE PRONOUNCED = 1")
    int getPronouncedTrueCount();

    // ── LISTENED ─────────────────────────────────────────────────────
    @Query("SELECT COUNT(*) FROM ARABIC_BANGLA_WORDS WHERE LISTENED = 0")
    int getListenedFalseCount();

    @Query("SELECT COUNT(*) FROM ARABIC_BANGLA_WORDS WHERE LISTENED = 1")
    int getListenedTrueCount();

    // ── GUESSED ──────────────────────────────────────────────────────
    @Query("SELECT COUNT(*) FROM ARABIC_BANGLA_WORDS WHERE GUESSED = 0")
    int getGuessedFalseCount();

    @Query("SELECT COUNT(*) FROM ARABIC_BANGLA_WORDS WHERE GUESSED = 1")
    int getGuessedTrueCount();

    // ── RECALLED ─────────────────────────────────────────────────────
    @Query("SELECT COUNT(*) FROM ARABIC_BANGLA_WORDS WHERE RECALLED = 0")
    int getRecalledFalseCount();

    @Query("SELECT COUNT(*) FROM ARABIC_BANGLA_WORDS WHERE RECALLED = 1")
    int getRecalledTrueCount();


    // ── SET TRUE for a single word (by SERIAL_ID) ───────────────────
    @Query("UPDATE ARABIC_BANGLA_WORDS SET PRONOUNCED = 1 WHERE SERIAL_ID = :id")
    void setPronounced(int id);

    @Query("UPDATE ARABIC_BANGLA_WORDS SET LISTENED = 1 WHERE SERIAL_ID = :id")
    void setListened(int id);

    @Query("UPDATE ARABIC_BANGLA_WORDS SET GUESSED = 1 WHERE SERIAL_ID = :id")
    void setGuessed(int id);

    @Query("UPDATE ARABIC_BANGLA_WORDS SET RECALLED = 1 WHERE SERIAL_ID = :id")
    void setRecalled(int id);


    // ── RESET ALL to false for entire DB ────────────────────────────
    @Query("UPDATE ARABIC_BANGLA_WORDS SET PRONOUNCED = 0")
    void resetAllPronounced();

    @Query("UPDATE ARABIC_BANGLA_WORDS SET LISTENED = 0")
    void resetAllListened();

    @Query("UPDATE ARABIC_BANGLA_WORDS SET GUESSED = 0")
    void resetAllGuessed();

    @Query("UPDATE ARABIC_BANGLA_WORDS SET RECALLED = 0")
    void resetAllRecalled();

    @Update
    void updateWord(Word word);
}