package com.example.androidproject.service;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.androidproject.stronaglowna.MainActivity;
import com.example.androidproject.baza.dao.KategoriaDAO;
import com.example.androidproject.baza.dao.TransakcjaCyklicznaDAO;
import com.example.androidproject.baza.dao.TransakcjaDAO;
import com.example.androidproject.baza.encje.KategoriaEntity;
import com.example.androidproject.baza.encje.TransakcjaCyklicznaEntity;
import com.example.androidproject.baza.encje.TransakcjaEntity;
import com.example.androidproject.utils.TransactionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TransactionsService {
    MainActivity activity;

    TransakcjaCyklicznaDAO recurringDAO;
    TransakcjaDAO transactionDAO;
    KategoriaDAO categoryDAO;

    public static TransakcjaEntity createUpdatedTransaction(int transactionUid, double amount, String category, String description, Date date) {
        TransakcjaEntity updatedTransaction = new TransakcjaEntity();
        updatedTransaction.setUid(transactionUid);
        updatedTransaction.setKwota(String.valueOf(amount));
        updatedTransaction.setKategoria(category);
        updatedTransaction.setData(date);
        updatedTransaction.setOpis(description);
        return updatedTransaction; //TODO dodalem tutaj static jakby cos sie psulo w edycji to tu spojrz
    }

    public boolean isRecurring(TransakcjaEntity transaction) {
        return recurringDAO.findByIdTransakcji(transaction.getUid()) != null || transaction.isCyclicChild();
    }

    public static void validateInputs(String amountText, Date date) throws IllegalArgumentException {
        if (date == null) {
            throw new IllegalArgumentException("Nieprawidłowa data");
        }
        if (amountText.isEmpty()) {
            throw new IllegalArgumentException("Proszę podać kwotę");
        }
    }

    public TransactionsService(MainActivity activity) {
        this.activity = activity;
        recurringDAO = activity.getDb().transakcjaCyklicznaDAO();
        transactionDAO = activity.getDb().transakcjaDAO();
        categoryDAO = activity.getDb().kategoriaDAO();
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

    public void updateCategoryAfterTransaction(TransakcjaEntity transakcja) {
        KategoriaEntity kategoria = categoryDAO.findByName(transakcja.getKategoria());
        if (kategoria != null && kategoria.getLimit() != null && !kategoria.getLimit().isEmpty()) {
            double aktualnaKwota = kategoria.aktualnaKwota != null ? kategoria.aktualnaKwota : 0.0;
            aktualnaKwota += parseOrDefault(transakcja.getKwota(), 0.0);
            kategoria.aktualnaKwota = aktualnaKwota;
            categoryDAO.update(kategoria);
        }
    }

    public TransakcjaEntity prepareTransactionEntity(String kwota, String kategoria, String opis, Date data) {
        TransakcjaEntity transakcjaEntity = new TransakcjaEntity();
        transakcjaEntity.setKwota(kwota);
        transakcjaEntity.setKategoria(kategoria);
        transakcjaEntity.setOpis(opis);
        transakcjaEntity.setData(data);
        return transakcjaEntity;
    }

    public void addTransaction(String kwotaTekst, boolean isIncome, String kategoria, String opis, Date data, boolean isCyclic, String interval) throws IllegalArgumentException {
        if (kwotaTekst.isEmpty()) {
            throw new IllegalArgumentException("Proszę podać kwotę");
        }

        try {
            double kwota = Double.parseDouble(kwotaTekst);
            if (!isIncome) kwota = -kwota;


            TransakcjaEntity encja = prepareTransactionEntity(
                    String.valueOf(kwota),
                    kategoria,
                    opis,
                    data
            );
            encja.setParentTransactionId(null);
            encja.setCyclicChild(false);
            long transactionId = transactionDAO.insert(encja);
            encja.setUid((int) transactionId);
            if (isCyclic) {
                handleRecurringTransaction(encja, interval, data);
            }
            if (kwota < 0) {
                updateCategoryAfterTransaction(encja);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Nieprawidłowy format kwoty");
        }
    }

    private void updateCategoryCurrentAmount(TransakcjaEntity oldTransaction, TransakcjaEntity updatedTransaction) {
        KategoriaEntity category = categoryDAO.findByName(oldTransaction.getKategoria());

        if (category != null && category.getLimit() != null && !category.getLimit().isEmpty()) {
            double aktualnaKwota = category.aktualnaKwota != null ? category.aktualnaKwota : 0.0;

            // Odejmij starą kwotę od aktualnaKwota (jeśli to wydatek)
            if (Double.parseDouble(oldTransaction.getKwota()) < 0) {
                aktualnaKwota += Math.abs(Double.parseDouble(oldTransaction.getKwota()));
            }

            // Dodaj nową kwotę do aktualnaKwota (jeśli to wydatek)
            if (Double.parseDouble(updatedTransaction.getKwota()) < 0) {
                aktualnaKwota -= Math.abs(Double.parseDouble(updatedTransaction.getKwota()));
            }

            // Zapisz nową aktualną kwotę w kategorii
            category.aktualnaKwota = aktualnaKwota;

            // Zaktualizuj kategorię w bazie danych
            categoryDAO.update(category);
        }
    }

    public double parseAmount(String amountText, boolean isIncome) throws NumberFormatException {
        double amount = Double.parseDouble(amountText);
        return isIncome ? amount : -amount;
    }

    public boolean determineTransactionType(String amountText) {
        double amount = Double.parseDouble(amountText);
        return amount >= 0;
    }

    public String formatAmount(String amountText) {
        double amount = Math.abs(Double.parseDouble(amountText));
        return String.valueOf(amount);
    }

    public Date parseDateFromText(String dateText) {
        SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy", new Locale("pl"));
        try {
            return format.parse(dateText);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String formatDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy", new Locale("pl"));
        return format.format(date);
    }

    public void getCategoryAdapter(@NonNull MainActivity activity, String selectedCategory, @NonNull Spinner spinner, Context context) {
        List<KategoriaEntity> categories = activity.getCategories();
        List<String> categoryNames = categories.stream().map(KategoriaEntity::getNazwa).collect(Collectors.toList());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Wybierz domyślną kategorię, jeśli podano
        if (selectedCategory != null) {
            int position = categoryNames.indexOf(selectedCategory);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
    }

    public void configureRecurringTransaction(int transactionUid, CheckBox cyclicPaymentCheckbox, Spinner intervalSpinner, Context context) {
        TransakcjaCyklicznaEntity recurringTransaction = getRecurringTransaction(transactionUid);
        if (recurringTransaction != null) {
            String interval = recurringTransaction.getInterwal();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, TransactionUtils.OPTIONS);
            intervalSpinner.setAdapter(adapter);
            intervalSpinner.setSelection(TransactionUtils.OPTIONS.indexOf(interval));
            cyclicPaymentCheckbox.setChecked(true);
            intervalSpinner.setVisibility(View.VISIBLE);
        } else {
            cyclicPaymentCheckbox.setChecked(false);
            intervalSpinner.setVisibility(View.GONE);
        }
    }

    public TransakcjaEntity loadTransactionArguments(Bundle arguments) {
        if (arguments == null) {
            return null;
        }
        int uid = arguments.getInt("uid");
        String kwota = arguments.getString("kwota");
        String kategoria = arguments.getString("kategoria");
        Date data = (Date) arguments.getSerializable("data");
        String opis = arguments.getString("opis");

        TransakcjaEntity transakcja = new TransakcjaEntity();
        transakcja.setUid(uid);
        transakcja.setKwota(kwota);
        transakcja.setKategoria(kategoria);
        transakcja.setData(data);
        transakcja.setOpis(opis);

        return transakcja;
    }

    public void showDatePicker(Context context, String currentDateText, TextView datePickerTextView) {
        Calendar calendar = Calendar.getInstance();

        try {
            Date currentDate = parseDateFromText(currentDateText);
            if (currentDate != null) {
                calendar.setTime(currentDate);
            }
        } catch (Exception ignored) {}

        openDatePickerDialog(
                context,
                calendar,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    datePickerTextView.setText(formatDate(selectedDate.getTime()));
                }
        );
    }

    public void openDatePickerDialog(Context context, Calendar calendar, DatePickerDialog.OnDateSetListener listener) {
        new DatePickerDialog(
                context,
                listener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    public void saveTransactionChanges(
            int transactionUid,
            String amountText,
            String newCategory,
            String newDescription,
            Date newDate,
            boolean isIncome,
            boolean wasCyclic,
            boolean isCyclic,
            String interval
    ) throws IllegalArgumentException {
        validateInputs(amountText, newDate);

        double amount = parseAmount(amountText, isIncome);

        TransakcjaEntity oldTransaction = transactionDAO.getTransactionByUid(transactionUid);

        TransakcjaEntity updatedTransaction = createUpdatedTransaction(
                transactionUid,
                amount,
                newCategory,
                newDescription,
                newDate
        );

        updateCategoryCurrentAmount(oldTransaction, updatedTransaction);
        transactionDAO.update(updatedTransaction);

        if (isCyclic) {
            Date startDate = Calendar.getInstance().getTime();
            handleRecurringTransaction(updatedTransaction, interval, startDate);
        } else {
            if (wasCyclic) {
                removeRecurringTransaction(updatedTransaction);
            }
        }
    }

    public List<TransakcjaEntity> getFilteredTransactions(String filterType) {
        switch (filterType) {
            case "oldest":
                return transactionDAO.getOldest();
            case "newest":
                return transactionDAO.getNewest();
            case "income":
                return transactionDAO.getByType(true);
            case "expenses":
                return transactionDAO.getByType(false);
            case "highest":
                return transactionDAO.getHighest();
            case "lowest":
                return transactionDAO.getLowest();
            default:
                return transactionDAO.getAllSortedByDate();
        }
    }

    public Bundle createTransactionBundle(TransakcjaEntity transaction) {
        Bundle bundle = new Bundle();
        bundle.putInt("uid", transaction.getUid());
        bundle.putString("kwota", transaction.getKwota());
        bundle.putString("kategoria", transaction.getKategoria());
        bundle.putSerializable("data", transaction.getData());
        bundle.putString("opis", transaction.getOpis());
        return bundle;
    }

    public void logTransactions(List<TransakcjaEntity> transactions) {
        for (TransakcjaEntity transaction : transactions) {
            Log.d("TransactionLog", transaction.toString());
        }
    }

    private double parseOrDefault(String value, double defaultValue) {
        try {
            return value != null ? Double.parseDouble(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

}
