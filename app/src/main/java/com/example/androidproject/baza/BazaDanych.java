package com.example.androidproject.baza;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.androidproject.transakcje.dao.KategoriaDAO;
import com.example.androidproject.transakcje.dao.TransakcjaCyklicznaDAO;
import com.example.androidproject.transakcje.dao.TransakcjaDAO;
import com.example.androidproject.transakcje.encje.KategoriaEntity;
import com.example.androidproject.transakcje.encje.TransakcjaCyklicznaEntity;
import com.example.androidproject.transakcje.encje.TransakcjaEntity;

@Database(entities = {TransakcjaEntity.class, KategoriaEntity.class, TransakcjaCyklicznaEntity.class}, version = 1)
public abstract class BazaDanych extends RoomDatabase {
    public abstract TransakcjaDAO transakcjaDAO();
    public abstract KategoriaDAO kategoriaDAO();
    public abstract TransakcjaCyklicznaDAO transakcjaCyklicznaDAO();


}
