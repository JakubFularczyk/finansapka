package com.example.androidproject.ustawienia;

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

import com.example.androidproject.R;


public class HistoriaLimitowKategoriiFragment extends Fragment {

    public HistoriaLimitowKategoriiFragment() {
    }

    public static HistoriaLimitowKategoriiFragment newInstance() {
        return new HistoriaLimitowKategoriiFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historia_limitow_kategorii, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button limitKategorii = view.findViewById(R.id.limityKategoriiButton);
        limitKategorii.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_historiaLimitowKategoriiFragment_to_limitKategoriiFragment);
        });
    }
}