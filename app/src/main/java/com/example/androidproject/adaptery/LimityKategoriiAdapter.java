package com.example.androidproject.adaptery;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidproject.R;
import com.example.androidproject.baza.BazaDanych;
import com.example.androidproject.stronaglowna.MainActivity;
import com.example.androidproject.transakcje.dao.KategoriaDAO;
import com.example.androidproject.transakcje.dao.TransakcjaDAO;
import com.example.androidproject.transakcje.encje.KategoriaEntity;

import java.util.List;

public class LimityKategoriiAdapter extends BaseAdapter {

    private final Context context;
    private final List<KategoriaEntity> categories;

    public LimityKategoriiAdapter(Context context, List<KategoriaEntity> categories) {
        this.context = context;
        this.categories = categories;
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Object getItem(int position) {
        return categories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.lista_limitow_kategorii, parent, false);
        }

        TextView categoryNameText = convertView.findViewById(R.id.categoryNameText);
        TextView categoryAmountText = convertView.findViewById(R.id.categoryAmountText);
        TextView categoryLimitText = convertView.findViewById(R.id.categoryLimitText);
        TextView divider = convertView.findViewById(R.id.divider);

        KategoriaEntity categoryEntity = categories.get(position);
        String categoryName = categoryEntity.getNazwa();
        String limit = categoryEntity.getLimit();
        double aktualnaKwota = categoryEntity.getAktualnaKwota();

        categoryNameText.setText(categoryName);

        if (limit != null && !limit.isEmpty()) {
            categoryAmountText.setText(String.format("%.2f", Math.abs(aktualnaKwota)));
            categoryLimitText.setText(String.format("%.2f PLN", Double.parseDouble(limit)));
            divider.setText("/");

            if (Math.abs(aktualnaKwota) >= 0.85 * Double.parseDouble(limit)) {
                int redColor = context.getResources().getColor(android.R.color.holo_red_dark);

                categoryAmountText.setTextColor(redColor);
                categoryLimitText.setTextColor(redColor);
                divider.setTextColor(redColor);
            } else {
                int defaultColor = context.getResources().getColor(android.R.color.black);

                categoryAmountText.setTextColor(defaultColor);
                categoryLimitText.setTextColor(defaultColor);
                divider.setTextColor(defaultColor);
            }

            categoryAmountText.setVisibility(View.VISIBLE);
            categoryLimitText.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
        } else {
            categoryAmountText.setVisibility(View.GONE);
            categoryLimitText.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }



        convertView.setOnClickListener(v -> showSetLimitPopup(categoryEntity));

        // Obsługa długiego kliknięcia
        convertView.setOnLongClickListener(v -> {
            showPopupMenu(v, categoryEntity, position);
            return true;
        });
        return convertView;
    }

    private void showPopupMenu(View anchor, KategoriaEntity category, int position) {
        PopupMenu popupMenu = new PopupMenu(context, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.kategoria_popup_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete_category) {
                confirmDeleteCategory(category, position);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }
    private void showSetLimitPopup(KategoriaEntity categoryEntity) {
        String category = categoryEntity.getNazwa();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Zarządzaj limitem dla kategorii " + category);

        // Tworzenie widoku
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Podaj nowy limit w PLN");

        builder.setView(input);

        // Dodaj przycisk "Zapisz"
        builder.setPositiveButton("Zapisz", (dialog, which) -> {
            String limit = input.getText().toString().trim();
            if (!limit.isEmpty()) {
                saveCategoryLimit(categoryEntity, limit);
            } else {
                Toast.makeText(context, "Limit nie może być pusty!", Toast.LENGTH_SHORT).show();
            }
        });

        // Dodaj przycisk "Usuń limit"
        builder.setNeutralButton("Usuń limit", (dialog, which) -> {
            removeCategoryLimit(categoryEntity);
            Toast.makeText(context, "Limit usunięty!", Toast.LENGTH_SHORT).show();
        });

        // Dodaj przycisk "Anuluj"
        builder.setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }
    private void saveCategoryLimit(KategoriaEntity category, String limit) {
        category.setLimit(limit);
        save(category);
    }

    private void removeCategoryLimit(KategoriaEntity category) {
        category.setLimit(null);
        save(category);
    }
    private void save(KategoriaEntity category) {
        MainActivity mainActivity = (MainActivity) context;
        BazaDanych db = mainActivity.getDb();
        KategoriaDAO kategoriaDAO = db.kategoriaDAO();
        kategoriaDAO.update(category);
        notifyDataSetChanged();
    }



    private void confirmDeleteCategory(KategoriaEntity category, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Usuń kategorię")
                .setMessage("Czy na pewno chcesz usunąć tę kategorię? Powiązane transakcje również zostaną usunięte.")
                .setPositiveButton("Usuń", (dialog, which) -> deleteCategory(category, position))
                .setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void deleteCategory(KategoriaEntity category, int position) {
        BazaDanych db = ((MainActivity) context).getDb();
        KategoriaDAO kategoriaDAO = db.kategoriaDAO();
        TransakcjaDAO transakcjaDAO = db.transakcjaDAO();

        // Usuń powiązane transakcje
        transakcjaDAO.deleteByCategory(category.getNazwa());

        // Usuń kategorię z bazy danych
        kategoriaDAO.delete(category);

        // Usuń kategorię z listy w adapterze
        categories.remove(position);
        notifyDataSetChanged();

        Toast.makeText(context, "Kategoria i powiązane transakcje zostały usunięte", Toast.LENGTH_SHORT).show();
    }



}
