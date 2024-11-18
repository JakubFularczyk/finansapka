package com.example.androidproject.stronaglowna;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.androidproject.R;

import java.util.ArrayList;
import java.util.List;


public class MainFragment extends Fragment {

    public MainFragment() {
        // Required empty public constructor
    }

    private TextView welcomeTextView;
    private TextView balanceTextView;
    private ListView transactionListView;
    private TextView noTransactionsTextView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        przejdzDoDodaniaTransakcji(view, R.id.action_mainFragment_to_dodajTransakcjeFragment);
        przejdzDoUstawien(view, R.id.action_mainFragment_to_ustawieniaFragment);
        przejdzDoHistorii(view, R.id.action_mainFragment_to_historiaTransakcjiFragment);
        przejdzDoAnalizy(view, R.id.action_mainFragment_to_analizaFinansowFragment);



        welcomeTextView = view.findViewById(R.id.welcomeTextView);
        balanceTextView = view.findViewById(R.id.balanceTextView);
        transactionListView = view.findViewById(R.id.transactionListView);
        noTransactionsTextView = view.findViewById(R.id.noTransactionsTextView);


        String username = "Jan";
        welcomeTextView.setText("Witaj, " + username);

        double balance = 2222.43;
        balanceTextView.setText(String.format("%.2f PLN", balance));
        balanceTextView.setTextColor(balance < 0 ? getResources().getColor(android.R.color.holo_red_dark) : getResources().getColor(android.R.color.holo_green_dark));

        // Handle transactions
        List<String> transactions = getTransactions(); // Replace with dynamic data retrieval
        if (transactions.isEmpty()) {
            noTransactionsTextView.setVisibility(View.VISIBLE);
            transactionListView.setVisibility(View.GONE);
        } else {
            noTransactionsTextView.setVisibility(View.GONE);
            transactionListView.setVisibility(View.VISIBLE);
            // TODO: Populate the ListView with transactions (adapter logic to be added)
        }


    }

    private void przejdzDoAnalizy(View view, int action_mainFragment_to_analizaFinansowFragment) {
        Button analizaButton = view.findViewById(R.id.analizaButton);
        analizaButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(action_mainFragment_to_analizaFinansowFragment);
        });
    }

    private static void przejdzDoHistorii(@NonNull View view, int action_mainFragment_to_historiaTransakcjiFragment) {
        Button wyswietlWszystkoButton = view.findViewById(R.id.wyswietlWszystkoButton);
        wyswietlWszystkoButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(action_mainFragment_to_historiaTransakcjiFragment);
        });
    }
    private static void przejdzDoDodaniaTransakcji(@NonNull View view, int action_mainFragment_to_przychodyFragment) {
        Button dodajTransakcje = view.findViewById(R.id.dodajTransakcjeButton);
        dodajTransakcje.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(action_mainFragment_to_przychodyFragment);
        });
    }
    private static void przejdzDoUstawien(@NonNull View view, int action_mainFragment_to_ustawieniaFragment) {
        Button ustawienia = view.findViewById(R.id.ustawieniaButton);
        ustawienia.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(action_mainFragment_to_ustawieniaFragment);
        });
    }



    private List<String> getTransactions() {
        // Replace with actual logic to fetch transactions
        return new ArrayList<>();
    }
}
