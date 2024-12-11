package com.example.androidproject.autoryzacja;

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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidproject.R;
import com.example.androidproject.baza.BazaDanych;
import com.example.androidproject.stronaglowna.MainActivity;
import com.example.androidproject.baza.dao.KategoriaDAO;
import com.example.androidproject.baza.dao.TransakcjaCyklicznaDAO;
import com.example.androidproject.baza.dao.TransakcjaDAO;
import com.example.androidproject.baza.dao.UserDAO;
import com.example.androidproject.baza.encje.UserEntity;


public class ZapomnianeHasloFragment extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_zapomniane_haslo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Znajdź widoki w layoutcie
        TextView noSecurityQuestionText = view.findViewById(R.id.noSecurityQuestionText);
        TextView securityQuestionText = view.findViewById(R.id.securityQuestionText);
        EditText securityAnswerInput = view.findViewById(R.id.securityAnswerInput);
        FrameLayout recoverButtonContainer = view.findViewById(R.id.recoverButtonContainer);
        Button deleteAccountButton = view.findViewById(R.id.deleteAccountButton);

        // Pobierz instancję DAO i użytkownika
        UserDAO userDAO = ((MainActivity) requireActivity()).getDb().userDAO();
        UserEntity user = userDAO.findFirstUser(); // Pobierz pierwszego użytkownika z bazy danych

        if (user != null && user.getSecurityQuestion() != null && !user.getSecurityQuestion().isEmpty()) {
            // Jeśli pytanie pomocnicze istnieje
            noSecurityQuestionText.setVisibility(View.GONE); // Ukryj komunikat
            securityQuestionText.setVisibility(View.VISIBLE); // Pokaż pytanie
            securityQuestionText.setText(user.getSecurityQuestion());
            securityAnswerInput.setVisibility(View.VISIBLE); // Pokaż pole na odpowiedź
            recoverButtonContainer.setVisibility(View.VISIBLE); // Pokaż przycisk odzyskania konta
        } else {
            // Jeśli pytania pomocniczego brak
            noSecurityQuestionText.setVisibility(View.VISIBLE); // Pokaż komunikat
            securityQuestionText.setVisibility(View.GONE); // Ukryj pytanie
            securityAnswerInput.setVisibility(View.GONE); // Ukryj pole odpowiedzi
            recoverButtonContainer.setVisibility(View.GONE); // Ukryj przycisk odzyskania konta
        }

        // Obsługa przycisku "Odzyskaj konto"
        recoverButtonContainer.setOnClickListener(v -> {
            if (user != null) {
                String enteredAnswer = securityAnswerInput.getText().toString().trim().toLowerCase(); // Pomija wielkość liter
                String storedAnswer = user.getSecurityAnswer().toLowerCase(); // Pomija wielkość liter

                if (enteredAnswer.equals(storedAnswer)) {
                    // Przekierowanie do zmiany PIN-u w ustawieniach
                    Toast.makeText(requireContext(), "Poprawna odpowiedź! Przechodzę do ustawień.", Toast.LENGTH_SHORT).show();
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_zapomnianeHasloFragment_to_ustawieniaFragment);
                } else {
                    // Błędna odpowiedź
                    Toast.makeText(requireContext(), "Błędna odpowiedź na pytanie pomocnicze.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Obsługa przycisku "Usuń konto"
        deleteAccountButton.setOnClickListener(v -> deleteAccount());


    }

    private void deleteAccount() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) {
            Toast.makeText(requireContext(), "Błąd: Nie udało się uzyskać instancji MainActivity.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pobranie bazy danych
        BazaDanych db = mainActivity.getDb();
        UserDAO userDAO = db.userDAO();
        TransakcjaDAO transakcjaDAO = db.transakcjaDAO();
        TransakcjaCyklicznaDAO transakcjaCyklicznaDAO = db.transakcjaCyklicznaDAO();
        KategoriaDAO kategoriaDAO = db.kategoriaDAO();

        try {
            UserEntity user = userDAO.findFirstUser();
            if (user == null) {
                Toast.makeText(requireContext(), "Błąd: Nie znaleziono użytkownika do usunięcia.", Toast.LENGTH_SHORT).show();
                return;
            }

            int userId = user.getId();

            // Usunięcie danych powiązanych z użytkownikiem
            transakcjaDAO.clearTable();
            transakcjaCyklicznaDAO.clearTable();
            kategoriaDAO.clearTable();

            // Usunięcie użytkownika
            userDAO.deleteUserById(userId);

            Toast.makeText(requireContext(), "Konto zostało pomyślnie usunięte.", Toast.LENGTH_SHORT).show();

            // Przekierowanie do ekranu rejestracji
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_zapomnianeHasloFragment_to_rejestracjaFragment);

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Błąd podczas usuwania konta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}