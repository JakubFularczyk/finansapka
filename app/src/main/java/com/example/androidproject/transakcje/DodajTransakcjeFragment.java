package com.example.androidproject.transakcje;

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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import com.example.androidproject.R;
import com.example.androidproject.baza.BazaDanych;
import com.example.androidproject.stronaglowna.MainActivity;
import com.example.androidproject.transakcje.dao.TransakcjaDAO;
import com.example.androidproject.transakcje.encje.TransakcjaEntity;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DodajTransakcjeFragment extends Fragment {
    private Calendar localCalendar = Calendar.getInstance();

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


        CheckBox cyclicPaymentCheckbox = view.findViewById(R.id.cyclicPaymentCheckbox);
        Spinner okresRozliczeniowySpinner = view.findViewById(R.id.okresRozliczeniowySpinner);

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

        Spinner spinner = view.findViewById(R.id.kategoriaSpinner);
        List<String> categories = mainActivity.getCategories();
        ArrayAdapter<String> kategoriaAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        spinner.setAdapter(kategoriaAdapter);
        EditText kwotaInput = view.findViewById(R.id.kwotaInput);
        Button openDatePickerButton = view.findViewById(R.id.openDatePickerButton);
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Wybierz datę")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        openDatePickerButton.setOnClickListener(v -> {
            try {
                datePicker.show(requireActivity().getSupportFragmentManager(), "DATE_PICKER");
            } catch (Exception e) {
                Log.e("DodajTransakcjeFragment", "Błąd przy otwieraniu DatePicker: " + e.getMessage());
            }
        });
        datePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                localCalendar.setTimeInMillis(selection);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                openDatePickerButton.setText(sdf.format(localCalendar.getTime()));
            } else {
                Log.e("DodajTransakcjeFragment", "Nie udało się pobrać daty z DatePicker.");
            }
        });

        Button navigateToHistoryButton = view.findViewById(R.id.dodajTransakcjeButton2);
        navigateToHistoryButton.setOnClickListener(v -> {
            Log.d(getTag(), "Dane do zapisania: ");
            Log.d(getTag(), "Kwota: " + kwotaInput.getText());
            Log.d(getTag(), "Kategoria: " + spinner.getSelectedItem());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Log.d(getTag(), "Data: " + sdf.format(localCalendar.getTime()));


            TransakcjaDAO transakcjaDAO = getTransakcjaDao();
            TransakcjaEntity encja = przygotujEncje(
                    kwotaInput.getText().toString(),
                    spinner.getSelectedItem().toString(),
                    localCalendar.getTime()
            );
            transakcjaDAO.insert(encja);
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_dodajTransakcjeFragment_to_transakcjaDodanaFragment);
        });
    }

    private TransakcjaDAO getTransakcjaDao() {
        MainActivity activity = (MainActivity) getActivity();
        BazaDanych db = activity.getDb();
        return db.transakcjaDAO();
    }

    private TransakcjaEntity przygotujEncje(String kwota, String kategoria, Date data) {
        TransakcjaEntity transakcjaEntity = new TransakcjaEntity();
        transakcjaEntity.setKwota(kwota);
        transakcjaEntity.setKategoria(kategoria);
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