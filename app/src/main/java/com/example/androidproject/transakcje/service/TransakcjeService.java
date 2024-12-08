package com.example.androidproject.transakcje.service;

import android.util.Log;

import com.example.androidproject.baza.BazaDanych;
import com.example.androidproject.stronaglowna.MainActivity;
import com.example.androidproject.transakcje.dao.KategoriaDAO;
import com.example.androidproject.transakcje.dao.TransakcjaCyklicznaDAO;
import com.example.androidproject.transakcje.encje.KategoriaEntity;
import com.example.androidproject.transakcje.encje.TransakcjaCyklicznaEntity;
import com.example.androidproject.transakcje.encje.TransakcjaEntity;

import java.util.Date;

public class TransakcjeService {

    MainActivity activity;
    TransakcjaCyklicznaDAO recurringDAO;

    public TransakcjeService(MainActivity activity) {
        this.activity = activity;
        recurringDAO = activity.getDb().transakcjaCyklicznaDAO();
    }

    public void handleRecurringTransaction(TransakcjaEntity transaction, String interval, Date startDate) {
        TransakcjaCyklicznaEntity existingRecurringTransaction = recurringDAO.findByIdTransakcji(transaction.getUid());
        if (existingRecurringTransaction != null) {
            existingRecurringTransaction.setInterwal(interval);
            existingRecurringTransaction.setDataOd(startDate);
            recurringDAO.update(existingRecurringTransaction);
        } else {
            TransakcjaCyklicznaEntity newRecurringTransaction = new TransakcjaCyklicznaEntity();
            newRecurringTransaction.setIdTransakcji(transaction.getUid());
            newRecurringTransaction.setInterwal(interval);
            newRecurringTransaction.setDataOd(startDate);
            recurringDAO.insert(newRecurringTransaction);
        }
    }

    public TransakcjaCyklicznaEntity getRecurringTransaction(int transactionUid) {
        return recurringDAO.findByIdTransakcji(transactionUid);
    }

    public void removeRecurringTransaction(TransakcjaEntity transaction) {
        recurringDAO.deleteRecurringTransactionById(transaction.getUid());
    }

    public boolean isRecurring(TransakcjaEntity transaction) {
        return recurringDAO.findByIdTransakcji(transaction.getUid()) != null || transaction.isCyclicChild();
    }

    private TransakcjaCyklicznaDAO getRecurringTransactionDao() {
        BazaDanych db = activity.getDb();
        return db.transakcjaCyklicznaDAO();
    }

    public void updateCategoryAfterTransaction(TransakcjaEntity transakcja) {
        KategoriaDAO kategoriaDAO = activity.getDb().kategoriaDAO();
        KategoriaEntity kategoria = kategoriaDAO.findByName(transakcja.getKategoria());
        if (kategoria != null && kategoria.getLimit() != null && !kategoria.getLimit().isEmpty()) {
            kategoria.aktualnaKwota += Double.parseDouble(transakcja.getKwota());
            kategoriaDAO.update(kategoria);
        }
    }


}
