package com.example.androidproject.adaptery;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.androidproject.R;
import com.example.androidproject.transakcje.encje.TransakcjaEntity;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


public class HistoriaTransakcjiAdapter extends BaseAdapter {
    private final Context context;
    private final List<TransakcjaEntity> transactions;

    public HistoriaTransakcjiAdapter(Context context, List<TransakcjaEntity> transactions) {
        this.context = context;
        this.transactions = transactions;
    }

    @Override
    public int getCount() {
        return transactions.size();
    }

    @Override
    public Object getItem(int position) {
        return transactions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.lista_transakcji_historia, parent, false);
        }

        TransakcjaEntity transaction = transactions.get(position);

        // Pobranie widoków z layoutu
        TextView dateTextView = convertView.findViewById(R.id.transactionDateText);
        TextView amountTextView = convertView.findViewById(R.id.transactionAmountText);
        TextView categoryTextView = convertView.findViewById(R.id.transactionCategoryText);
        TextView descriptionTextView = convertView.findViewById(R.id.transactionDescriptionText);

        // Ustawienie wartości
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("pl"));
        dateTextView.setText(dateFormat.format(transaction.getData()));
        amountTextView.setText(transaction.getKwota() + " PLN");
        categoryTextView.setText(transaction.getKategoria());
        descriptionTextView.setText(transaction.getOpis() != null ? transaction.getOpis() : "Brak opisu");

        return convertView;
    }
}
