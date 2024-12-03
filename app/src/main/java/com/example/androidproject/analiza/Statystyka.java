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

    public void setNaglowek(String naglowek) {
        this.naglowek = naglowek;
    }

    public String getWartosc() {
        return wartosc;
    }

    public void setWartosc(String wartosc) {
        this.wartosc = wartosc;
    }
}