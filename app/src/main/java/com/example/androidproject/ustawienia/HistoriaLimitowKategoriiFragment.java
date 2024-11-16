package com.example.androidproject.ustawienia;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.androidproject.R;

public class HistoriaLimitowKategoriiFragment extends Fragment {

    public HistoriaLimitowKategoriiFragment() {
    }

    public static HistoriaLimitowKategoriiFragment newInstance() {
        return new HistoriaLimitowKategoriiFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historia_limitow_kategorii, container, false);
    }
}