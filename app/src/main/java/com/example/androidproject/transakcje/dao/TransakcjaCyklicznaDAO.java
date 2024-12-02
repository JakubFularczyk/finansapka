package com.example.androidproject.transakcje.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.androidproject.transakcje.encje.TransakcjaCyklicznaEntity;


import java.util.List;

@Dao
public interface TransakcjaCyklicznaDAO {

    @Insert
    void insert(TransakcjaCyklicznaEntity transakcjaCykliczna);

    @Update
    void update(TransakcjaCyklicznaEntity transakcjaCykliczna);

    @Delete
    void delete(TransakcjaCyklicznaEntity transakcjaCykliczna);

    @Query("SELECT * FROM TransakcjaCyklicznaEntity WHERE idTransakcji = :idTransakcji")
    List<TransakcjaCyklicznaEntity> findByIdTransakcji(int idTransakcji);

    @Query("SELECT * FROM TransakcjaCyklicznaEntity")
    List<TransakcjaCyklicznaEntity> getAll();

    @Query("DELETE FROM TransakcjaCyklicznaEntity WHERE idTransakcji = :idTransakcji")
    void deleteRecurringTransactionById(int idTransakcji);


}

