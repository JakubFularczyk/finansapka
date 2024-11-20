package com.example.androidproject.stronaglowna;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.androidproject.R;
import com.example.androidproject.baza.BazaDanych;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BazaDanych db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = Room.databaseBuilder(getApplicationContext(),
                BazaDanych.class, "baza_danych").allowMainThreadQueries().build();
    }

    private final List<String> categories = new ArrayList<>(Arrays.asList("Dom", "Samochód", "Jedzenie"));

    public List<String> getCategories() {
        return categories;
    }

    public void addCategory(String category) {
        if (!categories.contains(category)) {
            categories.add(category);
        }
    }

    public BazaDanych getDb() {
        return db;
    }
}