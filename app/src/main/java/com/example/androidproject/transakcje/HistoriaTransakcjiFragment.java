package com.example.androidproject.transakcje;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.example.androidproject.R;
import com.example.androidproject.adaptery.HistoriaTransakcjiAdapter;
import com.example.androidproject.baza.BazaDanych;
import com.example.androidproject.stronaglowna.MainActivity;
import com.example.androidproject.transakcje.dao.TransakcjaDAO;
import com.example.androidproject.transakcje.encje.TransakcjaEntity;
import java.util.List;

public class HistoriaTransakcjiFragment extends Fragment {
    private ListView transactionListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historia_transakcji, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button dodajTransakcjeButton = view.findViewById(R.id.dodajKolejnaTransakcjeButton);
        dodajTransakcjeButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_historiaTransakcjiFragment_to_dodajTransakcjeFragment);
        });


        Button doneButton = view.findViewById(R.id.gotoweButton);
        doneButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_historiaTransakcjiFragment_to_mainFragment);
        });

        transactionListView = view.findViewById(R.id.transactionListView);
        wyswietlTransakcje();
    }

    private void wyswietlTransakcje() {
        TransakcjaDAO transakcjaDao = getTransakcjaDao();
        List<TransakcjaEntity> transakcje = transakcjaDao.getAllSortedByDate();


        for (TransakcjaEntity transakcja : transakcje) {
            Log.d(getTag(), transakcja.getKwota() + " " + transakcja.getKategoria() + " " + transakcja.getData());
        }


        HistoriaTransakcjiAdapter adapter = new HistoriaTransakcjiAdapter(requireContext(), transakcje);
        transactionListView.setAdapter(adapter);
        transactionListView.setOnItemClickListener((parent, view, position, id) -> {
            TransakcjaEntity selectedTransaction = transakcje.get(position);


            Bundle bundle = new Bundle();
            bundle.putInt("uid", selectedTransaction.getUid());
            bundle.putString("kwota", selectedTransaction.getKwota());
            bundle.putString("kategoria", selectedTransaction.getKategoria());
            bundle.putSerializable("data", selectedTransaction.getData());
            bundle.putString("opis", selectedTransaction.getOpis());


            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_historiaTransakcjiFragment_to_edycjaTransakcjiFragment, bundle);
        });


    }

    private TransakcjaDAO getTransakcjaDao() {
        MainActivity activity = (MainActivity) getActivity();
        BazaDanych db = activity.getDb();
        return db.transakcjaDAO();
    }


}