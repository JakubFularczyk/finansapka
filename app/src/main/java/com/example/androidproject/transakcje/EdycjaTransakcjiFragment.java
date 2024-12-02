package com.example.androidproject.transakcje;

import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidproject.R;
import com.example.androidproject.baza.BazaDanych;
import com.example.androidproject.customviews.CustomSwitch;
import com.example.androidproject.stronaglowna.MainActivity;
import com.example.androidproject.transakcje.dao.KategoriaDAO;
import com.example.androidproject.transakcje.dao.TransakcjaDAO;
import com.example.androidproject.transakcje.encje.KategoriaEntity;
import com.example.androidproject.transakcje.encje.TransakcjaEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class EdycjaTransakcjiFragment extends Fragment {

    private EditText kwotaInput, opisInput;
    private Spinner kategoriaSpinner, okresRozliczeniowySpinner;
    private TextView datePickerText;
    private CheckBox cyclicPaymentCheckbox;
    private CustomSwitch transactionTypeCustomSwitch;
    private int transactionUid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edycja_transakcji, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        loadArguments();
        setupListeners(view);
    }

    private void initViews(View view) {
        kwotaInput = view.findViewById(R.id.kwotaInput);
        opisInput = view.findViewById(R.id.opisInput);
        kategoriaSpinner = view.findViewById(R.id.kategoriaSpinner);
        okresRozliczeniowySpinner = view.findViewById(R.id.okresRozliczeniowySpinner);
        datePickerText = view.findViewById(R.id.datePickerText);
        cyclicPaymentCheckbox = view.findViewById(R.id.cyclicPaymentCheckbox);
        transactionTypeCustomSwitch = view.findViewById(R.id.transactionTypeCustomSwitch);
    }

    private void loadArguments() {
        if (getArguments() != null) {
            transactionUid = getArguments().getInt("uid");
            String kwotaString = getArguments().getString("kwota");
            String kategoria = getArguments().getString("kategoria");
            Date data = (Date) getArguments().getSerializable("data");
            String opis = getArguments().getString("opis");

            fillFields(kwotaString, kategoria, data, opis);
        }
    }

    private void fillFields(String kwotaString, String kategoria, Date data, String opis) {
        datePickerText.setText(formatDate(data));
        setTransactionType(kwotaString);
        kwotaInput.setText(formatAmount(kwotaString));
        opisInput.setText(opis);
        initKategoriaSpinner(kategoria);
    }

    private void setTransactionType(String kwotaString) {
        double kwota = Double.parseDouble(kwotaString);
        transactionTypeCustomSwitch.setChecked(kwota >= 0);
    }

    private String formatAmount(String kwotaString) {
        return String.valueOf(Math.abs(Double.parseDouble(kwotaString)));
    }

    private void initKategoriaSpinner(String selectedCategory) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            List<KategoriaEntity> categories = activity.getCategories();
            List<String> categoriesNames = categories.stream() //
                    .map(c -> c.getNazwa()) //
                    .collect(Collectors.toList());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, categoriesNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            kategoriaSpinner.setAdapter(adapter);

            if (selectedCategory != null) {
                int position = categories.indexOf(selectedCategory);
                if (position >= 0) {
                    kategoriaSpinner.setSelection(position);
                }
            }
        }
    }

    private void setupListeners(View view) {
        view.findViewById(R.id.openDatePickerButton).setOnClickListener(v -> openDatePicker());
        view.findViewById(R.id.potwierdzEdycjeTransakcjiButton).setOnClickListener(v -> saveChanges(view));
        view.findViewById(R.id.anulujEdycjeButton).setOnClickListener(v -> cancelEdit(view));
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        try {
            Date currentDate = parseDateFromText(datePickerText.getText().toString());
            if (currentDate != null) {
                calendar.setTime(currentDate);
            }
        } catch (Exception ignored) {}

        new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    datePickerText.setText(formatDate(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void saveChanges(View view) {
        String kwotaTekst = kwotaInput.getText().toString().trim();
        String newCategory = kategoriaSpinner.getSelectedItem().toString();
        String newDescription = opisInput.getText().toString();
        Date newDate = parseDateFromText(datePickerText.getText().toString());

        if (!validateInputs(kwotaTekst, newDate)) return;

        try {
            double amount = parseAmount(kwotaTekst);

            TransakcjaEntity oldTransaction = getTransakcjaDao().getTransactionByUid(transactionUid);
            TransakcjaEntity updatedTransaction = createUpdatedTransaction(amount, newCategory, newDescription, newDate);
            updateCategoryCurrentAmount(oldTransaction, updatedTransaction);
            getTransakcjaDao().update(updatedTransaction);
            navigateToHistory(view);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Nieprawidłowy format kwoty", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCategoryCurrentAmount(TransakcjaEntity oldTransaction, TransakcjaEntity updatedTransaction) {
        KategoriaDAO kategoriaDAO = getKategoriaDao();

        KategoriaEntity category = kategoriaDAO.findByName(oldTransaction.getKategoria());

        if (category != null && category.getLimit() != null && !category.getLimit().isEmpty()) {
            double aktualnaKwota = category.aktualnaKwota;

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
            kategoriaDAO.update(category);
        }
    }

    private boolean validateInputs(String amountText, Date date) {
        if (date == null) {
            Toast.makeText(requireContext(), "Nieprawidłowa data", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (amountText.isEmpty()) {
            Toast.makeText(requireContext(), "Proszę podać kwotę", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private double parseAmount(String amountText) {
        double amount = Double.parseDouble(amountText);
        if (!transactionTypeCustomSwitch.isChecked()) {
            amount = -amount;
        }
        return amount;
    }

    private TransakcjaEntity createUpdatedTransaction(double amount, String category, String description, Date date) {
        TransakcjaEntity updatedTransaction = new TransakcjaEntity();
        updatedTransaction.setUid(transactionUid);
        updatedTransaction.setKwota(String.valueOf(amount));
        updatedTransaction.setKategoria(category);
        updatedTransaction.setData(date);
        updatedTransaction.setOpis(description);
        return updatedTransaction;
    }

    private void navigateToHistory(View view) {
        NavController navController = Navigation.findNavController(view);
        navController.navigate(R.id.action_edycjaTransakcjiFragment_to_historiaTransakcjiFragment);
    }

    private void cancelEdit(View view) {
        navigateToHistory(view);
    }

    private Date parseDateFromText(String dateText) {
        SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy", new Locale("pl"));
        try {
            return format.parse(dateText);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String formatDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy", new Locale("pl"));
        return format.format(date);
    }

    private TransakcjaDAO getTransakcjaDao() {
        MainActivity activity = (MainActivity) getActivity();
        BazaDanych db = activity.getDb();
        return db.transakcjaDAO();
    }
    private KategoriaDAO getKategoriaDao() {
        MainActivity activity = (MainActivity) getActivity();
        BazaDanych db = activity.getDb();
        return db.kategoriaDAO();
    }
}
