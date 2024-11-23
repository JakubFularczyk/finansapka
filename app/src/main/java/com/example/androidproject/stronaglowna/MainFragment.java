package com.example.androidproject.stronaglowna;

import android.database.DataSetObserver;
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
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidproject.R;
import com.example.androidproject.baza.BazaDanych;
import com.example.androidproject.transakcje.dao.TransakcjaDAO;
import com.example.androidproject.transakcje.encje.TransakcjaEntity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class MainFragment extends Fragment {

    public MainFragment() {
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
        zaladujDaneHistoryczne(view);
        ImageButton notificationButton = view.findViewById(R.id.notificationButton);
        notificationButton.setOnClickListener(v -> pokazPowiadomienia());

        welcomeTextView = view.findViewById(R.id.welcomeTextView);
        balanceTextView = view.findViewById(R.id.balanceTextView);



        String username = "Jan";
        welcomeTextView.setText("Witaj, " + username);

        double balance = 2222.43;
        balanceTextView.setText(String.format("%.2f PLN", balance));
        balanceTextView.setTextColor(balance < 0 ? getResources().getColor(android.R.color.holo_red_dark) : getResources().getColor(android.R.color.holo_green_dark));

    }

    private void pokazPowiadomienia() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.powiadomienia, null);

        TextView limitNotificationTextView = bottomSheetView.findViewById(R.id.limitNotificationTextView);
        TextView subscriptionNotificationTextView = bottomSheetView.findViewById(R.id.subscriptionNotificationTextView);

        limitNotificationTextView.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Przejdź do limitów kategorii", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();

        });

        subscriptionNotificationTextView.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Sprawdź płatności cykliczne", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();

        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
    private void pokazKropkeGdyPowiadomienia(boolean hasNotifications) {
        View notificationDot = getView().findViewById(R.id.notificationBadge); // Czerwone kółko
        if (hasNotifications) {
            notificationDot.setVisibility(View.VISIBLE);
        } else {
            notificationDot.setVisibility(View.GONE);
        }
    }

    private void przejdzDoAnalizy(View view, int action_mainFragment_to_analizaFinansowFragment) {
        Button analizaButton = view.findViewById(R.id.analizaButton);
        analizaButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(action_mainFragment_to_analizaFinansowFragment);
        });
    }

    private void przejdzDoHistorii(@NonNull View view, int action_mainFragment_to_historiaTransakcjiFragment) {
        Button wyswietlWszystkoButton = view.findViewById(R.id.wyswietlWszystkoButton);
        wyswietlWszystkoButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(action_mainFragment_to_historiaTransakcjiFragment);
        });
    }

    private void przejdzDoDodaniaTransakcji(@NonNull View view, int action_mainFragment_to_przychodyFragment) {
        Button dodajTransakcje = view.findViewById(R.id.dodajTransakcjeButton);
        dodajTransakcje.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(action_mainFragment_to_przychodyFragment);
        });
    }

    private void przejdzDoUstawien(@NonNull View view, int action_mainFragment_to_ustawieniaFragment) {
        Button ustawienia = view.findViewById(R.id.ustawieniaButton);
        ustawienia.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(action_mainFragment_to_ustawieniaFragment);
        });
    }

    private void zaladujDaneHistoryczne(@NonNull View view) {
        ListView transactionListView = view.findViewById(R.id.transactionListView);

        MainActivity mainActivity = (MainActivity) getActivity();
        BazaDanych db = mainActivity.getDb();
        TransakcjaDAO transakcjaDAO = db.transakcjaDAO();
        List<TransakcjaEntity> najnowszeTransakcje = transakcjaDAO.getLatest3();
        List<String> wpisyHistoryczne = najnowszeTransakcje.stream()
                .map(t -> t.getData() + " " + t.getOpis() + " " + t.getKwota())
                .collect(Collectors.toList());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, wpisyHistoryczne);
        transactionListView.setAdapter(adapter);

        pokazWpisyHistoryczne(view, !wpisyHistoryczne.isEmpty());
    }

    private void pokazWpisyHistoryczne(View view, boolean widoczne) {
        transactionListView = view.findViewById(R.id.transactionListView);
        noTransactionsTextView = view.findViewById(R.id.noTransactionsTextView);

        if (widoczne) {
            noTransactionsTextView.setVisibility(View.GONE);
            transactionListView.setVisibility(View.VISIBLE);
        } else {
            noTransactionsTextView.setVisibility(View.VISIBLE);
            transactionListView.setVisibility(View.GONE);
        }
    }
}
