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
    private Button showAllButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize existing buttons
        Button przychodyButton = view.findViewById(R.id.dodajTransakcjeButton);
        przychodyButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_mainFragment_to_przychodyFragment);
        });

        Button ustawieniaButton = view.findViewById(R.id.ustawieniaButton);
        ustawieniaButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_mainFragment_to_ustawieniaFragment);
        });


        // Initialize new UI components
        welcomeTextView = view.findViewById(R.id.welcomeTextView);
        balanceTextView = view.findViewById(R.id.balanceTextView);
        transactionListView = view.findViewById(R.id.transactionListView);
        noTransactionsTextView = view.findViewById(R.id.noTransactionsTextView);
        showAllButton = view.findViewById(R.id.showAllButton);

        // Set welcome message and balance
        String username = "Jan"; // Replace with dynamic data if available
        welcomeTextView.setText("Witaj, " + username);

        double balance = -167.43; // Replace with dynamic data
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

        // Set "Wyświetl wszystko" button listener
        showAllButton.setOnClickListener(v -> {
            // Logic to navigate to transaction history screen (to be implemented)
        });
    }

    private List<String> getTransactions() {
        // Replace with actual logic to fetch transactions
        return new ArrayList<>();
    }
}
