package com.example.androidproject.transakcje.encje;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.androidproject.baza.DateConverter;

import java.util.Date;

@Entity(
        foreignKeys = @ForeignKey(
                entity = TransakcjaEntity.class,
                parentColumns = "uid",
                childColumns = "idTransakcji",
                onDelete = ForeignKey.CASCADE
        )
)
@TypeConverters({DateConverter.class})
public class TransakcjaCyklicznaEntity {

    @PrimaryKey(autoGenerate = true)
    public int uid;
    public int idTransakcji;
    public Date dataOd;
    public String interwal; // DZIEN, TYDZIEN, MIESIAC, ROK

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getIdTransakcji() {
        return idTransakcji;
    }

    public void setIdTransakcji(int idTransakcji) {
        this.idTransakcji = idTransakcji;
    }

    public Date getDataOd() {
        return dataOd;
    }

    public void setDataOd(Date dataOd) {
        this.dataOd = dataOd;
    }

    public String getInterwal() {
        return interwal;
    }

    public void setInterwal(String interwal) {
        this.interwal = interwal;
    }


}
