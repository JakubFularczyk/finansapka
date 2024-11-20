package com.example.androidproject.transakcje;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.androidproject.R;

public class DodanaTransakcjaFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transakcja_dodana, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Button powrotDoMenuButton = view.findViewById(R.id.powrotDoMenuButton);
        Button dodajKolejnaTransakcjeButton = view.findViewById(R.id.dodajKolejnaTransakcjeButton);
        Button przejdzDoHistoriiButton = view.findViewById(R.id.przejdzDoHistoriiButton);


        setupPowrotDoMenuButton(powrotDoMenuButton);
        setupDodajKolejnaTransakcjeButton(dodajKolejnaTransakcjeButton);
        setupPrzejdzDoHistoriiButton(przejdzDoHistoriiButton);
    }



    private void setupPowrotDoMenuButton(Button powrotDoMenuButton) {
        powrotDoMenuButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_transakcjaDodanaFragment_to_mainFragment);
        });
    }

    private void setupDodajKolejnaTransakcjeButton(Button dodajKolejnaTransakcjeButton) {
        dodajKolejnaTransakcjeButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_transakcjaDodanaFragment_to_dodajTransakcjeFragment);
        });
    }

    private void setupPrzejdzDoHistoriiButton(Button przejdzDoHistoriiButton) {
        przejdzDoHistoriiButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_transakcjaDodanaFragment_to_historiaTransakcjiFragment);
        });
    }
}