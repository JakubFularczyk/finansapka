package com.example.androidproject.transakcje;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.room.Room;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.androidproject.R;
//import com.example.androidproject.baza.BazaDanych;
import com.example.androidproject.baza.BazaDanych;
import com.example.androidproject.stronaglowna.MainActivity;
import com.example.androidproject.stronaglowna.MainFragment;
//import com.example.androidproject.transakcje.dao.TransakcjaDAO;
import com.example.androidproject.transakcje.dao.TransakcjaDAO;
import com.example.androidproject.transakcje.encje.TransakcjaEntity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DodajTransakcjeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dodaj_transakcje, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Spinner spinner = view.findViewById(R.id.kategoriaSpinner);
        List<String> testList = List.of("dom","samochod","jedzenie");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, testList);
        spinner.setAdapter(adapter);

        EditText kwotaInput = view.findViewById(R.id.kwotaInput);
        Calendar localCalendar = Calendar.getInstance();
        CalendarView kalendarz = view.findViewById(R.id.calendarView);
        kalendarz.setOnDateChangeListener(new OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                localCalendar.set(year, month, dayOfMonth);
            }
        });

        Button navigateToHistoryButton = view.findViewById(R.id.dodajPrzychodButton2);
        navigateToHistoryButton.setOnClickListener(v -> {
            Log.d(getTag(), "Dane do zapisania: ");
            Log.d(getTag(), "Kwota: " + kwotaInput.getText());
            Log.d(getTag(), "Kategoria: " + spinner.getSelectedItem());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Log.d(getTag(), "Data: " + sdf.format(localCalendar.getTime()));
            TransakcjaDAO transakcjaDAO = getTransakcjaDao();
            TransakcjaEntity encja = przygotujEncje(kwotaInput.getText().toString(), spinner.getSelectedItem().toString(), localCalendar.getTime());
            transakcjaDAO.insertAll(encja);
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_przychodyFragment_to_historiaTransakcjiFragment);
        });
    }

    private TransakcjaDAO getTransakcjaDao() {
        MainActivity activity = (MainActivity) getActivity();
        BazaDanych db = activity.getDb();
        return db.transakcjaDAO();
    }

    private TransakcjaEntity przygotujEncje(String kwota, String kategoria, Date data) {
        TransakcjaEntity transakcjaEntity = new TransakcjaEntity();
        transakcjaEntity.setKwota(kwota);
        transakcjaEntity.setKategoria(kategoria);
        transakcjaEntity.setData(data);
        return transakcjaEntity;
    }
}