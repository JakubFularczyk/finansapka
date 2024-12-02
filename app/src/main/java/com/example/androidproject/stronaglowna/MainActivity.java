package com.example.androidproject.stronaglowna;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.androidproject.R;
import com.example.androidproject.baza.BazaDanych;
import com.example.androidproject.transakcje.dao.KategoriaDAO;
import com.example.androidproject.transakcje.encje.KategoriaEntity;

import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private BazaDanych db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = Room.databaseBuilder(getApplicationContext(),
                BazaDanych.class, "baza_danych")
                .allowMainThreadQueries()
                .build();

        addCategories("Przychód", "Wydatek");
    }

    public List<KategoriaEntity> getCategories() {
        KategoriaDAO kategoriaDAO = db.kategoriaDAO();
        return kategoriaDAO.getAll();
    }

    private void addCategories(String ... categories) {
        for (String category : categories) {
            addCategory(category);
        }
    }

    public void addCategory(String category) {
        KategoriaDAO kategoriaDAO = db.kategoriaDAO();
        List<KategoriaEntity> kategorie = kategoriaDAO.getAll();
        long count = kategorie.stream() //
                .map(k -> k.getNazwa()) //
                .filter(n -> Objects.equals(n, category)) //
                .count();

        if (count == 0) {
            kategoriaDAO.insert(prepareCategory(category));
        }
    }

    KategoriaEntity prepareCategory(String nazwa) {
        KategoriaEntity kategoria = new KategoriaEntity();
        kategoria.setNazwa(nazwa);
        return kategoria;
    }



    public BazaDanych getDb() {
        return db;
    }
}