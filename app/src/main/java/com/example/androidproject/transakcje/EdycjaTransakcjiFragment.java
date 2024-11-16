package com.example.androidproject.transakcje;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.androidproject.R;


public class EdycjaTransakcjiFragment extends Fragment {

    public EdycjaTransakcjiFragment() {
    }

    public static EdycjaTransakcjiFragment newInstance() {
        return new EdycjaTransakcjiFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edycja_transakcji, container, false);
    }
}