package com.example.androidproject.service;

import com.example.androidproject.baza.dao.KategoriaDAO;
import com.example.androidproject.baza.dao.TransakcjaCyklicznaDAO;
import com.example.androidproject.baza.dao.TransakcjaDAO;
import com.example.androidproject.baza.encje.KategoriaEntity;
import com.example.androidproject.baza.encje.TransakcjaCyklicznaEntity;
import com.example.androidproject.baza.encje.TransakcjaEntity;
import com.example.androidproject.baza.encje.UserEntity;
import com.example.androidproject.stronaglowna.MainActivity;

import java.util.Date;
import java.util.List;

public class MainPageService {
    MainActivity activity;
    TransakcjaCyklicznaDAO recurringDAO;
    TransakcjaDAO transactionDAO;
    KategoriaDAO categoryDAO;

    public MainPageService(MainActivity activity) {
        this.activity = activity;
        recurringDAO = activity.getDb().transakcjaCyklicznaDAO();
        transactionDAO = activity.getDb().transakcjaDAO();
        categoryDAO = activity.getDb().kategoriaDAO();
    }

    public String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    public double loadBalanceData() {
        return calculateBalance(transactionDAO.getAll());
    }

    public double calculateBalance(List<TransakcjaEntity> transactions) {
        return transactions.stream()
                .mapToDouble(t -> Double.parseDouble(t.getKwota()))
                .sum();
    }

    public boolean checkLimitNotifications() {
        return categoryDAO.getAll().stream().anyMatch(this::isNearLimit);
    }

    private boolean isNearLimit(KategoriaEntity category) {
        if (category.getLimit() == null || category.getLimit().isEmpty()) return false;
        double limit = parseDoubleSafe(category.getLimit());
        return limit > 0 && Math.abs(category.getAktualnaKwota()) >= 0.85 * limit;
    }
    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public boolean hasRecurringTransactions() {
        Date today = new Date();
        return recurringDAO.getAll().stream()
                .anyMatch(r -> !r.getDataOd().after(today));
    }

    public List<TransakcjaEntity> getLatestTransactions() {
        return transactionDAO.getLatest3();
    }

    public String getUserWelcomeMessage() {
        UserEntity user = activity.getDb().userDAO().findById(1);
        if (user != null && user.getName() != null) {
            return "Witaj, " + capitalizeFirstLetter(user.getName());
        }
        return "Witaj, użytkowniku!";
    }
}
