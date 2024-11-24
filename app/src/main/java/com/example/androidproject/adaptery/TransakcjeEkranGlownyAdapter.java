package com.example.androidproject.adaptery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.androidproject.R;
import com.example.androidproject.transakcje.encje.TransakcjaEntity;
import java.util.List;

public class TransakcjeEkranGlownyAdapter extends BaseAdapter {
    private final Context context;
    private final List<TransakcjaEntity> transactions;

    public TransakcjeEkranGlownyAdapter(Context context, List<TransakcjaEntity> transactions) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.lista_transakcji_na_ekranie_glownym, parent, false);
        }

        TransakcjaEntity transaction = transactions.get(position);

        TextView amountText = convertView.findViewById(R.id.transactionAmountText);
        TextView categoryText = convertView.findViewById(R.id.transactionCategoryText);

        amountText.setText(transaction.getKwota() + " PLN");
        categoryText.setText(transaction.getKategoria());

        return convertView;
    }
}