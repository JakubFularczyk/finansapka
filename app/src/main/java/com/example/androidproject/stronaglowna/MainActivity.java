package com.example.androidproject.stronaglowna;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.androidproject.R;
import com.example.androidproject.baza.BazaDanych;

public class MainActivity extends AppCompatActivity {

    private BazaDanych db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = Room.databaseBuilder(getApplicationContext(),
                BazaDanych.class, "baza_danych").allowMainThreadQueries().build();
    }

    public BazaDanych getDb() {
        return db;
    }
}