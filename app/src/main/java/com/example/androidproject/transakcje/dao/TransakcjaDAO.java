package com.example.androidproject.transakcje.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.androidproject.transakcje.encje.TransakcjaEntity;

import java.util.List;

@Dao
public interface TransakcjaDAO {

    @Insert
    void insertAll(TransakcjaEntity transakcja);

    @Query("SELECT * FROM TransakcjaEntity")
    List<TransakcjaEntity> getAll();
}
