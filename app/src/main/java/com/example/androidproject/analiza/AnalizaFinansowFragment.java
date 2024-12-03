package com.example.androidproject.analiza;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.androidproject.R;
import com.example.androidproject.adaptery.StatystykiAdapter;
import com.example.androidproject.stronaglowna.MainActivity;
import com.example.androidproject.transakcje.dao.KategoriaDAO;
import com.example.androidproject.transakcje.dao.TransakcjaDAO;
import com.example.androidproject.transakcje.encje.TransakcjaEntity;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnalizaFinansowFragment extends Fragment {

    private ListView statsListView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analiza_finansow, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.powrotDoMenuButton).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_analizaFinansowFragment_to_mainFragment);
        });

        TextView emptyStateText = view.findViewById(R.id.dodajTransakcjeText);
        ListView statsListView = view.findViewById(R.id.statsListView);

        List<Statystyka> statystyki = pobierzStatystyki();

        if (statystyki.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            statsListView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            statsListView.setVisibility(View.VISIBLE);

            StatystykiAdapter adapter = new StatystykiAdapter(requireContext(), statystyki);
            statsListView.setAdapter(adapter);
        }
    }

    private List<Statystyka> pobierzStatystyki() {
        List<Statystyka> statystyki = new ArrayList<>();
        TransakcjaDAO transakcjaDAO = ((MainActivity) getActivity()).getDb().transakcjaDAO();
        KategoriaDAO kategoriaDAO = ((MainActivity) getActivity()).getDb().kategoriaDAO();

        // Najczęściej używana kategoria
        String najczestszaKategoria = kategoriaDAO.getMostFrequentCategory();
        if (najczestszaKategoria != null && !najczestszaKategoria.isEmpty()) {
            statystyki.add(new Statystyka(
                    "Najczęściej dodawana kategoria",
                    najczestszaKategoria
            ));
        }

        // Rekordowa transakcja
        TransakcjaEntity rekord = transakcjaDAO.getHighestTransaction();
        if (rekord != null) {
            // Formatowanie kwoty
            double kwota = Double.parseDouble(rekord.getKwota());
            String sformatowanaKwota = (kwota % 1 == 0) ? // Jeśli brak części dziesiętnej
                    String.valueOf((int) kwota) : String.format(Locale.getDefault(), "%.2f", kwota);

            // Formatowanie daty
            Date dataRekordu = rekord.getData();
            String[] miesiaceMianownik = {
                    "Stycznia", "Lutego", "Marca", "Kwietnia", "Maja", "Czerwca",
                    "Lipca", "Sierpnia", "Września", "Października", "Listopada", "Grudnia"
            };
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dataRekordu);
            int dzien = calendar.get(Calendar.DAY_OF_MONTH);
            int miesiac = calendar.get(Calendar.MONTH); // 0-based
            String nazwaMiesiaca = miesiaceMianownik[miesiac];

            // Dodanie do statystyk
            statystyki.add(new Statystyka(
                    "Rekordowa kwota transakcji",
                    sformatowanaKwota + " PLN dnia " + dzien + " " + nazwaMiesiaca
            ));
        }

        // Miesiąc z największą liczbą transakcji
        String najczestszyMiesiac = transakcjaDAO.getMonthWithMostTransactions();
        if (najczestszyMiesiac != null && !najczestszyMiesiac.isEmpty()) {
            try {
                String[] parts = najczestszyMiesiac.split("-");
                if (parts.length == 2) {
                    int rok = Integer.parseInt(parts[0]);
                    int miesiac = Integer.parseInt(parts[1]);

                    if (miesiac >= 1 && miesiac <= 12) {
                        // Tablica miesięcy w mianowniku
                        String[] miesiaceMianownik = {
                                "Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec",
                                "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień"
                        };

                        // Pobranie nazwy miesiąca na podstawie indeksu
                        String nazwaMiesiaca = miesiaceMianownik[miesiac - 1];

                        statystyki.add(new Statystyka(
                                "Miesiąc z największą liczbą transakcji",
                                nazwaMiesiaca + " " + rok
                        ));
                    }
                }
            } catch (Exception e) {
                // Ignorowanie błędów
            }
        }

        // Najczęściej używana transakcja cykliczna
        String najczestszaCykliczna = transakcjaDAO.getMostFrequentRecurringTransaction();
        if (najczestszaCykliczna != null) {
            statystyki.add(new Statystyka("Najczęstsza transakcja cykliczna", najczestszaCykliczna));
        }

        return statystyki;
    }

}