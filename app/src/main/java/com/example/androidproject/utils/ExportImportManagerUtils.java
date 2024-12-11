package com.example.androidproject.utils;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.example.androidproject.stronaglowna.MainActivity;
import com.example.androidproject.baza.dao.KategoriaDAO;
import com.example.androidproject.baza.dao.TransakcjaCyklicznaDAO;
import com.example.androidproject.baza.dao.TransakcjaDAO;
import com.example.androidproject.baza.encje.KategoriaEntity;
import com.example.androidproject.baza.encje.TransakcjaCyklicznaEntity;
import com.example.androidproject.baza.encje.TransakcjaEntity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


public class ExportImportManagerUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


    public static void exportDatabaseToJSON(Context context) {

        try {
            // Pobranie DAO
            TransakcjaDAO transakcjaDAO = ((MainActivity) context).getDb().transakcjaDAO();
            TransakcjaCyklicznaDAO transakcjaCyklicznaDAO = ((MainActivity) context).getDb().transakcjaCyklicznaDAO();
            KategoriaDAO kategoriaDAO = ((MainActivity) context).getDb().kategoriaDAO();

            // Tworzenie obiektów JSON
            JSONObject databaseJson = new JSONObject();

            // Kategorie
            List<KategoriaEntity> kategorie = kategoriaDAO.getAll();
            JSONArray kategorieArray = new JSONArray();
            for (KategoriaEntity kategoria : kategorie) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("uid", kategoria.uid);
                jsonObject.put("nazwa", kategoria.nazwa);

                // Obsługa null dla limit
                jsonObject.put("limit", kategoria.limit != null ? kategoria.limit : JSONObject.NULL);

                // Obsługa null dla aktualnaKwota
                jsonObject.put("aktualnaKwota", kategoria.aktualnaKwota != null ? kategoria.aktualnaKwota : JSONObject.NULL);

                kategorieArray.put(jsonObject);
            }
            databaseJson.put("kategorie", kategorieArray);


            List<TransakcjaEntity> transakcje = transakcjaDAO.getAll(); // Inicjalizacja zmiennej 'transakcje'

            // Tworzenie tablicy JSON dla transakcji
            JSONArray transakcjeArray = new JSONArray();
            for (TransakcjaEntity transakcja : transakcje) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("uid", transakcja.uid);
                jsonObject.put("kwota", transakcja.kwota);
                jsonObject.put("kategoria", transakcja.kategoria);
                jsonObject.put("data", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(transakcja.data));
                jsonObject.put("opis", transakcja.opis != null ? transakcja.opis : ""); // Obsługa opcjonalnego opisu
                jsonObject.put("isCyclicChild", transakcja.isCyclicChild); // Nowe pole
                jsonObject.put("parentTransactionId", transakcja.parentTransactionId != null ? transakcja.parentTransactionId : JSONObject.NULL);

                transakcjeArray.put(jsonObject);
            }
            databaseJson.put("transakcje", transakcjeArray);

            // Transakcje cykliczne
            List<TransakcjaCyklicznaEntity> transakcjeCykliczne = transakcjaCyklicznaDAO.getAll();
            JSONArray transakcjeCykliczneArray = new JSONArray();
            for (TransakcjaCyklicznaEntity transakcjaCykliczna : transakcjeCykliczne) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("uid", transakcjaCykliczna.uid);
                jsonObject.put("idTransakcji", transakcjaCykliczna.idTransakcji); // Klucz obcy
                jsonObject.put("dataOd", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(transakcjaCykliczna.dataOd));
                jsonObject.put("interwal", transakcjaCykliczna.interwal); // np. DZIEN, TYDZIEN

                transakcjeCykliczneArray.put(jsonObject);
            }
            databaseJson.put("transakcjeCykliczne", transakcjeCykliczneArray);
            // Zapis do pliku
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Files.FileColumns.DISPLAY_NAME, "DaneFinansowe.json");
            contentValues.put(MediaStore.Files.FileColumns.MIME_TYPE, "application/json");
            contentValues.put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);

            Uri uri = context.getContentResolver().insert(MediaStore.Files.getContentUri("external"), contentValues);
            if (uri != null) {
                Log.d("ExportDebug", "Plik zapisany pod URI: " + uri.toString());
                try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
                    outputStream.write(databaseJson.toString().getBytes());
                    outputStream.flush();
                    Log.d("ExportDebug", "Plik zapisany poprawnie");
                    Toast.makeText(context, "Eksport zakończony sukcesem! Plik zapisany pod: " + uri, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e("ExportDebug", "Błąd zapisu pliku", e);
                }
            } else {
                Log.e("ExportDebug", "Nie udało się uzyskać URI dla pliku");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Błąd eksportu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void importDatabaseFromJSON(Context context, Uri uri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JSONObject databaseJson = new JSONObject(jsonBuilder.toString());

            // Pobierz DAO
            TransakcjaDAO transakcjaDAO = ((MainActivity) context).getDb().transakcjaDAO();
            TransakcjaCyklicznaDAO transakcjaCyklicznaDAO = ((MainActivity) context).getDb().transakcjaCyklicznaDAO();
            KategoriaDAO kategoriaDAO = ((MainActivity) context).getDb().kategoriaDAO();

            // Czyszczenie tabel
            kategoriaDAO.clearTable();
            transakcjaDAO.clearTable();
            transakcjaCyklicznaDAO.clearTable();

            // Kategorie
            JSONArray kategorieArray = databaseJson.getJSONArray("kategorie");
            for (int i = 0; i < kategorieArray.length(); i++) {
                JSONObject jsonObject = kategorieArray.getJSONObject(i);

                KategoriaEntity kategoria = new KategoriaEntity();
                kategoria.uid = jsonObject.getInt("uid");
                kategoria.nazwa = jsonObject.getString("nazwa");

                // Obsługa null dla limit
                if (jsonObject.has("limit") && !jsonObject.isNull("limit")) {
                    kategoria.limit = jsonObject.getString("limit");
                } else {
                    kategoria.limit = null; // Domyślna wartość
                }

                // Obsługa null dla aktualnaKwota
                if (jsonObject.has("aktualnaKwota") && !jsonObject.isNull("aktualnaKwota")) {
                    kategoria.setAktualnaKwota(jsonObject.getDouble("aktualnaKwota"));
                } else {
                    kategoria.setAktualnaKwota(null); // Domyślna wartość
                }

                kategoriaDAO.insert(kategoria);
            }


            // Transakcje
            JSONArray transakcjeArray = databaseJson.getJSONArray("transakcje");
            for (int i = 0; i < transakcjeArray.length(); i++) {
                JSONObject jsonObject = transakcjeArray.getJSONObject(i);

                TransakcjaEntity transakcja = new TransakcjaEntity();
                transakcja.uid = jsonObject.getInt("uid");
                transakcja.kwota = jsonObject.getString("kwota");
                transakcja.kategoria = jsonObject.getString("kategoria");
                transakcja.data = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(jsonObject.getString("data"));
                transakcja.opis = jsonObject.has("opis") ? jsonObject.getString("opis") : null; // Obsługa opcjonalnego pola

                // Obsługa cyklicznych pól
                transakcja.isCyclicChild = jsonObject.optBoolean("isCyclicChild", false);
                transakcja.parentTransactionId = jsonObject.has("parentTransactionId") && !jsonObject.isNull("parentTransactionId")
                        ? jsonObject.getInt("parentTransactionId")
                        : null;

                transakcjaDAO.insert(transakcja);
            }

            // Transakcje cykliczne
            JSONArray transakcjeCykliczneArray = databaseJson.getJSONArray("transakcjeCykliczne");
            for (int i = 0; i < transakcjeCykliczneArray.length(); i++) {
                JSONObject jsonObject = transakcjeCykliczneArray.getJSONObject(i);

                TransakcjaCyklicznaEntity transakcjaCykliczna = new TransakcjaCyklicznaEntity();
                transakcjaCykliczna.uid = jsonObject.getInt("uid");
                transakcjaCykliczna.idTransakcji = jsonObject.getInt("idTransakcji"); // Klucz obcy
                transakcjaCykliczna.dataOd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(jsonObject.getString("dataOd"));
                transakcjaCykliczna.interwal = jsonObject.getString("interwal");

                // Sprawdzenie, czy klucz obcy idTransakcji istnieje w bazie danych
                TransakcjaEntity powiazanaTransakcja = transakcjaDAO.getById(transakcjaCykliczna.idTransakcji);
                if (powiazanaTransakcja != null) {
                    // Jeśli transakcja istnieje, wstaw transakcję cykliczną do bazy danych
                    transakcjaCyklicznaDAO.insert(transakcjaCykliczna);
                } else {
                    // Loguj błąd lub przechowaj do dalszego przetwarzania
                    Log.e("ImportError", "Nie znaleziono TransakcjaEntity dla idTransakcji: " + transakcjaCykliczna.idTransakcji);
                }
            }

            Toast.makeText(context, "Import zakończony sukcesem!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Błąd importu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
