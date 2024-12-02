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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.example.androidproject.R;
import com.example.androidproject.adaptery.HistoriaTransakcjiAdapter;
import com.example.androidproject.baza.BazaDanych;
import com.example.androidproject.stronaglowna.MainActivity;
import com.example.androidproject.transakcje.dao.TransakcjaDAO;
import com.example.androidproject.transakcje.encje.TransakcjaEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoriaTransakcjiFragment extends Fragment {

    private ListView transactionListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historia_transakcji, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void initViews(View view) {
        setupAddTransactionButton(view);
        setupDoneButton(view);
        setupFilterIcon(view);
        transactionListView = view.findViewById(R.id.transactionListView);
        loadTransactions();
    }

    private void setupAddTransactionButton(View view) {
        Button dodajTransakcjeButton = view.findViewById(R.id.dodajKolejnaTransakcjeButton);
        dodajTransakcjeButton.setOnClickListener(this::navigateToAddTransaction);
    }

    private void setupDoneButton(View view) {
        Button doneButton = view.findViewById(R.id.gotoweButton);
        doneButton.setOnClickListener(this::navigateToMain);
    }

    private void setupFilterIcon(View view) {
        ImageView filterIcon = view.findViewById(R.id.filterIcon);
        filterIcon.setOnClickListener(this::showFilterMenu);
    }

    private void navigateToAddTransaction(View view) {
        NavController navController = Navigation.findNavController(view);
        navController.navigate(R.id.action_historiaTransakcjiFragment_to_dodajTransakcjeFragment);
    }

    private void navigateToMain(View view) {
        NavController navController = Navigation.findNavController(view);
        navController.navigate(R.id.action_historiaTransakcjiFragment_to_mainFragment);
    }

    private void loadTransactions() {
        List<TransakcjaEntity> transactions = getTransakcjaDao().getAllSortedByDate();
        logTransactions(transactions);
        setupTransactionListView(transactions);
    }

    private void logTransactions(List<TransakcjaEntity> transactions) {
        for (TransakcjaEntity transaction : transactions) {
            Log.d(getTag(), transaction.getKwota() + " " + transaction.getKategoria() + " " + transaction.getData());
        }
    }

    private void setupTransactionListView(List<TransakcjaEntity> transactions) {
        HistoriaTransakcjiAdapter adapter = new HistoriaTransakcjiAdapter(requireContext(), transactions);
        transactionListView.setAdapter(adapter);
        transactionListView.setOnItemClickListener((parent, view, position, id) -> navigateToEditTransaction(view, transactions.get(position)));
    }

    private void navigateToEditTransaction(View view, TransakcjaEntity transaction) {
        Bundle bundle = createTransactionBundle(transaction);
        NavController navController = Navigation.findNavController(view);
        navController.navigate(R.id.action_historiaTransakcjiFragment_to_edycjaTransakcjiFragment, bundle);
    }

    private Bundle createTransactionBundle(TransakcjaEntity transaction) {
        Bundle bundle = new Bundle();
        bundle.putInt("uid", transaction.getUid());
        bundle.putString("kwota", transaction.getKwota());
        bundle.putString("kategoria", transaction.getKategoria());
        bundle.putSerializable("data", transaction.getData());
        bundle.putString("opis", transaction.getOpis());
        return bundle;
    }

    private void showFilterMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), anchor);
        popupMenu.getMenuInflater().inflate(R.menu.menu_filtra, popupMenu.getMenu());

        Map<Integer, String> filterMapping = getFilterMapping();
        popupMenu.setOnMenuItemClickListener(item -> applyFilterFromMenu(filterMapping, item.getItemId()));
        popupMenu.show();
    }

    private Map<Integer, String> getFilterMapping() {
        Map<Integer, String> filterMapping = new HashMap<>();
        filterMapping.put(R.id.filter_oldest, "oldest");
        filterMapping.put(R.id.filter_newest, "newest");
        filterMapping.put(R.id.filter_income, "income");
        filterMapping.put(R.id.filter_expenses, "expenses");
        filterMapping.put(R.id.filter_highest, "highest");
        filterMapping.put(R.id.filter_lowest, "lowest");
        return filterMapping;
    }

    private boolean applyFilterFromMenu(Map<Integer, String> filterMapping, int itemId) {
        String filterType = filterMapping.get(itemId);
        if (filterType != null) {
            applyFilter(filterType);
            return true;
        }
        return false;
    }

    private void applyFilter(String filterType) {
        List<TransakcjaEntity> filteredTransactions = getFilteredTransactions(filterType);
        updateTransactionListView(filteredTransactions);
    }

    private List<TransakcjaEntity> getFilteredTransactions(String filterType) {
        TransakcjaDAO transakcjaDAO = getTransakcjaDao();
        switch (filterType) {
            case "oldest":
                return transakcjaDAO.getOldest();
            case "newest":
                return transakcjaDAO.getNewest();
            case "income":
                return transakcjaDAO.getByType(true);
            case "expenses":
                return transakcjaDAO.getByType(false);
            case "highest":
                return transakcjaDAO.getHighest();
            case "lowest":
                return transakcjaDAO.getLowest();
            default:
                return transakcjaDAO.getAllSortedByDate();
        }
    }

    private void updateTransactionListView(List<TransakcjaEntity> transactions) {
        HistoriaTransakcjiAdapter adapter = new HistoriaTransakcjiAdapter(requireContext(), transactions);
        transactionListView.setAdapter(adapter);
    }

    private TransakcjaDAO getTransakcjaDao() {
        MainActivity activity = (MainActivity) getActivity();
        BazaDanych db = activity.getDb();
        return db.transakcjaDAO();
    }
}
