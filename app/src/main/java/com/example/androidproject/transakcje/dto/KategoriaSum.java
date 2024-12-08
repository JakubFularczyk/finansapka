package com.example.androidproject.transakcje.dto;

public class KategoriaSum {
    public String kategoria; // Nazwa kategorii
    public float suma;       // Suma wydatków

    // Konstruktor (opcjonalnie)
    public KategoriaSum(String kategoria, float suma) {
        this.kategoria = kategoria;
        this.suma = suma;
    }
}
