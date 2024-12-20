package com.example.androidproject.adaptery;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.androidproject.R;
import com.example.androidproject.stronaglowna.MainActivity;
import com.example.androidproject.baza.dao.KategoriaDAO;
import com.example.androidproject.baza.dao.TransakcjaCyklicznaDAO;
import com.example.androidproject.baza.dao.TransakcjaDAO;
import com.example.androidproject.baza.encje.KategoriaEntity;
import com.example.androidproject.baza.encje.TransakcjaEntity;
import com.example.androidproject.service.TransactionsService;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoriaTransakcjiAdapter extends BaseAdapter {

    private TransactionsService transactionsService;
    private final Context context;
    private final List<TransakcjaEntity> transactions;

    public HistoriaTransakcjiAdapter(Context context, Activity activity, List<TransakcjaEntity> transactions) {
        this.context = context;
        this.transactionsService = new TransactionsService((MainActivity) activity);
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


        TextView dateTextView = convertView.findViewById(R.id.transactionDateText);
        TextView amountTextView = convertView.findViewById(R.id.transactionAmountText);
        TextView categoryTextView = convertView.findViewById(R.id.transactionCategoryText);
        TextView descriptionTextView = convertView.findViewById(R.id.transactionDescriptionText);
        TextView recurringLabel = convertView.findViewById(R.id.cyclicTransactionText);


        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("pl"));
        dateTextView.setText(dateFormat.format(transaction.getData()));


        amountTextView.setText(transaction.getKwota() + " PLN");
        categoryTextView.setText(transaction.getKategoria());
        descriptionTextView.setText(transaction.getOpis() != null ? transaction.getOpis() : "Brak opisu");


        double kwota = Double.parseDouble(transaction.getKwota());
        if (kwota < 0) {
            amountTextView.setTextColor(context.getResources().getColor(R.color.dark_red));
        } else {
            amountTextView.setTextColor(context.getResources().getColor(R.color.dark_green));
        }

        TransakcjaCyklicznaDAO recurringDAO = ((MainActivity) context).getDb().transakcjaCyklicznaDAO();
        boolean isRecurring = recurringDAO.isRecurringTransaction(transaction.getUid());

        if (isRecurring) {
            recurringLabel.setVisibility(View.VISIBLE); // Pokaż etykietę "Transakcja cykliczna"
        } else {
            recurringLabel.setVisibility(View.GONE); // Ukryj etykietę
        }

        convertView.setOnLongClickListener(v -> {
            v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(150).withEndAction(() -> {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150);
            });

            showPopupMenu(v, transaction, position, isRecurring);
            return true;
        });

        return convertView;
    }

    private void showPopupMenu(View anchor, TransakcjaEntity transaction, int position, boolean isRecurring) {
        PopupMenu popupMenu = new PopupMenu(context, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.historia_transakcji_popup_menu, popupMenu.getMenu());



        if (isRecurring) {
            popupMenu.getMenu().findItem(R.id.action_delete_recurring).setVisible(true); // Opcja dla cyklicznych
            popupMenu.getMenu().findItem(R.id.action_delete).setVisible(false);
        } else {
            popupMenu.getMenu().findItem(R.id.action_delete_recurring).setVisible(false); // Opcja dla zwykłych
            popupMenu.getMenu().findItem(R.id.action_delete).setVisible(true);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit) {
                editTransaction(anchor, transaction);
                return true;
            } else if (item.getItemId() == R.id.action_delete) {
                deleteTransaction(transaction, position);
                return true;
            } else if (item.getItemId() == R.id.action_delete_recurring) {
                if (isRecurring) {
                    deleteRecurringTransaction(transaction);
                }
                return true;
            }
            return false;
        });

        popupMenu.show();
    }
    private void deleteRecurringTransaction(TransakcjaEntity transaction) {
        // Wyświetlenie dialogu wyboru dla użytkownika
        new AlertDialog.Builder(context)
                .setTitle("Usuń transakcję cykliczną")
                .setMessage("Co chcesz zrobić?")
                .setPositiveButton("Usuń wszystkie rekordy", (dialog, which) -> deleteAllRecurringTransactions(transaction))
                .setNegativeButton("Wyłącz cykliczność", (dialog, which) -> disableRecurringTransaction(transaction))
                .setNeutralButton("Anuluj", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void disableRecurringTransaction(TransakcjaEntity transaction) {
        try {
            // TODO mozna zamienic na com.example.androidproject.transakcje.service.TransakcjeService.removeRecurringTransaction
            TransakcjaCyklicznaDAO recurringDAO = ((MainActivity) context).getDb().transakcjaCyklicznaDAO();

            // Usuń rekord cykliczności
            recurringDAO.deleteRecurringTransactionById(transaction.getUid());

            Toast.makeText(context, "Cykliczność transakcji została wyłączona", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Błąd podczas wyłączania cykliczności", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAllRecurringTransactions(TransakcjaEntity transaction) {
        try {
            TransakcjaDAO transakcjaDAO = ((MainActivity) context).getDb().transakcjaDAO();
            TransakcjaCyklicznaDAO recurringDAO = ((MainActivity) context).getDb().transakcjaCyklicznaDAO();

            // Usuń powiązane transakcje cykliczne z tabeli TransakcjaEntity
            List<TransakcjaEntity> linkedTransactions = transakcjaDAO.getCopiesByParentId(transaction.getUid());
            for (TransakcjaEntity linkedTransaction : linkedTransactions) {
                transakcjaDAO.delete(linkedTransaction); // Usunięcie kopii z bazy
            }

            // Usuń główny rekord z tabeli TransakcjaCyklicznaEntity
            recurringDAO.deleteRecurringTransactionById(transaction.getUid());

            // Usuń główną transakcję
            transakcjaDAO.delete(transaction);

            // Usuń z listy w adapterze
            transactions.removeIf(t -> t.getUid() == transaction.getUid() || t.isCyclicChild());
            notifyDataSetChanged();

            Toast.makeText(context, "Usunięto transakcję cykliczną i jej kopie", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Błąd podczas usuwania transakcji cyklicznej", Toast.LENGTH_SHORT).show();
        }
    }

    private void editTransaction(View view, TransakcjaEntity transaction) {
        Bundle bundle = new Bundle();
        bundle.putInt("uid", transaction.getUid());
        bundle.putString("kwota", transaction.getKwota());
        bundle.putString("kategoria", transaction.getKategoria());
        bundle.putSerializable("data", transaction.getData());
        bundle.putString("opis", transaction.getOpis());
        bundle.putBoolean("cykliczna", transactionsService.isRecurring(transaction));

        NavController navController = Navigation.findNavController(view);
        navController.navigate(R.id.action_historiaTransakcjiFragment_to_edycjaTransakcjiFragment, bundle);
    }

    private void deleteTransaction(TransakcjaEntity transaction, int position) {
        TransakcjaDAO transakcjaDAO = ((MainActivity) context).getDb().transakcjaDAO();
        KategoriaDAO kategoriaDAO = ((MainActivity) context).getDb().kategoriaDAO();

        KategoriaEntity category = kategoriaDAO.findByName(transaction.getKategoria());
        if (category != null && category.getLimit() != null && !category.getLimit().isEmpty()) {
            double transactionAmount = Double.parseDouble(transaction.getKwota());
            if (transactionAmount < 0) {
                if (category.aktualnaKwota == null) {
                    category.aktualnaKwota = 0.0;
                }
                category.aktualnaKwota += Math.abs(transactionAmount);
            }


            kategoriaDAO.update(category);
        }

        transakcjaDAO.delete(transaction);
        transactions.remove(position);
        notifyDataSetChanged();
        Toast.makeText(context, "Transakcja usunięta", Toast.LENGTH_SHORT).show();
    }



}
