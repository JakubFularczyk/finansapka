package com.example.androidproject.adaptery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.androidproject.R;

import java.util.List;
import java.util.Map;

public class LimityKategoriiAdapter extends BaseAdapter {
    private final Context context;
    private final List<String> categories;
    private final Map<String, String> categoryLimits; // Map for category limits

    public LimityKategoriiAdapter(Context context, List<String> categories, Map<String, String> categoryLimits) {
        this.context = context;
        this.categories = categories;
        this.categoryLimits = categoryLimits; // Assign the limits map
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

        // Pobranie widoków
        TextView categoryNameText = convertView.findViewById(R.id.categoryNameText);
        TextView categoryLimitText = convertView.findViewById(R.id.categoryLimitText);

        // Pobranie danych
        String category = categories.get(position);
        String limit = categoryLimits.get(category); // Pobierz limit dla danej kategorii

        // Ustawienie nazwy kategorii
        categoryNameText.setText(category);

        // Ustawienie limitu, jeśli istnieje
        if (limit != null && !limit.isEmpty()) {
            categoryLimitText.setText(limit + " PLN");
            categoryLimitText.setVisibility(View.VISIBLE);
        } else {
            categoryLimitText.setVisibility(View.GONE);
        }

        return convertView;
    }
}
