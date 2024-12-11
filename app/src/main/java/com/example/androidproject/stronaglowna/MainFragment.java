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
import com.example.androidproject.baza.encje.UserEntity;
import com.example.androidproject.service.MainPageService;
import com.example.androidproject.service.TransactionsService;
import com.example.androidproject.utils.TransactionUtils;
import com.example.androidproject.baza.dao.KategoriaDAO;
import com.example.androidproject.baza.dao.TransakcjaDAO;
import com.example.androidproject.baza.encje.KategoriaEntity;
import com.example.androidproject.baza.encje.TransakcjaCyklicznaEntity;
import com.example.androidproject.baza.encje.TransakcjaEntity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MainFragment extends Fragment {


    private TextView welcomeTextView;
    private TextView balanceTextView;
    private MainPageService mainPageService;
    private ListView transactionListView;
    private TextView noTransactionsTextView;
    private TransactionsService transactionsService;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        transactionsService = new TransactionsService((MainActivity) requireActivity());
        mainPageService = new MainPageService((MainActivity) requireActivity());
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupNavigation(view);
        loadData();
        checkLimitNotifications(view);
        checkRecurringTransactions();
    }

    private void checkLimitNotifications(View view) {
        View notificationBadge = view.findViewById(R.id.notificationBadge);
        boolean hasLimitNotification = mainPageService.checkLimitNotifications();
        notificationBadge.setVisibility(hasLimitNotification ? View.VISIBLE : View.GONE);
    }

    private void initializeViews(@NonNull View view) {
        welcomeTextView = view.findViewById(R.id.welcomeTextView);
        balanceTextView = view.findViewById(R.id.balanceTextView);
        transactionListView = view.findViewById(R.id.transactionListView);
        noTransactionsTextView = view.findViewById(R.id.noTransactionsTextView);

        ImageButton notificationButton = view.findViewById(R.id.notificationButton);
        notificationButton.setOnClickListener(v -> showNotifications());
    }
    private void loadData() {
        welcomeTextView.setText(mainPageService.getUserWelcomeMessage());
        loadBalanceData();
        loadTransactionList();
    }
    private void loadBalanceData() {
        double balance = mainPageService.loadBalanceData();
        balanceTextView.setText(String.format("%.2f PLN", balance));
        int colorResId = balance < 0 ? android.R.color.holo_red_dark : android.R.color.holo_green_dark;
        balanceTextView.setTextColor(getResources().getColor(colorResId));
    }
    private void loadTransactionList() {
        List<TransakcjaEntity> latestTransactions = mainPageService.getLatestTransactions();

        if (latestTransactions.isEmpty()) {
            noTransactionsTextView.setVisibility(View.VISIBLE);
            transactionListView.setVisibility(View.GONE);
        } else {
            noTransactionsTextView.setVisibility(View.GONE);
            transactionListView.setVisibility(View.VISIBLE);

            TransakcjeEkranGlownyAdapter adapter = new TransakcjeEkranGlownyAdapter(requireContext(), latestTransactions);
            transactionListView.setAdapter(adapter);
        }
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


    private void showNotifications() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.powiadomienia, null);
        View notificationBadge = requireActivity().findViewById(R.id.notificationBadge);
        View limitNotificationFrame = bottomSheetView.findViewById(R.id.limitNotificationFrame);
        View subscriptionNotificationFrame = bottomSheetView.findViewById(R.id.subscriptionNotificationFrame);
        TextView limitNotificationTextView = bottomSheetView.findViewById(R.id.limitNotificationTextView);
        Button przejdzDoLimitowButton = bottomSheetView.findViewById(R.id.przejdzDoLimitowButton);
        TextView subscriptionNotificationTextView = bottomSheetView.findViewById(R.id.subscriptionNotificationTextView);
        Button takButton = bottomSheetView.findViewById(R.id.takButton);
        Button nieButton = bottomSheetView.findViewById(R.id.nieButton);
        Button zmienDateButton = bottomSheetView.findViewById(R.id.zmienDateButton);

        MainActivity mainActivity = (MainActivity) getActivity();
        BazaDanych db = mainActivity.getDb();

        boolean hasLimitNotification = handleLimitNotifications(db, limitNotificationTextView, przejdzDoLimitowButton, bottomSheetDialog);
        boolean hasRecurringNotification = handleRecurringTransactions(db, subscriptionNotificationTextView, takButton, nieButton, zmienDateButton, bottomSheetDialog);

        updateNotificationVisibility(notificationBadge, limitNotificationFrame, subscriptionNotificationFrame, hasLimitNotification, hasRecurringNotification);

        // Wyświetlenie dialogu
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private boolean handleLimitNotifications(BazaDanych db, TextView limitNotificationTextView, Button przejdzDoLimitowButton, BottomSheetDialog bottomSheetDialog) {
        List<KategoriaEntity> categories = db.kategoriaDAO().getAll();
        for (KategoriaEntity category : categories) {
            if (isNearLimit(category)) {
                limitNotificationTextView.setText("Zbliżasz się do limitu kategorii " + category.getNazwa() + "!");
                przejdzDoLimitowButton.setOnClickListener(v -> {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_mainFragment_to_historiaLimitowKategoriiFragment);
                    bottomSheetDialog.dismiss();
                });
                return true;
            }
        }
        return false;
    }

    private boolean handleRecurringTransactions(BazaDanych db, TextView subscriptionNotificationTextView, Button takButton, Button nieButton, Button zmienDateButton, BottomSheetDialog bottomSheetDialog) {
        List<TransakcjaCyklicznaEntity> recurringTransactions = db.transakcjaCyklicznaDAO().getAll();
        Date today = new Date();
        for (TransakcjaCyklicznaEntity recurring : recurringTransactions) {
            Date updatedNextDate = TransactionUtils.calculateNextDate(recurring.getDataOd(), recurring.getInterwal());
            if (!updatedNextDate.after(today)) {
                TransakcjaEntity linkedTransaction = db.transakcjaDAO().getTransactionByUid(recurring.getIdTransakcji());
                if (linkedTransaction != null) {
                    subscriptionNotificationTextView.setText("Czy transakcja cykliczna " + linkedTransaction.getOpis() + " została wykonana?");
                    setupRecurringButtons(linkedTransaction, recurring, updatedNextDate, takButton, nieButton, zmienDateButton, bottomSheetDialog);
                    return true;
                }
            }
        }
        return false;
    }

    private void setupRecurringButtons(TransakcjaEntity transaction, TransakcjaCyklicznaEntity recurringTransaction, Date updatedNextDate, Button takButton, Button nieButton, Button zmienDateButton, BottomSheetDialog bottomSheetDialog) {
        takButton.setOnClickListener(v -> {
            addTransactionFromRecurring(transaction, recurringTransaction, updatedNextDate);
            bottomSheetDialog.dismiss();
            loadBalanceData();
        });
        nieButton.setOnClickListener(v -> {
            updateNextRecurringDate(recurringTransaction, updatedNextDate);
            bottomSheetDialog.dismiss();
        });
        zmienDateButton.setOnClickListener(v -> {
            showDatePickerToReschedule(recurringTransaction);
            bottomSheetDialog.dismiss();
        });
    }

    private boolean isNearLimit(KategoriaEntity category) {
        if (category.getLimit() == null || category.getLimit().isEmpty()) return false;
        try {
            double limit = Double.parseDouble(category.getLimit());
            return limit > 0 && Math.abs(category.getAktualnaKwota()) >= 0.85 * limit;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void updateNotificationVisibility(View notificationBadge, View limitNotificationFrame, View subscriptionNotificationFrame, boolean hasLimitNotification, boolean hasRecurringNotification) {
        limitNotificationFrame.setVisibility(hasLimitNotification ? View.VISIBLE : View.GONE);
        subscriptionNotificationFrame.setVisibility(hasRecurringNotification ? View.VISIBLE : View.GONE);
        notificationBadge.setVisibility(hasLimitNotification || hasRecurringNotification ? View.VISIBLE : View.GONE);
    }
    private void addTransactionFromRecurring(TransakcjaEntity transaction, TransakcjaCyklicznaEntity recurringTransaction, Date updatedNextDate) {
        MainActivity mainActivity = (MainActivity) getActivity();
        BazaDanych db = mainActivity.getDb();
        TransakcjaDAO transakcjaDAO = db.transakcjaDAO();

        TransakcjaEntity newTransaction = new TransakcjaEntity();
        newTransaction.setKwota(transaction.getKwota());
        newTransaction.setKategoria(transaction.getKategoria());
        newTransaction.setOpis(transaction.getOpis());
        newTransaction.setData(updatedNextDate);
        newTransaction.setCyclicChild(true);
        newTransaction.setParentTransactionId(transaction.getUid());

        transakcjaDAO.insert(newTransaction);


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
        loadBalanceData();
    }

    private void refreshTransactionList() {
        MainActivity mainActivity = (MainActivity) getActivity();
        BazaDanych db = mainActivity.getDb();
        TransakcjaDAO transakcjaDAO = db.transakcjaDAO();
        List<TransakcjaEntity> najnowszeTransakcje = transakcjaDAO.getLatest3();
        ListView transactionListView = requireView().findViewById(R.id.transactionListView);
        TransakcjeEkranGlownyAdapter adapter = new TransakcjeEkranGlownyAdapter(requireContext(), najnowszeTransakcje);
        transactionListView.setAdapter(adapter);
    }

    private void updateNextRecurringDate(TransakcjaCyklicznaEntity recurringTransaction, Date updatedNextDate) {
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
        boolean hasRecurringNotification = mainPageService.hasRecurringTransactions();
        View notificationBadge = getActivity().findViewById(R.id.notificationBadge);
        notificationBadge.setVisibility(hasRecurringNotification ? View.VISIBLE : View.GONE);
    }
}
