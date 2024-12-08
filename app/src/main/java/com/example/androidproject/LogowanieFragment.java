package com.example.androidproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.androidproject.R;
import com.example.androidproject.baza.BazaDanych;
import com.example.androidproject.stronaglowna.MainActivity;
import com.example.androidproject.transakcje.dao.UserDAO;
import com.example.androidproject.transakcje.encje.UserEntity;

public class LogowanieFragment extends Fragment {

    private EditText pinInput;
    private Button loginButton;
    private BazaDanych db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_logowanie, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicjalizacja widoków
        pinInput = view.findViewById(R.id.pinInput);
        loginButton = view.findViewById(R.id.loginButton);
        TextView forgottenPassword = view.findViewById(R.id.forgottenPassword);

        // Pobranie instancji bazy danych
        db = ((MainActivity) requireActivity()).getDb();

        // Obsługa przycisku logowania
        loginButton.setOnClickListener(v -> handleLogin());
        forgottenPassword.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_logowanieFragment_to_zapomnianeHasloFragment);
        });
    }

    private void handleLogin() {
        if (pinInput == null || db == null) {
            Toast.makeText(getContext(), "Błąd inicjalizacji", Toast.LENGTH_SHORT).show();
            return;
        }

        String pin = pinInput.getText().toString().trim();

        if (pin.isEmpty()) {
            Toast.makeText(getContext(), "Wprowadź PIN!", Toast.LENGTH_SHORT).show();
            return;
        }

        UserDAO userDAO = db.userDAO();
        UserEntity user = userDAO.findByPin(pin);

        if (user == null) {
            Toast.makeText(getContext(), "Niepoprawny PIN", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Zalogowano jako: " + user.getName(), Toast.LENGTH_SHORT).show();
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_logowanieFragment_to_mainFragment);
        }
    }
}
