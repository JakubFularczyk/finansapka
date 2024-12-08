package com.example.androidproject.analiza;

public class Statystyka {

    private String naglowek;
    private String wartosc;

    public Statystyka(String naglowek, String wartosc) {
        this.naglowek = naglowek;
        this.wartosc = wartosc;
    }

    public String getNaglowek() {
        return naglowek;
    }

    public String getWartosc() {
        return wartosc;
    }


}