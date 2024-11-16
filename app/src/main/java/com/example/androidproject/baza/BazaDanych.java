package com.example.androidproject.baza;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.androidproject.transakcje.dao.TransakcjaDAO;
import com.example.androidproject.transakcje.encje.TransakcjaEntity;

@Database(entities = {TransakcjaEntity.class}, version = 1)
public abstract class BazaDanych extends RoomDatabase {
    public abstract TransakcjaDAO transakcjaDAO();
}