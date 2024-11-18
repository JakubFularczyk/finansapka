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
import android.widget.ImageButton;

import com.example.androidproject.R;


public class EdycjaTransakcjiFragment extends Fragment {

    public EdycjaTransakcjiFragment() {
    }

    public static EdycjaTransakcjiFragment newInstance() {
        return new EdycjaTransakcjiFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edycja_transakcji, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        powrocDoHistoriiTransakcjiZatwierdzenie(view, R.id.action_edycjaTransakcjiFragment_to_historiaTransakcjiFragment);
        powrocDoHistoriiTransakcjiAnulowanie(view, R.id.action_edycjaTransakcjiFragment_to_historiaTransakcjiFragment);
    }

        //TODO dopisanie logiki jak juz bede miec rekordy zeby przy tym klinieciu nie zapisywalo zmian
    private static void powrocDoHistoriiTransakcjiZatwierdzenie(@NonNull View view, int action_edycjaTransakcjiFragment_to_historiaTransakcjiFragment) {
        ImageButton powrocDoHistoriiTransakcjiZatwierdzenieButton = view.findViewById(R.id.potwierdzEdycjeTransakcjiButton);
        powrocDoHistoriiTransakcjiZatwierdzenieButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(action_edycjaTransakcjiFragment_to_historiaTransakcjiFragment);
        });
    }

    private static void powrocDoHistoriiTransakcjiAnulowanie(@NonNull View view, int action_dodajTransakcjeFragment_to_mainFragment) {
        ImageButton powrocDoHistoriiTransakcjiAnulowanieButton = view.findViewById(R.id.anulujEdycjeButton);
        powrocDoHistoriiTransakcjiAnulowanieButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(action_dodajTransakcjeFragment_to_mainFragment);
        });
    }
}