package com.example.androidproject.ustawienia;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

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


        Button usunKontoButton = view.findViewById(R.id.usunKontoButton);
        usunKontoButton.setOnClickListener(v -> pokazPopupUsunKonto());
        Button zmianaKoduPinButton = view.findViewById(R.id.zmianaKoduPinButton);
        zmianaKoduPinButton.setOnClickListener(v -> pokazPopupZmianaPinu());

        Button limitKategorii = view.findViewById(R.id.limityKategoriiButton);
        limitKategorii.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_ustawieniaFragment_to_historiaLimitowKategoriiFragment);
        });

    }

    private void pokazPopupZmianaPinu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Zmień kod PIN");


        EditText input = new EditText(requireContext());
        input.setHint("Wprowadź nowy PIN");
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(input);


        builder.setPositiveButton("Zmień", (dialog, which) -> {
            String nowyPin = input.getText().toString();
            if (nowyPin.isEmpty() || nowyPin.length() < 4) {

                AlertDialog.Builder errorBuilder = new AlertDialog.Builder(requireContext());
                errorBuilder.setTitle("Błąd");
                errorBuilder.setMessage("PIN musi składać się z co najmniej 4 cyfr!");
                errorBuilder.setPositiveButton("OK", (errorDialog, w) -> errorDialog.dismiss());
                errorBuilder.show();
            } else {

                zapiszNowyPin(nowyPin);
            }
        });

        builder.setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss());


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void zapiszNowyPin(String nowyPin) {
        AlertDialog.Builder successBuilder = new AlertDialog.Builder(requireContext());
        successBuilder.setTitle("Sukces");
        successBuilder.setMessage("PIN został zmieniony pomyślnie!");
        successBuilder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        successBuilder.show();
    }

    private void pokazPopupUsunKonto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Usuń Konto");
        builder.setMessage("Czy na pewno chcesz usunąć konto?");


        builder.setPositiveButton("Tak", (dialog, which) -> {

            dialog.dismiss();
        });


        builder.setNegativeButton("Nie", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}