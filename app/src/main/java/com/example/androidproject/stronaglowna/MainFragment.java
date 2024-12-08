package com.example.androidproject.stronaglowna;

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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidproject.R;
import com.example.androidproject.baza.BazaDanych;
import com.example.androidproject.adaptery.TransakcjeEkranGlownyAdapter;
import com.example.androidproject.transakcje.dao.UserDAO;
import com.example.androidproject.transakcje.encje.UserEntity;
import com.example.androidproject.utils.TransactionUtils;
import com.example.androidproject.transakcje.dao.KategoriaDAO;
import com.example.androidproject.transakcje.dao.TransakcjaDAO;
import com.example.androidproject.transakcje.encje.KategoriaEntity;
import com.example.androidproject.transakcje.encje.TransakcjaCyklicznaEntity;
import com.example.androidproject.transakcje.encje.TransakcjaEntity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainFragment extends Fragment {

    public MainFragment() {}

    private TextView welcomeTextView;
    private TextView balanceTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupNavigation(view);
        loadUserData();
        loadBalanceData(view);
        loadHistoricalData(view);
        checkLimitNotifications(view);
        checkRecurringTransactions();
    }

    private void checkLimitNotifications(View view) {
        View notificationBadge = view.findViewById(R.id.notificationBadge);


        MainActivity mainActivity = (MainActivity) getActivity();
        BazaDanych db = mainActivity.getDb();
        List<KategoriaEntity> categories = db.kategoriaDAO().getAll();

        boolean hasLimitNotification = false;

        // Sprawdzamy limity kategorii
        for (KategoriaEntity category : categories) {
            if (category.getLimit() != null && !category.getLimit().isEmpty()) {
                double limit = Double.parseDouble(category.getLimit());
                if (limit > 0 && Math.abs(category.getAktualnaKwota()) >= 0.85 * limit) {
                    hasLimitNotification = true;
                    break;
                }
            }
        }

        notificationBadge.setVisibility(hasLimitNotification ? View.VISIBLE : View.GONE);
        notificationBadge.setVisibility(hasLimitNotification ? View.VISIBLE : View.GONE);
    }

    private void initializeViews(@NonNull View view) {
        welcomeTextView = view.findViewById(R.id.welcomeTextView);
        balanceTextView = view.findViewById(R.id.balanceTextView);

        ImageButton notificationButton = view.findViewById(R.id.notificationButton);
        notificationButton.setOnClickListener(v -> showNotifications());
    }

    private void setupNavigation(@NonNull View view) {
        setupNavigationButton(view, R.id.dodajTransakcjeButton, R.id.action_mainFragment_to_dodajTransakcjeFragment);
        setupNavigationButton(view, R.id.ustawieniaButton, R.id.action_mainFragment_to_ustawieniaFragment);
        setupNavigationButton(view, R.id.wyswietlWszystkoButton, R.id.action_mainFragment_to_historiaTransakcjiFragment);
        setupNavigationButton(view, R.id.analizaButton, R.id.action_mainFragment_to_analizaFinansowFragment);
    }

    private void setupNavigationButton(@NonNull View view, int buttonId, int actionId) {
        Button button = view.findViewById(buttonId);
        button.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(actionId);
        });
    }
    private void loadUserData() {
        String username = capitalizeFirstLetter(((MainActivity) requireActivity()).getDb().userDAO().findById(1).getName());
        welcomeTextView.setText("Witaj, " + username);
    }

    private String capitalizeFirstLetter(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    private void loadBalanceData(@NonNull View view) {

        MainActivity mainActivity = (MainActivity) getActivity();
        BazaDanych db = mainActivity.getDb();
        TransakcjaDAO transakcjaDAO = db.transakcjaDAO();
        List<TransakcjaEntity> wszystkieTransakcje = transakcjaDAO.getAll();

        double balance = calculateBalance(wszystkieTransakcje);
        updateBalanceTextView(balance);
    }

    private double calculateBalance(List<TransakcjaEntity> transactions) {
        return transactions.stream()
                .mapToDouble(t -> Double.parseDouble(t.getKwota()))
                .sum();
    }

    private void updateBalanceTextView(double balance) {
        balanceTextView.setText(String.format("%.2f PLN", balance));
        int colorResId = balance < 0 ? android.R.color.holo_red_dark : android.R.color.holo_green_dark;
        balanceTextView.setTextColor(getResources().getColor(colorResId));
    }

    private void loadHistoricalData(@NonNull View view) {
        ListView transactionListView = view.findViewById(R.id.transactionListView);
        TextView noTransactionsTextView = view.findViewById(R.id.noTransactionsTextView);

        MainActivity mainActivity = (MainActivity) getActivity();
        BazaDanych db = mainActivity.getDb();
        TransakcjaDAO transakcjaDAO = db.transakcjaDAO();
        List<TransakcjaEntity> najnowszeTransakcje = transakcjaDAO.getLatest3();

        if (najnowszeTransakcje.isEmpty()) {
            noTransactionsTextView.setVisibility(View.VISIBLE);
            transactionListView.setVisibility(View.GONE);
        } else {
            noTransactionsTextView.setVisibility(View.GONE);
            transactionListView.setVisibility(View.VISIBLE);

            TransakcjeEkranGlownyAdapter adapter = new TransakcjeEkranGlownyAdapter(requireContext(), najnowszeTransakcje);
            transactionListView.setAdapter(adapter);
        }
    }

    private void showNotifications() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.powiadomienia, null);
        View notificationBadge = getActivity().findViewById(R.id.notificationBadge);
        // Sekcje layoutu
        View limitNotificationFrame = bottomSheetView.findViewById(R.id.limitNotificationFrame);
        View subscriptionNotificationFrame = bottomSheetView.findViewById(R.id.subscriptionNotificationFrame);
        TextView limitNotificationTextView = bottomSheetView.findViewById(R.id.limitNotificationTextView);
        Button przejdzDoLimitowButton = bottomSheetView.findViewById(R.id.przejdzDoLimitowButton);

        TextView subscriptionNotificationTextView = bottomSheetView.findViewById(R.id.subscriptionNotificationTextView);
        Button takButton = bottomSheetView.findViewById(R.id.takButton);
        Button nieButton = bottomSheetView.findViewById(R.id.nieButton);
        Button zmienDateButton = bottomSheetView.findViewById(R.id.zmienDateButton);

        // Pobranie danych
        MainActivity mainActivity = (MainActivity) getActivity();
        BazaDanych db = mainActivity.getDb();
        List<KategoriaEntity> categories = db.kategoriaDAO().getAll();
        List<TransakcjaCyklicznaEntity> recurringTransactions = db.transakcjaCyklicznaDAO().getAll();

        boolean hasLimitNotification = false;
        boolean hasRecurringNotification = false;

        // Sprawdzanie limitów kategorii
        for (KategoriaEntity category : categories) {
            try {
                if (category.getLimit() == null || category.getLimit().isEmpty()) {
                    continue; // Ignoruj kategorię, jeśli limit jest null lub pusty
                }

                double limit = Double.parseDouble(category.getLimit());
                double aktualnaKwota = category.getAktualnaKwota();

                if (limit > 0 && Math.abs(aktualnaKwota) >= 0.85 * limit) {
                    hasLimitNotification = true;
                    limitNotificationTextView.setText("Zbliżasz się do limitu kategorii " + category.getNazwa() + "!");
                    przejdzDoLimitowButton.setOnClickListener(v -> {
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigate(R.id.action_mainFragment_to_historiaLimitowKategoriiFragment);

                        bottomSheetDialog.dismiss();
                    });
                    break;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        // Sprawdzanie transakcji cyklicznych
        for (TransakcjaCyklicznaEntity recurring : recurringTransactions) {
            Date today = new Date();
            Log.d("MainFragment", "today: " + today);
            Date nextTransactionDate = recurring.getDataOd();
            String interval = recurring.getInterwal();
            Date updatedNextDate = TransactionUtils.calculateNextDate(nextTransactionDate, interval);
            Log.d("MainFragment", "updatedNextDate: " + updatedNextDate);
            if (updatedNextDate.before(today) || updatedNextDate.equals(today)) {
                hasRecurringNotification = true;
                TransakcjaEntity linkedTransaction = db.transakcjaDAO().getTransactionByUid(recurring.getIdTransakcji());
                if (linkedTransaction != null) {
                    subscriptionNotificationTextView.setText("Czy transakcja cykliczna " + linkedTransaction.getOpis() + " została wykonana?");
                    takButton.setOnClickListener(v -> {
                        addTransactionFromRecurring(linkedTransaction, recurring, updatedNextDate);
                        //updateNextRecurringDate(recurring);
                        bottomSheetDialog.dismiss();
                        loadBalanceData(requireView());
                    });
                    nieButton.setOnClickListener(v -> {
                        updateNextRecurringDate(recurring, updatedNextDate);
                        bottomSheetDialog.dismiss();
                    });
                    zmienDateButton.setOnClickListener(v -> {
                        showDatePickerToReschedule(recurring);
                        bottomSheetDialog.dismiss();
                    });
                    break;
                }
            }
        }

        // Ustawianie widoczności sekcji
        if (hasLimitNotification) {
            limitNotificationFrame.setVisibility(View.VISIBLE);
            notificationBadge.setVisibility(View.VISIBLE);
        } else {
            limitNotificationFrame.setVisibility(View.GONE);
        }

        if (hasRecurringNotification) {
            subscriptionNotificationFrame.setVisibility(View.VISIBLE);
            notificationBadge.setVisibility(View.VISIBLE);
        } else {
            subscriptionNotificationFrame.setVisibility(View.GONE);
        }

        if (!hasLimitNotification && !hasRecurringNotification) {
            notificationBadge.setVisibility(View.GONE);
        }

        // Wyświetlenie dialogu
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
    private void addTransactionFromRecurring(TransakcjaEntity transaction, TransakcjaCyklicznaEntity recurringTransaction, Date updatedNextDate) {
        MainActivity mainActivity = (MainActivity) getActivity();
        BazaDanych db = mainActivity.getDb();
        TransakcjaDAO transakcjaDAO = db.transakcjaDAO();

        /*Date nextTransactionDate = recurringTransaction.getDataOd();

        String interval = recurringTransaction.getInterwal();
        Date updatedNextDate = TransactionUtils.calculateNextDate(nextTransactionDate, interval);*/

        // Tworzenie nowej transakcji
        TransakcjaEntity newTransaction = new TransakcjaEntity();
        newTransaction.setKwota(transaction.getKwota());
        newTransaction.setKategoria(transaction.getKategoria());
        newTransaction.setOpis(transaction.getOpis());
        newTransaction.setData(updatedNextDate); // nowa najblizsza data (po poprzedniej)
        newTransaction.setCyclicChild(true);
        newTransaction.setParentTransactionId(transaction.getUid());

        // Wstawienie transakcji
        transakcjaDAO.insert(newTransaction);

        // Zapisz kolejną datę w recurringTransaction
        recurringTransaction.setDataOd(updatedNextDate);
        db.transakcjaCyklicznaDAO().update(recurringTransaction);

        KategoriaDAO kategoriaDAO = db.kategoriaDAO();
        KategoriaEntity kategoria = kategoriaDAO.findByName(transaction.getKategoria());
        if (kategoria != null && kategoria.getLimit() != null && !kategoria.getLimit().isEmpty()) {
            kategoria.aktualnaKwota += Double.parseDouble(transaction.getKwota());
            kategoriaDAO.update(kategoria);
        }

        // Powiadomienie użytkownika
        Toast.makeText(requireContext(), "Dodano transakcję cykliczną!", Toast.LENGTH_SHORT).show();
        Toast.makeText(requireContext(), "Data kolejnej transakcji cyklicznej: " + updatedNextDate, Toast.LENGTH_SHORT).show();
        // Odświeżenie listy transakcji
        refreshTransactionList();

        // Odświeżenie salda
        loadBalanceData(requireView());
    }

    private void refreshTransactionList() {
        MainActivity mainActivity = (MainActivity) getActivity();
        BazaDanych db = mainActivity.getDb();
        TransakcjaDAO transakcjaDAO = db.transakcjaDAO();

        // Pobranie trzech najnowszych transakcji
        List<TransakcjaEntity> najnowszeTransakcje = transakcjaDAO.getLatest3();

        // Tworzenie nowego adaptera za każdym razem
        ListView transactionListView = requireView().findViewById(R.id.transactionListView);
        TransakcjeEkranGlownyAdapter adapter = new TransakcjeEkranGlownyAdapter(requireContext(), najnowszeTransakcje);
        transactionListView.setAdapter(adapter);
    }

    private void updateNextRecurringDate(TransakcjaCyklicznaEntity recurringTransaction, Date updatedNextDate) {
        /*String interval = recurringTransaction.getInterwal();
        Date nextDate = TransactionUtils.calculateNextDate(recurringTransaction.getDataOd(), interval);*/

        recurringTransaction.setDataOd(updatedNextDate);
        MainActivity mainActivity = (MainActivity) getActivity();
        BazaDanych db = mainActivity.getDb();
        db.transakcjaCyklicznaDAO().update(recurringTransaction);

        Toast.makeText(requireContext(), "Data kolejnej transakcji cyklicznej: " + updatedNextDate, Toast.LENGTH_SHORT).show();
    }

    private void showDatePickerToReschedule(TransakcjaCyklicznaEntity recurringTransaction) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(recurringTransaction.getDataOd());

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            recurringTransaction.setDataOd(calendar.getTime());
            MainActivity mainActivity = (MainActivity) getActivity();
            BazaDanych db = mainActivity.getDb();
            db.transakcjaCyklicznaDAO().update(recurringTransaction);

            Toast.makeText(requireContext(), "Przypomnienie zaktualizowane!", Toast.LENGTH_SHORT).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void checkRecurringTransactions() {
        MainActivity mainActivity = (MainActivity) getActivity();
        BazaDanych db = mainActivity.getDb();

        // Pobierz wszystkie transakcje cykliczne
        List<TransakcjaCyklicznaEntity> recurringTransactions = db.transakcjaCyklicznaDAO().getAll();

        Date today = new Date();
        boolean hasRecurringNotification = false;

        // Sprawdzenie dat transakcji cyklicznych
        for (TransakcjaCyklicznaEntity recurring : recurringTransactions) {
            if (recurring.getDataOd().before(today) || recurring.getDataOd().equals(today)) {
                // Jeśli istnieje transakcja na dziś lub wcześniejsza, ustaw flagę
                hasRecurringNotification = true;
                break;
            }
        }

        // Ustawienie widoczności badge
        View notificationBadge = getActivity().findViewById(R.id.notificationBadge);
        notificationBadge.setVisibility(hasRecurringNotification ? View.VISIBLE : View.GONE);
    }

    private boolean hasExceededCategoryLimit(List<KategoriaEntity> categories) {
        for (KategoriaEntity category : categories) {
            try {
                if (category.getLimit() != null && !category.getLimit().isEmpty()) {
                    double limit = Double.parseDouble(category.getLimit());
                    if (limit > 0 && Math.abs(category.getAktualnaKwota()) >= 0.85 * limit) {
                        return true;
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace(); // Logowanie błędu, jeśli limit jest w złym formacie
            }
        }
        return false;
    }
}
