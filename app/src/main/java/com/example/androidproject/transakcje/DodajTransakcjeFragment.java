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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.androidproject.R;
import com.example.androidproject.baza.BazaDanych;
import com.example.androidproject.baza.TransactionUtils;
import com.example.androidproject.customviews.CustomSwitch;
import com.example.androidproject.stronaglowna.MainActivity;
import com.example.androidproject.transakcje.dao.KategoriaDAO;
import com.example.androidproject.transakcje.dao.TransakcjaCyklicznaDAO;
import com.example.androidproject.transakcje.dao.TransakcjaDAO;
import com.example.androidproject.transakcje.encje.KategoriaEntity;
import com.example.androidproject.transakcje.encje.TransakcjaCyklicznaEntity;
import com.example.androidproject.transakcje.encje.TransakcjaEntity;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
                Arrays.asList("Dzienna", "Tygodniowa", "Miesięczna", "Roczna")
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
        View openDatePickerButton = view.findViewById(R.id.openDatePickerButton);
        FrameLayout openDatePickerContainer = view.findViewById(R.id.openDatePickerContainer);

        if (isChecked) {
            datePickerText.setText(formatDate(new Date()));
            toggleDatePickerEnabled(false, openDatePickerButton, openDatePickerContainer);
            okresRozliczeniowySpinner.setVisibility(View.VISIBLE);
        } else {
            toggleDatePickerEnabled(true, openDatePickerButton, openDatePickerContainer);
            okresRozliczeniowySpinner.setVisibility(View.GONE);
        }
    }

    private void toggleDatePickerEnabled(boolean isEnabled, View button, FrameLayout container) {
        datePickerText.setEnabled(isEnabled);
        button.setEnabled(isEnabled);
        container.setEnabled(isEnabled);
        container.setClickable(isEnabled);
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
        String kwotaTekst = kwotaInput.getText().toString().trim();
        if (kwotaTekst.isEmpty()) {
            Toast.makeText(requireContext(), "Proszę podać kwotę", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double kwota = Double.parseDouble(kwotaTekst);
            if (!isIncome) kwota = -kwota;

            TransakcjaEntity encja = prepareTransactionEntity(
                    String.valueOf(kwota),
                    kategoriaSpinner.getSelectedItem().toString(),
                    opisInput.getText().toString(),
                    localCalendar.getTime()
            );

            TransakcjaDAO transakcjaDAO = getTransakcjaDao();
            long transactionId = transakcjaDAO.insert(encja);
            encja.setUid((int) transactionId);
            if (cyclicPaymentCheckbox.isChecked()) {
                handleRecurringTransaction(encja);
            }


            if (kwota < 0) {
                updateCategoryAfterTransaction(encja);
            }


            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_dodajTransakcjeFragment_to_transakcjaDodanaFragment);

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Nieprawidłowy format kwoty", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleRecurringTransaction(TransakcjaEntity transaction) {
        String interval = okresRozliczeniowySpinner.getSelectedItem().toString();
        Date nextDate = TransactionUtils.calculateNextDate(localCalendar.getTime(), interval);

        TransakcjaCyklicznaEntity recurringTransaction = new TransakcjaCyklicznaEntity();
        recurringTransaction.setIdTransakcji(transaction.getUid());
        recurringTransaction.setDataOd(nextDate);
        recurringTransaction.setInterwal(interval);

        TransakcjaCyklicznaDAO recurringDao = getRecurringTransactionDao();
        recurringDao.insert(recurringTransaction);

        Log.d("RecurringTransaction", "Recurring transaction scheduled for: " + nextDate);
    }

    private void updateCategoryAfterTransaction(TransakcjaEntity transakcja) {
        KategoriaDAO kategoriaDAO = getKategoriaDao();
        KategoriaEntity kategoria = kategoriaDAO.findByName(transakcja.getKategoria());
        if (kategoria != null && kategoria.getLimit() != null && !kategoria.getLimit().isEmpty()) {
            kategoria.aktualnaKwota += Double.parseDouble(transakcja.getKwota());
            kategoriaDAO.update(kategoria);
        }
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

    private KategoriaDAO  getKategoriaDao() {
        MainActivity activity = (MainActivity) getActivity();
        BazaDanych db = activity.getDb();
        return db.kategoriaDAO();
    }
    private TransakcjaCyklicznaDAO getRecurringTransactionDao() {
        MainActivity activity = (MainActivity) getActivity();
        BazaDanych db = activity.getDb();
        return db.transakcjaCyklicznaDAO();
    }

    private TransakcjaEntity prepareTransactionEntity(String kwota, String kategoria, String opis, Date data) {
        TransakcjaEntity transakcjaEntity = new TransakcjaEntity();
        transakcjaEntity.setKwota(kwota);
        transakcjaEntity.setKategoria(kategoria);
        transakcjaEntity.setOpis(opis);
        transakcjaEntity.setData(data);
        return transakcjaEntity;
    }

    private static void setupNavigationToMenu(@NonNull View view, int actionId) {
        ImageButton przejdzDoMenuButton = view.findViewById(R.id.przejdzDoMenuButton);
        przejdzDoMenuButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(actionId);
        });
    }


}
