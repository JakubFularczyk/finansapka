package com.example.androidproject.adaptery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.androidproject.R;
import com.example.androidproject.baza.encje.TransakcjaEntity;
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

        TextView amountTextView = convertView.findViewById(R.id.transactionAmountText);
        TextView categoryTextView = convertView.findViewById(R.id.transactionCategoryText);

        int listViewHeight = parent.getHeight();
        ViewGroup.LayoutParams params = convertView.getLayoutParams();
        params.height = listViewHeight / 3;
        convertView.setLayoutParams(params);

        amountTextView.setText(transaction.getKwota() + " PLN");
        categoryTextView.setText(transaction.getKategoria());

        double kwota = Double.parseDouble(transaction.getKwota());
        if (kwota < 0) {
            amountTextView.setTextColor(context.getResources().getColor(R.color.dark_red));
        } else {
            amountTextView.setTextColor(context.getResources().getColor(R.color.dark_green));
        }

        return convertView;
    }
}
