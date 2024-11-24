package com.example.androidproject.transakcje.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.androidproject.transakcje.encje.TransakcjaEntity;

import java.util.List;

@Dao
public interface TransakcjaDAO {

    @Insert
    void insert(TransakcjaEntity transakcja);

    @Query("SELECT * FROM TransakcjaEntity")
    List<TransakcjaEntity> getAll();

    @Query("SELECT * FROM TransakcjaEntity ORDER BY data DESC LIMIT 3")
    List<TransakcjaEntity> getLatest3();

    @Update
    void update(TransakcjaEntity transakcja);


    @Query("SELECT * FROM TransakcjaEntity ORDER BY data DESC")
    List<TransakcjaEntity> getAllSortedByDate();
}
