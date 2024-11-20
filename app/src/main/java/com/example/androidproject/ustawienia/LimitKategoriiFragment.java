package com.example.androidproject.ustawienia;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.example.androidproject.R;
import com.example.androidproject.stronaglowna.MainActivity;

import java.util.ArrayList;
import java.util.List;


public class LimitKategoriiFragment extends Fragment {


    public static LimitKategoriiFragment newInstance() {
        return new LimitKategoriiFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_limit_kategorii, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        MainActivity mainActivity = (MainActivity) requireActivity();
        List<String> categories = mainActivity.getCategories();

        AutoCompleteTextView kategoriaDropdown = view.findViewById(R.id.kategoriaDropdown);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        kategoriaDropdown.setAdapter(adapter);


        Button dodajKategorieButton = view.findViewById(R.id.dodajKategorieButton);
        dodajKategorieButton.setVisibility(View.GONE);

        dodajKategorieButton.setOnClickListener(v -> {
            String nowaKategoria = kategoriaDropdown.getText().toString();
            if (!nowaKategoria.isEmpty() && !categories.contains(nowaKategoria)) {

                mainActivity.addCategory(nowaKategoria);


                adapter.notifyDataSetChanged();


                kategoriaDropdown.setText("");
                dodajKategorieButton.setVisibility(View.GONE);
            }
        });

        // Pokaż przycisk, jeśli użytkownik wpisuje nową kategorię
        kategoriaDropdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                dodajKategorieButton.setVisibility(View.VISIBLE);
            }
        });

        // Ukryj przycisk po opuszczeniu pola tekstowego, jeśli nic nie wpisano
        kategoriaDropdown.setOnDismissListener(() -> {
            if (kategoriaDropdown.getText().toString().isEmpty()) {
                dodajKategorieButton.setVisibility(View.GONE);
            }
        });
    }
}