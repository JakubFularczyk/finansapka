package com.example.androidproject.transakcje.encje;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class KategoriaEntity {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    public String nazwa;

    public String limit;

    @ColumnInfo(defaultValue = "0")
    public double aktualnaKwota;

    public int getUid() {
        return uid;
    }

    public double getAktualnaKwota() {
        return aktualnaKwota;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getNazwa() {
        return nazwa;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }
}
