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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.androidproject.R;
import com.example.androidproject.utils.TransactionUtils;
import com.example.androidproject.customviews.CustomSwitch;
import com.example.androidproject.stronaglowna.MainActivity;
import com.example.androidproject.baza.encje.KategoriaEntity;
import com.example.androidproject.service.TransactionsService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DodajTransakcjeFragment extends Fragment {

    private Calendar localCalendar = Calendar.getInstance();
    private EditText kwotaInput, opisInput;
    private Spinner kategoriaSpinner, okresRozliczeniowySpinner;
    private TextView datePickerText;
    private boolean isIncome = true;
    private CheckBox cyclicPaymentCheckbox;
    private TransactionsService transactionsService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        transactionsService = new TransactionsService((MainActivity) requireActivity());
        return inflater.inflate(R.layout.fragment_dodaj_transakcje, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupCategorySpinner();
        setupRecurringPaymentCheckbox(view);
        setupDatePicker(view);
        setupAddTransactionButton(view);
        setupNavigationToMenu(view, R.id.action_dodajTransakcjeFragment_to_mainFragment);


    }

    private void initializeViews(View view) {
        kwotaInput = view.findViewById(R.id.kwotaInput);
        opisInput = view.findViewById(R.id.opisInput);
        kategoriaSpinner = view.findViewById(R.id.kategoriaSpinner);
        datePickerText = view.findViewById(R.id.datePickerText);
        okresRozliczeniowySpinner = view.findViewById(R.id.okresRozliczeniowySpinner);
        cyclicPaymentCheckbox = view.findViewById(R.id.cyclicPaymentCheckbox);

        ArrayAdapter<String> okresAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                //Arrays.asList("Dzienna", "Tygodniowa", "Miesięczna", "Roczna")
                TransactionUtils.OPTIONS
        );
        okresRozliczeniowySpinner.setAdapter(okresAdapter);

        CustomSwitch transactionTypeCustomSwitch = view.findViewById(R.id.transactionTypeCustomSwitch);
        transactionTypeCustomSwitch.setChecked(true);
        transactionTypeCustomSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> isIncome = isChecked);
    }


    private void setupCategorySpinner() {
        MainActivity mainActivity = (MainActivity) requireActivity();
        List<KategoriaEntity> categories = mainActivity.getCategories();
        List<String> categoriesNames = categories.stream() //
                .map(c -> c.getNazwa()) //
                .collect(Collectors.toList());
        ArrayAdapter<String> kategoriaAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, categoriesNames);
        kategoriaSpinner.setAdapter(kategoriaAdapter);

    }

    private void setupRecurringPaymentCheckbox(View view) {
        cyclicPaymentCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleRecurringPaymentViews(view, isChecked));
    }

    private void toggleRecurringPaymentViews(View view, boolean isChecked) {
        // Ustaw widoczność spinnera intewałów
        if (isChecked) {
            okresRozliczeniowySpinner.setVisibility(View.VISIBLE);
        } else {
            okresRozliczeniowySpinner.setVisibility(View.GONE);
        }
    }

    private void setupDatePicker(View view) {
        Button openDatePickerButton = view.findViewById(R.id.openDatePickerButton);
        openDatePickerButton.setOnClickListener(v -> openDatePicker());

        if (localCalendar != null) {
            datePickerText.setText(formatDate(localCalendar.getTime()));
        }
    }

    private void openDatePicker() {
        int year = localCalendar.get(Calendar.YEAR);
        int month = localCalendar.get(Calendar.MONTH);
        int day = localCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    localCalendar.set(selectedYear, selectedMonth, selectedDay);
                    datePickerText.setText(formatDate(localCalendar.getTime()));
                }, year, month, day);
        datePickerDialog.show();
    }

    private void setupAddTransactionButton(View view) {
        Button dodajTransakcjeButton = view.findViewById(R.id.dodajTransakcjeButton2);
        dodajTransakcjeButton.setOnClickListener(v -> addTransaction());
    }

    private void addTransaction() {
        try {
            transactionsService.addTransaction(
                    kwotaInput.getText().toString().trim(),
                    isIncome,
                    kategoriaSpinner.getSelectedItem().toString(),
                    opisInput.getText().toString(),
                    localCalendar.getTime(),
                    cyclicPaymentCheckbox.isChecked(),
                    okresRozliczeniowySpinner.getSelectedItem() != null
                            ? okresRozliczeniowySpinner.getSelectedItem().toString()
                            : null
            );

            // Nawigacja po udanym dodaniu transakcji
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_dodajTransakcjeFragment_to_transakcjaDodanaFragment);

        } catch (IllegalArgumentException e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("pl"));
        return sdf.format(date);
    }


    private static void setupNavigationToMenu(@NonNull View view, int actionId) {
        ImageButton przejdzDoMenuButton = view.findViewById(R.id.przejdzDoMenuButton);
        przejdzDoMenuButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(actionId);
        });
    }


}
