package com.example.androidproject.adaptery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.androidproject.R;
import com.example.androidproject.analiza.Statystyka;

import java.util.List;

public class StatystykiAdapter extends BaseAdapter {

    private final Context context;
    private final List<Statystyka> statystyki;

    public StatystykiAdapter(Context context, List<Statystyka> statystyki) {
        this.context = context;
        this.statystyki = statystyki;
    }

    @Override
    public int getCount() {
        return statystyki.size();
    }

    @Override
    public Object getItem(int position) {
        return statystyki.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.lista_statystyk, parent, false);
        }

        TextView naglowek = convertView.findViewById(R.id.statystykiNaglowek);
        TextView wartosc = convertView.findViewById(R.id.statystykiWartosc);

        Statystyka statystyka = statystyki.get(position);

        naglowek.setText(statystyka.getNaglowek());
        wartosc.setText(statystyka.getWartosc());

        return convertView;
    }
}