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
import com.example.androidproject.transakcje.dao.TransakcjaDAO;
import com.example.androidproject.transakcje.encje.TransakcjaEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EdycjaTransakcjiFragment extends Fragment {

    // Pola widoków
    private EditText kwotaInput, opisInput;
    private Spinner kategoriaSpinner, okresRozliczeniowySpinner;
    private TextView datePickerText;
    private CheckBox cyclicPaymentCheckbox;
    private int transactionUid;
    private CustomSwitch transactionTypeCustomSwitch;

    // Konstruktor
    public EdycjaTransakcjiFragment() {}

    public static EdycjaTransakcjiFragment newInstance() {
        return new EdycjaTransakcjiFragment();
    }

    // Tworzenie widoku
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edycja_transakcji, container, false);
    }

    // Inicjalizacja widoków i logiki fragmentu
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        loadArguments();

        setupListeners(view);
    }

    // Metoda inicjalizująca widoki
    private void initViews(View view) {
        kwotaInput = view.findViewById(R.id.kwotaInput);
        kategoriaSpinner = view.findViewById(R.id.kategoriaSpinner);
        opisInput = view.findViewById(R.id.opisInput);
        datePickerText = view.findViewById(R.id.datePickerText);
        cyclicPaymentCheckbox = view.findViewById(R.id.cyclicPaymentCheckbox);
        okresRozliczeniowySpinner = view.findViewById(R.id.okresRozliczeniowySpinner);
        transactionTypeCustomSwitch = view.findViewById(R.id.transactionTypeCustomSwitch);
    }

    // Metoda ładująca dane przekazane w argumentach
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

    // Wypełnianie pól danymi
    private void fillFields(String kwotaString, String kategoria, Date data, String opis) {
        datePickerText.setText(formatDate(data));
        setTransactionType(kwotaString);

        kwotaInput.setText(String.valueOf(Math.abs(Double.parseDouble(kwotaString))));
        opisInput.setText(opis);
        initKategoriaSpinner(kategoria);
    }

    // Ustawienie typu transakcji
    private void setTransactionType(String kwotaString) {
        double kwota = Double.parseDouble(kwotaString);
        if (kwota < 0) {
            transactionTypeCustomSwitch.setChecked(false); // Wydatek
        } else {
            transactionTypeCustomSwitch.setChecked(true); // Przychód
        }
    }

    // Inicjalizacja spinnera kategorii
    private void initKategoriaSpinner(String selectedCategory) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            List<String> categories = activity.getCategories();

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, categories);
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

    // Ustawienie listenerów dla przycisków
    private void setupListeners(View view) {
        view.findViewById(R.id.openDatePickerButton).setOnClickListener(v -> openDatePicker());
        view.findViewById(R.id.potwierdzEdycjeTransakcjiButton).setOnClickListener(v -> zapiszZmiany(view));
        view.findViewById(R.id.anulujEdycjeButton).setOnClickListener(v -> cancelEdit(view));
    }

    // Otwieranie DatePickerDialog
    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        try {
            Date currentDate = parseDateFromText(datePickerText.getText().toString());
            if (currentDate != null) {
                calendar.setTime(currentDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    // Zapisywanie zmian
    private void zapiszZmiany(View view) {
        String kwotaTekst = kwotaInput.getText().toString().trim();
        String nowaKategoria = kategoriaSpinner.getSelectedItem().toString();
        String nowyOpis = opisInput.getText().toString();
        Date nowaData = parseDateFromText(datePickerText.getText().toString());

        if (!validateInputs(kwotaTekst, nowaData)) return;

        try {
            double kwota = parseKwota(kwotaTekst);

            TransakcjaEntity updatedTransaction = createUpdatedTransaction(kwota, nowaKategoria, nowyOpis, nowaData);
            getTransakcjaDao().update(updatedTransaction);

            navigateToHistory(view);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Nieprawidłowy format kwoty", Toast.LENGTH_SHORT).show();
        }
    }

    // Walidacja danych wejściowych
    private boolean validateInputs(String kwotaTekst, Date nowaData) {
        if (nowaData == null) {
            Toast.makeText(requireContext(), "Nieprawidłowa data", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (kwotaTekst.isEmpty()) {
            Toast.makeText(requireContext(), "Proszę podać kwotę", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // Parsowanie kwoty z pola tekstowego
    private double parseKwota(String kwotaTekst) {
        double kwota = Double.parseDouble(kwotaTekst);
        if (!transactionTypeCustomSwitch.isChecked()) {
            kwota = -kwota;
        }
        return kwota;
    }

    // Tworzenie zaktualizowanej transakcji
    private TransakcjaEntity createUpdatedTransaction(double kwota, String nowaKategoria, String nowyOpis, Date nowaData) {
        TransakcjaEntity updatedTransaction = new TransakcjaEntity();
        updatedTransaction.setUid(transactionUid);
        updatedTransaction.setKwota(String.valueOf(kwota));
        updatedTransaction.setKategoria(nowaKategoria);
        updatedTransaction.setData(nowaData);
        updatedTransaction.setOpis(nowyOpis);
        return updatedTransaction;
    }

    // Nawigacja do historii
    private void navigateToHistory(View view) {
        NavController navController = Navigation.findNavController(view);
        navController.navigate(R.id.action_edycjaTransakcjiFragment_to_historiaTransakcjiFragment);
    }

    // Anulowanie edycji
    private void cancelEdit(View view) {
        Navigation.findNavController(view)
                .navigate(R.id.action_edycjaTransakcjiFragment_to_historiaTransakcjiFragment);
    }

    // Parsowanie daty z tekstu
    private Date parseDateFromText(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy", new Locale("pl"));
        try {
            return format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Formatowanie daty do tekstu
    private String formatDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy", new Locale("pl"));
        return format.format(date);
    }

    // Pobieranie obiektu DAO
    private TransakcjaDAO getTransakcjaDao() {
        MainActivity activity = (MainActivity) getActivity();
        BazaDanych db = activity.getDb();
        return db.transakcjaDAO();
    }
}
