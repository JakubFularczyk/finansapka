package com.example.androidproject.baza;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.androidproject.baza.dao.KategoriaDAO;
import com.example.androidproject.baza.dao.TransakcjaCyklicznaDAO;
import com.example.androidproject.baza.dao.TransakcjaDAO;
import com.example.androidproject.baza.dao.UserDAO;
import com.example.androidproject.baza.encje.KategoriaEntity;
import com.example.androidproject.baza.encje.TransakcjaCyklicznaEntity;
import com.example.androidproject.baza.encje.TransakcjaEntity;
import com.example.androidproject.baza.encje.UserEntity;

@Database(entities = {TransakcjaEntity.class, KategoriaEntity.class, TransakcjaCyklicznaEntity.class, UserEntity.class}, version = 1)
public abstract class BazaDanych extends RoomDatabase {
    public abstract TransakcjaDAO transakcjaDAO();
    public abstract KategoriaDAO kategoriaDAO();
    public abstract TransakcjaCyklicznaDAO transakcjaCyklicznaDAO();
    public abstract UserDAO userDAO();

}
