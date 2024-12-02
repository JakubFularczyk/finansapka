package com.example.androidproject.ustawienia;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.androidproject.R;
import com.example.androidproject.adaptery.LimityKategoriiAdapter;
import com.example.androidproject.baza.BazaDanych;
import com.example.androidproject.stronaglowna.MainActivity;
import com.example.androidproject.transakcje.dao.KategoriaDAO;
import com.example.androidproject.transakcje.encje.KategoriaEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoriaLimitowKategoriiFragment extends Fragment {

    private ListView limitCategoryListView;
    private List<KategoriaEntity> categories;
    private LimityKategoriiAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historia_limitow_kategorii, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFragment(view);
    }

    private void initFragment(View view) {
        initCategoriesAndLimits();
        setupListView(view);
        setupButtons(view);
    }

    private void initCategoriesAndLimits() {
        MainActivity mainActivity = (MainActivity) requireActivity();
        categories = mainActivity.getCategories();

    }

    private void setupListView(View view) {
        limitCategoryListView = view.findViewById(R.id.limitCategoryListView);
        adapter = new LimityKategoriiAdapter(requireContext(), categories);
        limitCategoryListView.setAdapter(adapter);
    }

    private void setupButtons(View view) {
        setupReturnButton(view);
        setupAddCategoryButton(view);
    }

    private void setupReturnButton(View view) {
        Button powrotButton = view.findViewById(R.id.powrotButton);
        powrotButton.setOnClickListener(v -> navigateToSettings(v));
    }

    private void setupAddCategoryButton(View view) {
        Button limityKategoriiButton = view.findViewById(R.id.limityKategoriiButton);
        MainActivity mainActivity = (MainActivity) requireActivity();
        limityKategoriiButton.setOnClickListener(v -> showAddCategoryPopup(mainActivity));
    }

    private void navigateToSettings(View view) {
        NavController navController = Navigation.findNavController(view);
        navController.navigate(R.id.action_historiaLimitowKategoriiFragment_to_ustawieniaFragment);
    }

    private void showAddCategoryPopup(MainActivity mainActivity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Dodaj nową kategorię");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_dodaj_kategorie, null);
        builder.setView(dialogView);

        EditText categoryInput = dialogView.findViewById(R.id.kategoriaInput);

        builder.setPositiveButton("Dodaj", (dialog, which) -> addNewCategory(mainActivity, categoryInput.getText().toString().trim()));
        builder.setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void addNewCategory(MainActivity mainActivity, String newCategory) {
        if (!newCategory.isEmpty()) {
            mainActivity.addCategory(newCategory);
            adapter.notifyDataSetChanged();
            Toast.makeText(requireContext(), "Dodano kategorię: " + newCategory, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Kategoria nie może być pusta!", Toast.LENGTH_SHORT).show();
        }
    }






}
