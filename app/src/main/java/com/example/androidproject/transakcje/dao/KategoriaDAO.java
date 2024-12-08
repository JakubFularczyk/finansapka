package com.example.androidproject.transakcje.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.androidproject.transakcje.encje.KategoriaEntity;


import java.util.List;

@Dao
public interface KategoriaDAO {

    @Insert
    void insert(KategoriaEntity kategoria);

    @Update
    void update(KategoriaEntity kategoria);

    @Query("SELECT * FROM KategoriaEntity")
    List<KategoriaEntity> getAll();

    @Query("SELECT * FROM KategoriaEntity WHERE nazwa = :nazwa")
    KategoriaEntity findByName(String nazwa);

    @Delete
    void delete(KategoriaEntity kategoria);

    @Query("SELECT kategoria FROM TransakcjaEntity WHERE kategoria IS NOT NULL GROUP BY kategoria ORDER BY COUNT(*) DESC LIMIT 1")
    String getMostFrequentCategory();

    @Query("DELETE FROM KategoriaEntity")
    void clearTable();
}

