package com.example.androidproject.transakcje.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.androidproject.transakcje.encje.TransakcjaEntity;

import java.util.List;

@Dao
public interface TransakcjaDAO {


    @Insert
    long insert(TransakcjaEntity transakcja);


    @Query("SELECT * FROM TransakcjaEntity")
    List<TransakcjaEntity> getAll();

    @Query("SELECT * FROM TransakcjaEntity ORDER BY data DESC LIMIT 3")
    List<TransakcjaEntity> getLatest3();

    @Update
    void update(TransakcjaEntity transakcja);

    @Query("SELECT * FROM TransakcjaEntity WHERE uid = :uid")
    TransakcjaEntity getTransactionByUid(int uid);


    @Query("SELECT * FROM TransakcjaEntity ORDER BY data DESC")
    List<TransakcjaEntity> getAllSortedByDate();

    @Delete
    void delete(TransakcjaEntity transakcja);

    @Query("SELECT * FROM TransakcjaEntity ORDER BY data ASC")
    List<TransakcjaEntity> getOldest();

    @Query("SELECT * FROM TransakcjaEntity ORDER BY data DESC")
    List<TransakcjaEntity> getNewest();

    @Query("SELECT * FROM TransakcjaEntity WHERE (:isIncome = 1 AND kwota > 0) OR (:isIncome = 0 AND kwota < 0)")
    List<TransakcjaEntity> getByType(boolean isIncome);


    @Query("SELECT * FROM TransakcjaEntity ORDER BY CAST(kwota AS REAL) DESC")
    List<TransakcjaEntity> getHighest();

    @Query("SELECT * FROM TransakcjaEntity ORDER BY CAST(kwota AS REAL) ASC")
    List<TransakcjaEntity> getLowest();

    @Query("DELETE FROM TransakcjaEntity WHERE kategoria = :categoryName")
    void deleteByCategory(String categoryName);

    @Query("DELETE FROM TransakcjaEntity WHERE uid = :parentId OR parentTransactionId = :parentId")
    void deleteRecurringTransactionWithCopies(int parentId);

    @Query("SELECT * FROM TransakcjaEntity WHERE parentTransactionId = :parentId AND isCyclicChild = 1")
    List<TransakcjaEntity> getCopiesByParentId(int parentId);

    @Query("SELECT * FROM TransakcjaEntity ORDER BY CAST(kwota AS REAL) DESC LIMIT 1")
    TransakcjaEntity getHighestTransaction();

    @Query("SELECT strftime('%Y-%m', datetime(data / 1000, 'unixepoch')) " +
            "FROM TransakcjaEntity " +
            "GROUP BY strftime('%Y-%m', datetime(data / 1000, 'unixepoch')) " +
            "ORDER BY COUNT(*) DESC " +
            "LIMIT 1")
    String getMonthWithMostTransactions();
    @Query("SELECT opis FROM TransakcjaEntity WHERE isCyclicChild = 1 GROUP BY opis ORDER BY COUNT(opis) DESC LIMIT 1")
    String getMostFrequentRecurringTransaction();



}
