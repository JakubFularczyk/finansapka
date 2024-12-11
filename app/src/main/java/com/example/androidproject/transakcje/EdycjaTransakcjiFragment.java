package com.example.androidproject.transakcje;

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
import com.example.androidproject.utils.TransactionUtils;
import com.example.androidproject.customviews.CustomSwitch;
import com.example.androidproject.stronaglowna.MainActivity;

import com.example.androidproject.baza.encje.TransakcjaEntity;
import com.example.androidproject.service.TransactionsService;

import java.util.Date;


public class EdycjaTransakcjiFragment extends Fragment {

    private EditText kwotaInput, opisInput;
    private Spinner kategoriaSpinner, okresRozliczeniowySpinner;
    private TextView datePickerText;
    private CheckBox cyclicPaymentCheckbox;
    private CustomSwitch transactionTypeCustomSwitch;
    private int transactionUid;
    private TransactionsService transactionsService;
    private boolean wasCyclic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        transactionsService = new TransactionsService((MainActivity) requireActivity());
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

        cyclicPaymentCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    okresRozliczeniowySpinner.setVisibility(View.VISIBLE);
                } else {
                    okresRozliczeniowySpinner.setVisibility(View.GONE);
                }
        });

        ArrayAdapter<String> okresAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                TransactionUtils.OPTIONS
        );
        okresRozliczeniowySpinner.setAdapter(okresAdapter);
    }

    private void loadArguments() {
        if (getArguments() != null) {
            TransakcjaEntity transakcja = transactionsService.loadTransactionArguments(getArguments());
            if (transakcja != null) {
                transactionUid = transakcja.getUid();
                wasCyclic = getArguments().getBoolean("cykliczna");
                fillFields(
                        transakcja.getKwota(),
                        transakcja.getKategoria(),
                        transakcja.getData(),
                        transakcja.getOpis()
                );
            }
        }
    }

    private void fillFields(String kwotaString, String kategoria, Date data, String opis) {
        datePickerText.setText(transactionsService.formatDate(data));
        transactionTypeCustomSwitch.setChecked(transactionsService.determineTransactionType(kwotaString));
        kwotaInput.setText(transactionsService.formatAmount(kwotaString));
        opisInput.setText(opis);
        initKategoriaSpinner(kategoria);
        cyclicPaymentCheckbox.setChecked(wasCyclic);

        transactionsService.configureRecurringTransaction(transactionUid, cyclicPaymentCheckbox, okresRozliczeniowySpinner, requireContext());
    }

    private void initKategoriaSpinner(String selectedCategory) {
        transactionsService.getCategoryAdapter(
                (MainActivity) requireActivity(),
                selectedCategory,
                kategoriaSpinner,
                requireContext()
        );
    }

    private void setupListeners(View view) {
        view.findViewById(R.id.openDatePickerButton).setOnClickListener(v -> openDatePicker());
        view.findViewById(R.id.potwierdzEdycjeTransakcjiButton).setOnClickListener(v -> saveChanges(view));
        view.findViewById(R.id.anulujEdycjeButton).setOnClickListener(v -> cancelEdit(view));
    }

    private void openDatePicker() {
        transactionsService.showDatePicker(
                requireContext(),
                datePickerText.getText().toString(),
                datePickerText
        );
    }

    private void saveChanges(View view) {
        String kwotaTekst = kwotaInput.getText().toString().trim();
        String newCategory = kategoriaSpinner.getSelectedItem().toString();
        String newDescription = opisInput.getText().toString();
        Date newDate = transactionsService.parseDateFromText(datePickerText.getText().toString());

        try {
            transactionsService.saveTransactionChanges(
                    transactionUid,
                    kwotaTekst,
                    newCategory,
                    newDescription,
                    newDate,
                    transactionTypeCustomSwitch.isChecked(),
                    wasCyclic,
                    cyclicPaymentCheckbox.isChecked(),
                    okresRozliczeniowySpinner.getSelectedItem() != null
                            ? okresRozliczeniowySpinner.getSelectedItem().toString()
                            : null
            );

            navigateToHistory(view);
        } catch (IllegalArgumentException e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToHistory(View view) {
        NavController navController = Navigation.findNavController(view);
        navController.navigate(R.id.action_edycjaTransakcjiFragment_to_historiaTransakcjiFragment);
    }

    private void cancelEdit(View view) {
        navigateToHistory(view);
    }


}
