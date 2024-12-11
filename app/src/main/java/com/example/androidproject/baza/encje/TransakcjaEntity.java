package com.example.androidproject.baza.encje;

import androidx.room.ColumnInfo;
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

    @ColumnInfo(name = "isCyclicChild") // Dodajemy nazwę kolumny w bazie
    public boolean isCyclicChild = false;

    public Integer parentTransactionId;


    public Integer getParentTransactionId() {
        return parentTransactionId;
    }

    public void setParentTransactionId(Integer parentTransactionId) {
        this.parentTransactionId = parentTransactionId;
    }

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

    public boolean isCyclicChild() {
        return isCyclicChild;
    }

    public void setCyclicChild(boolean cyclicChild) {
        isCyclicChild = cyclicChild;
    }

}
