package com.example.androidproject.transakcje;

import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DodajTransakcjeFragment extends Fragment {
    private Calendar localCalendar = Calendar.getInstance();
    private EditText kwotaInput, opisInput;
    private Spinner kategoriaSpinner, okresRozliczeniowySpinner;
    private Button openDatePickerButton;
    private TextView datePickerText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dodaj_transakcje, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicjalizacja widoków
        MainActivity mainActivity = (MainActivity) requireActivity();
        powrocDoMenu(view, R.id.action_dodajTransakcjeFragment_to_mainFragment);

        kwotaInput = view.findViewById(R.id.kwotaInput);
        opisInput = view.findViewById(R.id.opisInput);
        kategoriaSpinner = view.findViewById(R.id.kategoriaSpinner);
        datePickerText  = view.findViewById(R.id.datePickerText);
        openDatePickerButton = view.findViewById(R.id.openDatePickerButton);


        // Spinner dla kategorii
        List<String> categories = mainActivity.getCategories();
        ArrayAdapter<String> kategoriaAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, categories);
        kategoriaSpinner.setAdapter(kategoriaAdapter);

        // Spinner dla okresu rozliczeniowego
        CheckBox cyclicPaymentCheckbox = view.findViewById(R.id.cyclicPaymentCheckbox);
        okresRozliczeniowySpinner = view.findViewById(R.id.okresRozliczeniowySpinner);
        ArrayAdapter<String> okresAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("Dzienna", "Tygodniowa", "Miesięczna", "Roczna")
        );
        okresRozliczeniowySpinner.setAdapter(okresAdapter);

        cyclicPaymentCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                okresRozliczeniowySpinner.setVisibility(View.VISIBLE);
            } else {
                okresRozliczeniowySpinner.setVisibility(View.GONE);
            }
        });

        // Obsługa wyboru daty
        Button openDatePickerButton = view.findViewById(R.id.openDatePickerButton);
        openDatePickerButton.setOnClickListener(v -> openDatePicker());


        if (localCalendar != null) {
            datePickerText.setText(formatDate(localCalendar.getTime()));
        }

        // Dodanie transakcji do bazy danych
        Button dodajTransakcjeButton = view.findViewById(R.id.dodajTransakcjeButton2);
        dodajTransakcjeButton.setOnClickListener(v -> {
            Log.d(getTag(), "Dodawanie transakcji...");
            TransakcjaDAO transakcjaDAO = getTransakcjaDao();
            TransakcjaEntity encja = przygotujEncje(
                    kwotaInput.getText().toString(),
                    kategoriaSpinner.getSelectedItem().toString(),
                    opisInput.getText().toString(),
                    localCalendar.getTime()
            );

            transakcjaDAO.insert(encja);

            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_dodajTransakcjeFragment_to_transakcjaDodanaFragment);
        });
    }

    private void openDatePicker() {
        // Ustawienie początkowej daty
        int year = localCalendar.get(Calendar.YEAR);
        int month = localCalendar.get(Calendar.MONTH);
        int day = localCalendar.get(Calendar.DAY_OF_MONTH);

        // Utworzenie DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    localCalendar.set(selectedYear, selectedMonth, selectedDay);
                    datePickerText.setText(formatDate(localCalendar.getTime()));
                }, year, month, day);

        datePickerDialog.show();
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("pl"));
        return sdf.format(date);
    }

    private TransakcjaDAO getTransakcjaDao() {
        MainActivity activity = (MainActivity) getActivity();
        BazaDanych db = activity.getDb();
        return db.transakcjaDAO();
    }

    private TransakcjaEntity przygotujEncje(String kwota, String kategoria, String opis, Date data) {
        TransakcjaEntity transakcjaEntity = new TransakcjaEntity();
        transakcjaEntity.setKwota(kwota);
        transakcjaEntity.setKategoria(kategoria);
        transakcjaEntity.setOpis(opis);
        transakcjaEntity.setData(data);
        return transakcjaEntity;
    }

    private static void powrocDoMenu(@NonNull View view, int action_dodajTransakcjeFragment_to_mainFragment) {
        ImageButton przejdzDoMenuButton = view.findViewById(R.id.przejdzDoMenuButton);
        przejdzDoMenuButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(action_dodajTransakcjeFragment_to_mainFragment);
        });
    }
}
