package com.example.androidproject.baza.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.androidproject.utils.dto.KategoriaSum;
import com.example.androidproject.utils.dto.MonthSum;
import com.example.androidproject.baza.encje.TransakcjaEntity;

import java.util.List;

@Dao
public interface TransakcjaDAO {


    @Insert
    long insert(TransakcjaEntity transakcja);

    @Query("DELETE FROM transakcjaentity")
    void clearTable();

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

    @Query("SELECT kategoria AS kategoria, SUM(CAST(kwota AS REAL)) AS suma " +
            "FROM TransakcjaEntity " +
            "WHERE kwota < 0 AND strftime('%Y-%m', datetime(data / 1000, 'unixepoch')) = strftime('%Y-%m', 'now') " +
            "GROUP BY kategoria")
    List<KategoriaSum> getMonthlyExpensesByCategory();


    @Query("SELECT kategoria, SUM(CAST(kwota AS REAL)) AS suma " +
            "FROM TransakcjaEntity " +
            "WHERE kwota > 0 AND strftime('%Y-%m', datetime(data / 1000, 'unixepoch')) = strftime('%Y-%m', 'now') " +
            "GROUP BY kategoria")
    List<KategoriaSum> getMonthlyIncomesByCategory();

    @Query("SELECT strftime('%m', datetime(data / 1000, 'unixepoch')) AS month, " +
            "SUM(CAST(kwota AS REAL)) AS total " +
            "FROM TransakcjaEntity " +
            "WHERE kwota < 0 " + // Wydatki
            "GROUP BY month " +
            "ORDER BY month")
    List<MonthSum> getMonthlyExpenses();

    @Query("SELECT strftime('%m', datetime(data / 1000, 'unixepoch')) AS month, " +
            "SUM(CAST(kwota AS REAL)) AS total " +
            "FROM TransakcjaEntity " +
            "WHERE kwota > 0 " + // Przychody
            "GROUP BY month " +
            "ORDER BY month")
    List<MonthSum> getMonthlyIncomes();

    @Query("SELECT * FROM TransakcjaEntity WHERE uid = :id LIMIT 1")
    TransakcjaEntity getById(int id);

}
