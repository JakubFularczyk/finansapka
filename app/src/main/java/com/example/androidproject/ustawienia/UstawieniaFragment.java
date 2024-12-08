package com.example.androidproject.ustawienia;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.androidproject.R;
import com.example.androidproject.baza.BazaDanych;
import com.example.androidproject.stronaglowna.MainActivity;
import com.example.androidproject.transakcje.dao.KategoriaDAO;
import com.example.androidproject.transakcje.dao.TransakcjaCyklicznaDAO;
import com.example.androidproject.transakcje.dao.TransakcjaDAO;
import com.example.androidproject.transakcje.dao.UserDAO;
import com.example.androidproject.transakcje.encje.UserEntity;
import com.example.androidproject.utils.ExportImportManager;

public class UstawieniaFragment extends Fragment {

    private ActivityResultLauncher<Intent> importLauncher;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ustawienia, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        importLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            // Wywołanie importu danych z poprawnym przekazaniem URI
                            ExportImportManager.importDatabaseFromJSON(requireContext(), uri);
                        } else {
                            Toast.makeText(requireContext(), "Nie wybrano pliku", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);


        Button exportButton = view.findViewById(R.id.eksportBazyButton);
        Button importButton = view.findViewById(R.id.importBazyDanychButton);

        exportButton.setOnClickListener(v -> {
            ExportImportManager.exportDatabaseToJSON(requireContext());
        });

        importButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("application/json"); // Ustaw typ MIME na JSON
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            // Ustaw domyślny katalog na "Documents"
            intent.putExtra("android.provider.extra.INITIAL_URI", MediaStore.Files.getContentUri("external"));

            importLauncher.launch(intent);
        });
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

        builder.setPositiveButton("Zmień", (dialog, which) -> {
            String newPin = input.getText().toString();
            validateAndSavePin(newPin);
        });
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
        // Pobierz bazę danych i DAO z MainActivity
        MainActivity mainActivity = (MainActivity) getActivity();
        BazaDanych db = mainActivity.getDb();
        UserDAO userDAO = db.userDAO();

        int userId = 1; // Powiązany z zalogowanym użytkownikiem

        // Zaktualizuj PIN w bazie danych
        userDAO.updateUserPin(userId, newPin);

        // Powiadom użytkownika
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
        MainActivity mainActivity = (MainActivity) getActivity();
        BazaDanych db = mainActivity.getDb();
        UserDAO userDAO = db.userDAO();
        TransakcjaDAO transakcjaDAO = db.transakcjaDAO();
        TransakcjaCyklicznaDAO transakcjaCyklicznaDAO = db.transakcjaCyklicznaDAO();
        KategoriaDAO kategoriaDAO = db.kategoriaDAO();

        try {
            UserEntity user = userDAO.findFirstUser();
            int userId = user.getId();

            // Usuń dane powiązane z użytkownikiem
            transakcjaDAO.clearTable();
            transakcjaCyklicznaDAO.clearTable();
            kategoriaDAO.clearTable();

            // Usuń użytkownika
            userDAO.deleteUserById(userId);



            // Przekierowanie do ekranu rejestracji
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_ustawieniaFragment_to_rejestracjaFragment);
        } catch (IllegalStateException e) {
            Toast.makeText(requireContext(), "Nie udało się usunąć konta: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
