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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidproject.R;
import com.example.androidproject.baza.BazaDanych;
import com.example.androidproject.customviews.CustomSwitch;
import com.example.androidproject.stronaglowna.MainActivity;
import com.example.androidproject.transakcje.dao.TransakcjaDAO;
import com.example.androidproject.transakcje.encje.TransakcjaEntity;
import com.google.android.material.switchmaterial.SwitchMaterial;

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
    private TextView datePickerText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dodaj_transakcje, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity mainActivity = (MainActivity) requireActivity();
        powrocDoMenu(view, R.id.action_dodajTransakcjeFragment_to_mainFragment);

        kwotaInput = view.findViewById(R.id.kwotaInput);
        opisInput = view.findViewById(R.id.opisInput);
        kategoriaSpinner = view.findViewById(R.id.kategoriaSpinner);
        datePickerText = view.findViewById(R.id.datePickerText);
        CustomSwitch transactionTypeCustomSwitch = view.findViewById(R.id.transactionTypeCustomSwitch);
        CheckBox cyclicPaymentCheckbox = view.findViewById(R.id.cyclicPaymentCheckbox);

        final boolean[] isIncome = {true};
        transactionTypeCustomSwitch.setChecked(true);


        transactionTypeCustomSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isIncome[0] = isChecked;
        });


        List<String> categories = mainActivity.getCategories();
        ArrayAdapter<String> kategoriaAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, categories);
        kategoriaSpinner.setAdapter(kategoriaAdapter);


        cyclicPaymentCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            View openDatePickerButton = view.findViewById(R.id.openDatePickerButton);
            FrameLayout openDatePickerContainer = view.findViewById(R.id.openDatePickerContainer);

            if (isChecked) {
                // Blokowanie przycisku wyboru daty
                datePickerText.setText(formatDate(new Date()));
                datePickerText.setEnabled(false);
                openDatePickerButton.setEnabled(false);
                openDatePickerContainer.setEnabled(false);
                openDatePickerContainer.setClickable(false);

                // Pokazanie okresu rozliczeniowego
                okresRozliczeniowySpinner.setVisibility(View.VISIBLE);
            } else {
                // Odblokowanie przycisku wyboru daty
                datePickerText.setEnabled(true);
                openDatePickerButton.setEnabled(true);
                openDatePickerContainer.setEnabled(true);
                openDatePickerContainer.setClickable(true);

                // Ukrycie okresu rozliczeniowego
                okresRozliczeniowySpinner.setVisibility(View.GONE);
            }
        });
        okresRozliczeniowySpinner = view.findViewById(R.id.okresRozliczeniowySpinner);
        ArrayAdapter<String> okresAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("Dzienna", "Tygodniowa", "Miesięczna", "Roczna")
        );
        okresRozliczeniowySpinner.setAdapter(okresAdapter);



        Button openDatePickerButton = view.findViewById(R.id.openDatePickerButton);
        openDatePickerButton.setOnClickListener(v -> openDatePicker());

        if (localCalendar != null) {
            datePickerText.setText(formatDate(localCalendar.getTime()));
        }


        Button dodajTransakcjeButton = view.findViewById(R.id.dodajTransakcjeButton2);
        dodajTransakcjeButton.setOnClickListener(v -> {
            TransakcjaDAO transakcjaDAO = getTransakcjaDao();
            String kwotaTekst = kwotaInput.getText().toString().trim();
            if (kwotaTekst.isEmpty()) {
                Toast.makeText(requireContext(), "Proszę podać kwotę", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                double kwota = Double.parseDouble(kwotaTekst);
                if (!isIncome[0]) {
                    kwota = -kwota;
                }
                TransakcjaEntity encja = przygotujEncje(
                        String.valueOf(kwota),
                        kategoriaSpinner.getSelectedItem().toString(),
                        opisInput.getText().toString(),
                        localCalendar.getTime()
                );
                transakcjaDAO.insert(encja);
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_dodajTransakcjeFragment_to_transakcjaDodanaFragment);

            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Nieprawidłowy format kwoty", Toast.LENGTH_SHORT).show();
            }
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
        transakcjaEntity.setKwota(kwota); // Ustawiamy kwotę jako String
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
