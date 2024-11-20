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
import com.example.androidproject.R;
import com.example.androidproject.baza.BazaDanych;
import com.example.androidproject.stronaglowna.MainActivity;
import com.example.androidproject.transakcje.dao.TransakcjaDAO;
import com.example.androidproject.transakcje.encje.TransakcjaEntity;
import java.util.List;

public class HistoriaTransakcjiFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_historia_transakcji, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button doneButton = view.findViewById(R.id.gotoweButton);
        doneButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_historiaTransakcjiFragment_to_mainFragment);
        });

        wyswietlTransakcje();
    }

    private void wyswietlTransakcje() {
        TransakcjaDAO transakcjaDao = getTransakcjaDao();
        List<TransakcjaEntity> transakcje = transakcjaDao.getAll();
        for(TransakcjaEntity transakcja : transakcje) {
            Log.d(getTag(), transakcja.getKwota() + " " + transakcja.getKategoria() + " " + transakcja.getData());
        }
    }

    private TransakcjaDAO getTransakcjaDao() {
        MainActivity activity = (MainActivity) getActivity();
        BazaDanych db = activity.getDb();
        return db.transakcjaDAO();
    }

}