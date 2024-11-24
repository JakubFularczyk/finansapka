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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.androidproject.R;
import com.example.androidproject.baza.BazaDanych;
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

    private EditText kwotaInput, opisInput;
    private Spinner kategoriaSpinner, okresRozliczeniowySpinner;
    private TextView datePickerText;
    private CheckBox cyclicPaymentCheckbox;
    private int transactionUid;

    public EdycjaTransakcjiFragment() {
    }

    public static EdycjaTransakcjiFragment newInstance() {
        return new EdycjaTransakcjiFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edycja_transakcji, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicjalizacja widoków
        kwotaInput = view.findViewById(R.id.kwotaInput);
        kategoriaSpinner = view.findViewById(R.id.kategoriaSpinner);
        opisInput = view.findViewById(R.id.opisInput);
        datePickerText = view.findViewById(R.id.datePickerText);
        cyclicPaymentCheckbox = view.findViewById(R.id.cyclicPaymentCheckbox);
        okresRozliczeniowySpinner = view.findViewById(R.id.okresRozliczeniowySpinner);

        // Pobieranie danych z argumentów
        if (getArguments() != null) {
            transactionUid = getArguments().getInt("uid");
            String kwota = getArguments().getString("kwota");
            String kategoria = getArguments().getString("kategoria");
            Date data = (Date) getArguments().getSerializable("data");
            String opis = getArguments().getString("opis");

            // Wypełnienie pól
            kwotaInput.setText(kwota);
            opisInput.setText(opis);
            datePickerText.setText(formatDate(data));

            // Inicjalizacja Spinnera kategorii
            initKategoriaSpinner(kategoria);
        }

        Button openDatePickerButton = view.findViewById(R.id.openDatePickerButton);
        openDatePickerButton.setOnClickListener(v -> openDatePicker());

        // Przycisk zapisu zmian
        Button potwierdzEdycjeButton = view.findViewById(R.id.potwierdzEdycjeTransakcjiButton);
        potwierdzEdycjeButton.setOnClickListener(v -> zapiszZmiany(view));

        // Przycisk anulowania edycji
        ImageButton anulujButton = view.findViewById(R.id.anulujEdycjeButton);
        anulujButton.setOnClickListener(v -> Navigation.findNavController(view)
                .navigate(R.id.action_edycjaTransakcjiFragment_to_historiaTransakcjiFragment));
    }


    private void openDatePicker() {
        // Wyciągnięcie obecnej daty z pola datePickerText
        Calendar calendar = Calendar.getInstance();
        try {
            Date currentDate = parseDateFromText(datePickerText.getText().toString());
            calendar.setTime(currentDate);
        } catch (Exception e) {
            e.printStackTrace(); // W przypadku błędu używamy bieżącej daty
        }

        // Stworzenie DatePickerDialog
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Ustawienie wybranej daty w TextView
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    datePickerText.setText(formatDate(selectedDate.getTime()));
                }, year, month, day);

        datePickerDialog.show();
    }

    private void initKategoriaSpinner(String selectedCategory) {
        // Pobieranie kategorii z MainActivity
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            List<String> categories = activity.getCategories();

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            kategoriaSpinner.setAdapter(adapter);

            // Ustawienie wybranej kategorii
            if (selectedCategory != null) {
                int position = categories.indexOf(selectedCategory);
                if (position >= 0) {
                    kategoriaSpinner.setSelection(position);
                }
            }
        }
    }

    private void zapiszZmiany(View view) {
        // Pobranie zmienionych danych z pól
        String nowaKwota = kwotaInput.getText().toString();
        String nowaKategoria = kategoriaSpinner.getSelectedItem().toString();
        String nowyOpis = opisInput.getText().toString();
        Date nowaData = parseDateFromText(datePickerText.getText().toString());

        // Aktualizacja transakcji w bazie danych
        TransakcjaDAO transakcjaDao = getTransakcjaDao();
        TransakcjaEntity updatedTransaction = new TransakcjaEntity();
        updatedTransaction.setUid(transactionUid);
        updatedTransaction.setKwota(nowaKwota);
        updatedTransaction.setKategoria(nowaKategoria);
        updatedTransaction.setData(nowaData);
        updatedTransaction.setOpis(nowyOpis);
        transakcjaDao.update(updatedTransaction);

        // Powrót do historii transakcji
        NavController navController = Navigation.findNavController(view);
        navController.navigate(R.id.action_edycjaTransakcjiFragment_to_historiaTransakcjiFragment);
    }

    private Date parseDateFromText(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy");
        try {
            return format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
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
}
