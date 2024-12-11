package com.example.androidproject.autoryzacja;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.androidproject.R;
import com.example.androidproject.baza.dao.UserDAO;
import com.example.androidproject.baza.encje.UserEntity;
import com.example.androidproject.stronaglowna.MainActivity;

public class RejestracjaFragment extends Fragment {

    private EditText nameInput;
    private EditText pinInput;
    private CheckBox securityQuestionCheckBox;
    private Spinner securityQuestionSpinner;
    private EditText securityAnswerInput;


    private UserDAO userDAO;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rejestracja, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicjalizacja widoków
        nameInput = view.findViewById(R.id.nameInput);
        pinInput = view.findViewById(R.id.pinInput);
        securityQuestionCheckBox = view.findViewById(R.id.securityQuestionCheckBox);
        securityQuestionSpinner = view.findViewById(R.id.securityQuestionSpinner);
        securityAnswerInput = view.findViewById(R.id.securityAnswerInput);
        view.findViewById(R.id.registerButton).setOnClickListener(v -> handleRegistration(view));

        // Ukrywanie kontenera pytania bezpieczeństwa
        View securityQuestionContainer = view.findViewById(R.id.securityQuestionContainer);
        securityQuestionCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            securityQuestionContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Inicjalizacja DAO
        userDAO = ((MainActivity) requireActivity()).getDb().userDAO();

        // Obsługa przycisku "Zarejestruj się"
        view.findViewById(R.id.registerButton).setOnClickListener(v -> handleRegistration(view));
    }

    private void handleRegistration(View view) {
        String name = nameInput.getText().toString().trim();
        String pin = pinInput.getText().toString().trim();


        // Walidacja wymaganych pól
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(requireContext(), "Podaj imię!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(pin) || pin.length() != 4) {
            Toast.makeText(requireContext(), "PIN musi mieć 4 cyfry!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tworzenie użytkownika
        UserEntity user = new UserEntity();
        user.setName(name);
        user.setPin(pin);


        // Obsługa pytania bezpieczeństwa (opcjonalne)
        if (securityQuestionCheckBox.isChecked()) {
            String question = securityQuestionSpinner.getSelectedItem().toString();
            String answer = securityAnswerInput.getText().toString().trim();

            if (TextUtils.isEmpty(answer)) {
                Toast.makeText(requireContext(), "Podaj odpowiedź na pytanie bezpieczeństwa!", Toast.LENGTH_SHORT).show();
                return;
            }

            user.setSecurityQuestion(question);
            user.setSecurityAnswer(answer);
        }

        // Zapis do bazy
        userDAO.insert(user);
        Toast.makeText(requireContext(), "Zarejestrowano pomyślnie!", Toast.LENGTH_SHORT).show();

        // Przejście do ekranu głównego
        NavController navController = Navigation.findNavController(view);
        navController.navigate(R.id.action_rejestracjaFragment_to_mainFragment);
    }
}
