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
import android.widget.ImageButton;

import com.example.androidproject.R;

public class UstawieniaFragment extends Fragment {

    public UstawieniaFragment() {
        // Required empty public constructor
    }

    public static UstawieniaFragment newInstance() {
        return new UstawieniaFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ustawienia, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void initViews(View view) {
        setupDeleteAccountButton(view);
        setupChangePinButton(view);
        setupCategoryLimitButton(view);
        setupReturnToMenuButton(view);
    }

    private void setupDeleteAccountButton(View view) {
        Button usunKontoButton = view.findViewById(R.id.usunKontoButton);
        usunKontoButton.setOnClickListener(v -> showDeleteAccountPopup());
    }

    private void setupChangePinButton(View view) {
        Button zmianaKoduPinButton = view.findViewById(R.id.zmianaKoduPinButton);
        zmianaKoduPinButton.setOnClickListener(v -> showChangePinPopup());
    }

    private void setupCategoryLimitButton(View view) {
        Button limitKategoriiButton = view.findViewById(R.id.limityKategoriiButton);
        limitKategoriiButton.setOnClickListener(v -> navigateToCategoryLimits(view));
    }

    private void setupReturnToMenuButton(View view) {
        ImageButton przejdzDoMenuButton = view.findViewById(R.id.przejdzDoMenuButton);
        przejdzDoMenuButton.setOnClickListener(v -> navigateToMainMenu(view));
    }

    private void showChangePinPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Zmień kod PIN");

        EditText input = new EditText(requireContext());
        input.setHint("Wprowadź nowy PIN");
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Zmień", (dialog, which) -> validateAndSavePin(input.getText().toString()));
        builder.setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void validateAndSavePin(String newPin) {
        if (newPin.isEmpty() || newPin.length() < 4) {
            showErrorPopup("Błąd", "PIN musi składać się z co najmniej 4 cyfr!");
        } else {
            saveNewPin(newPin);
        }
    }

    private void saveNewPin(String newPin) {
        AlertDialog.Builder successBuilder = new AlertDialog.Builder(requireContext());
        successBuilder.setTitle("Sukces");
        successBuilder.setMessage("PIN został zmieniony pomyślnie!");
        successBuilder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        successBuilder.show();
    }

    private void showDeleteAccountPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Usuń Konto");
        builder.setMessage("Czy na pewno chcesz usunąć konto?");

        builder.setPositiveButton("Tak", (dialog, which) -> deleteAccount());
        builder.setNegativeButton("Nie", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void deleteAccount() {
        // Implementacja logiki usuwania konta
        // Na razie zamykamy dialog
    }

    private void showErrorPopup(String title, String message) {
        AlertDialog.Builder errorBuilder = new AlertDialog.Builder(requireContext());
        errorBuilder.setTitle(title);
        errorBuilder.setMessage(message);
        errorBuilder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        errorBuilder.show();
    }

    private void navigateToCategoryLimits(View view) {
        NavController navController = Navigation.findNavController(view);
        navController.navigate(R.id.action_ustawieniaFragment_to_historiaLimitowKategoriiFragment);
    }

    private void navigateToMainMenu(View view) {
        NavController navController = Navigation.findNavController(view);
        navController.navigate(R.id.action_ustawieniaFragment_to_mainFragment);
    }
}
