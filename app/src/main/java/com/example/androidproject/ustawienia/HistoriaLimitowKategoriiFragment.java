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
import com.example.androidproject.stronaglowna.MainActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoriaLimitowKategoriiFragment extends Fragment {

    private ListView limitCategoryListView;
    private List<String> categories;
    private Map<String, String> categoryLimits;
    private LimityKategoriiAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historia_limitow_kategorii, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        MainActivity mainActivity = (MainActivity) requireActivity();
        categories = mainActivity.getCategories();


        categoryLimits = new HashMap<>();


        limitCategoryListView = view.findViewById(R.id.limitCategoryListView);
        adapter = new LimityKategoriiAdapter(requireContext(), categories, categoryLimits);
        limitCategoryListView.setAdapter(adapter);


        limitCategoryListView.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedCategory = categories.get(position);
            showSetLimitPopup(selectedCategory);
        });


        Button powrotButton = view.findViewById(R.id.powrotButton);
        powrotButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_historiaLimitowKategoriiFragment_to_ustawieniaFragment);
        });

        Button limityKategoriiButton = view.findViewById(R.id.limityKategoriiButton);
        limityKategoriiButton.setOnClickListener(v -> showAddCategoryPopup(mainActivity));
    }

    private void showAddCategoryPopup(MainActivity mainActivity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Dodaj nową kategorię");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_dodaj_kategorie, null);
        builder.setView(dialogView);

        EditText categoryInput = dialogView.findViewById(R.id.kategoriaInput);

        builder.setPositiveButton("Dodaj", (dialog, which) -> {
            String newCategory = categoryInput.getText().toString().trim();

            if (!newCategory.isEmpty()) {
                mainActivity.addCategory(newCategory);
                adapter.notifyDataSetChanged();

                Toast.makeText(requireContext(), "Dodano kategorię: " + newCategory, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Kategoria nie może być pusta!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }


    private void showSetLimitPopup(String category) {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Ustaw limit dla kategorii " + category);


        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Podaj limit w PLN");
        builder.setView(input);


        builder.setPositiveButton("Zapisz", (dialog, which) -> {
            String limit = input.getText().toString();
            if (!limit.isEmpty()) {
                categoryLimits.put(category, limit);
                adapter.notifyDataSetChanged();
                Toast.makeText(requireContext(), "Limit ustawiony!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Limit nie może być pusty!", Toast.LENGTH_SHORT).show();
            }
        });


        builder.setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss());


        builder.create().show();
    }
}
