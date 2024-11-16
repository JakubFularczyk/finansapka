package com.example.androidproject.ustawienia;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.androidproject.R;


public class LimitKategoriiFragment extends Fragment {

    public LimitKategoriiFragment() {
        // Required empty public constructor
    }

    public static LimitKategoriiFragment newInstance() {
        return new LimitKategoriiFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_limit_kategorii, container, false);
    }
}