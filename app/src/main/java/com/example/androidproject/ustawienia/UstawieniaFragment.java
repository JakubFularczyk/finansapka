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


public class UstawieniaFragment extends Fragment {

    public UstawieniaFragment() {
        // Required empty public constructor
    }

    public static UstawieniaFragment newInstance() {
        return new UstawieniaFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ustawienia, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button limitKategorii = view.findViewById(R.id.limitKategoriiButton);
        limitKategorii.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_ustawieniaFragment_to_limitKategoriiFragment);
        });

    }
}