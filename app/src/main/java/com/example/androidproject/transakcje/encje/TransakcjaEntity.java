package com.example.androidproject.transakcje.encje;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.androidproject.baza.DateConverter;

import java.util.Date;


@Entity
@TypeConverters({DateConverter.class})
public class TransakcjaEntity {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    public String kwota;

    public String kategoria;

    public Date data;

    public String opis;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getKwota() {
        return kwota;
    }

    public void setKwota(String kwota) {
        this.kwota = kwota;
    }

    public String getKategoria() {
        return kategoria;
    }

    public void setKategoria(String kategoria) {
        this.kategoria = kategoria;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }
}
