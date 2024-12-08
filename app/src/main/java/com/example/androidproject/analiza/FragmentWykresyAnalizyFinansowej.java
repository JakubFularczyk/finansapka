package com.example.androidproject.analiza;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.androidproject.R;
import com.example.androidproject.baza.BazaDanych;
import com.example.androidproject.customviews.CustomSwitch;
import com.example.androidproject.transakcje.dao.TransakcjaDAO;
import com.example.androidproject.transakcje.dto.KategoriaSum;
import com.example.androidproject.stronaglowna.MainActivity;
import com.example.androidproject.transakcje.dto.MonthSum;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;

public class FragmentWykresyAnalizyFinansowej extends Fragment {

    private TransakcjaDAO transakcjaDAO; // DAO do zarządzania transakcjami

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wykresy_analizy_finansowej, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicjalizacja DAO
        MainActivity mainActivity = (MainActivity) getActivity();
        BazaDanych db = mainActivity.getDb();
        transakcjaDAO = db.transakcjaDAO();

        // Inicjalizacja wykresów
        PieChart pieChart1 = view.findViewById(R.id.pieChart1);
        PieChart pieChart2 = view.findViewById(R.id.pieChart2);
        BarChart barChart = view.findViewById(R.id.barChart);
        CustomSwitch transactionTypeCustomSwitch = view.findViewById(R.id.transactionTypeCustomSwitch);

        transactionTypeCustomSwitch.setChecked(false);
        updateBarChart(barChart, transakcjaDAO.getMonthlyExpenses(), false);

        // Obsługa zmiany stanu przełącznika
        transactionTypeCustomSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Przełączono na "Przychody"
                updateBarChart(barChart, transakcjaDAO.getMonthlyIncomes(), true);
            } else {
                // Przełączono na "Wydatki"
                updateBarChart(barChart, transakcjaDAO.getMonthlyExpenses(), false);
            }
        });
        
        // Konfiguracja wykresu wydatków
        List<KategoriaSum> monthlyExpenses = transakcjaDAO.getMonthlyExpensesByCategory();
        setupExpenseChart(pieChart1, monthlyExpenses);

        // Konfiguracja wykresu przychodów
        List<KategoriaSum> monthlyIncomes = transakcjaDAO.getMonthlyIncomesByCategory();
        setupIncomeChart(pieChart2, monthlyIncomes);
    }

    private void updateBarChart(BarChart chart, List<MonthSum> data, boolean isIncome) {
        List<BarEntry> entries = new ArrayList<>();
        String[] quarters = {"I kwartał", "II kwartał", "III kwartał", "IV kwartał"};
        float[] quarterlyTotals = new float[4]; // Tablica dla kwartałów

        // Grupowanie miesięcy w kwartały
        for (MonthSum item : data) {
            int monthIndex = Integer.parseInt(item.month) - 1; // Indeks miesiąca (0-based)
            int quarterIndex = monthIndex / 3; // Obliczenie indeksu kwartału (0 dla Q1, 1 dla Q2, itd.)
            quarterlyTotals[quarterIndex] += item.total; // Dodaj do odpowiedniego kwartału
        }

        // Dodaj dane do BarEntry
        for (int i = 0; i < quarterlyTotals.length; i++) {
            entries.add(new BarEntry(i, quarterlyTotals[i])); // Dodaj wszystkie kwartały
        }

        // Tworzenie BarDataSet
        BarDataSet dataSet = new BarDataSet(entries, "");
        int startColor = isIncome ? Color.GREEN : Color.RED; // Zielony dla przychodów, czerwony dla wydatków
        int endColor = Color.WHITE; // Gradient kończy się białym
        dataSet.setGradientColor(startColor, endColor); // Gradient
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(14f); // Większy rozmiar wartości

        // Dane do wykresu
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f); // Szerokość słupków
        chart.setData(barData);

        // Ustawienia osi X
        chart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value; // Rzutowanie na indeks
                if (index >= 0 && index < quarters.length) {
                    return quarters[index];
                } else {
                    return ""; // Wartość domyślna w razie błędu
                }
            }
        });
        chart.getXAxis().setGranularity(1f); // Krok co 1 (kwartał)
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM); // Oś X na dole
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setTextSize(14f); // Większa czcionka dla osi X
        chart.getXAxis().setTextColor(Color.BLACK);
        chart.getXAxis().setLabelRotationAngle(0f); // Brak rotacji tekstu
        chart.getXAxis().setYOffset(20f); // Większy margines dla tekstu osi X

        // Dodatkowe marginesy dla wykresu
        chart.setExtraOffsets(5f, 10f, 5f, 20f); // Większy margines dolny

        // Ustawienia osi Y
        chart.getAxisLeft().setGranularity(1f); // Krok na osi Y
        chart.getAxisLeft().setTextSize(14f); // Większa czcionka dla osi Y
        chart.getAxisLeft().setTextColor(Color.BLACK);
        chart.getAxisRight().setEnabled(false); // Wyłącz prawą oś Y

        // Wygląd wykresu
        chart.getDescription().setEnabled(false); // Usuń opis
        chart.getLegend().setEnabled(false); // Usuń legendę
        chart.animateY(1400); // Animacja w osi Y
        chart.invalidate(); // Odśwież wykres
    }

    private void setupExpenseChart(PieChart chart, List<KategoriaSum> data) {
        List<PieEntry> entries = convertToPieEntries(data);
        float totalSum = calculateTotalSum(data);
        setupPieChart(chart, entries, totalSum, "Wydatki", generateRedShades(entries.size()));
    }

    private void setupIncomeChart(PieChart chart, List<KategoriaSum> data) {
        List<PieEntry> entries = convertToPieEntries(data);
        float totalSum = calculateTotalSum(data);
        setupPieChart(chart, entries, totalSum, "Przychody", generateGreenShades(entries.size()));
    }

    private void setupPieChart(PieChart chart, List<PieEntry> entries, float totalSum, String centerText, List<Integer> colors) {
        PieDataSet dataSet = new PieDataSet(entries, ""); // Usuwamy nazwę legendy
        dataSet.setColors(colors); // Ustawiamy kolory
        dataSet.setValueTextColor(Color.BLACK); // Kolor wartości na wykresie
        dataSet.setValueTextSize(16f); // Większy rozmiar czcionki wartości

        PieData data = new PieData(dataSet);

        // Dodaj formatowanie wartości
        data.setValueFormatter(new PercentAndAmountFormatter(totalSum));

        chart.setData(data);

        // Ustawienia wykresu
        chart.setDrawEntryLabels(false); // Usuń etykiety z wykresu
        chart.setCenterText(centerText); // Tekst w środku
        chart.setCenterTextSize(24f); // Większy rozmiar tekstu w środku
        chart.setCenterTextColor(Color.BLACK); // Kolor tekstu w środku
        chart.setCenterTextTypeface(getResources().getFont(R.font.googlesans_bold)); // Czcionka: Google Sans Bold
        chart.getDescription().setEnabled(false); // Usuń opis
        chart.getLegend().setTextColor(Color.BLACK); // Widoczniejsza legenda
        chart.getLegend().setTextSize(16f); // Większa czcionka legendy
        chart.getLegend().setTypeface(getResources().getFont(R.font.google_sans_regular)); // Czcionka: Google Sans Regular
        chart.getLegend().setWordWrapEnabled(true); // Zwijanie tekstu legendy

        // Dodaj interakcję z legendą
        chart.setOnChartValueSelectedListener(new HighlightLegendListener(chart));

        chart.animateY(1400); // Animacja Y
        chart.invalidate(); // Odświeżanie wykresu
    }

    // Klasa obsługująca interakcję z legendą
    private static class HighlightLegendListener implements OnChartValueSelectedListener {
        private final PieChart chart;

        public HighlightLegendListener(PieChart chart) {
            this.chart = chart;
        }

        @Override
        public void onValueSelected(Entry e, Highlight h) {
            PieEntry pieEntry = (PieEntry) e;

            // Zresetuj wszystkie kolory w legendzie
            for (LegendEntry legendEntry : chart.getLegend().getEntries()) {
                legendEntry.formColor = Color.BLACK; // Ustaw domyślny kolor legendy
            }

            // Wyróżnij wybraną kategorię w legendzie
            for (LegendEntry legendEntry : chart.getLegend().getEntries()) {
                if (legendEntry.label.equals(pieEntry.getLabel())) {
                    legendEntry.formColor = Color.RED; // Wyróżnij na czerwono
                    break;
                }
            }

            chart.invalidate(); // Odśwież wykres
        }

        @Override
        public void onNothingSelected() {
            // Przywróć wszystkie kolory legendy
            for (LegendEntry legendEntry : chart.getLegend().getEntries()) {
                legendEntry.formColor = Color.BLACK; // Ustaw domyślny kolor
            }

            chart.invalidate(); // Odśwież wykres
        }
    }

    // Konwersja danych do PieEntry
    private List<PieEntry> convertToPieEntries(List<KategoriaSum> data) {
        List<PieEntry> entries = new ArrayList<>();
        for (KategoriaSum item : data) {
            entries.add(new PieEntry(Math.abs(item.suma), item.kategoria)); // Kwota jako wartość
        }
        return entries;
    }

    // Obliczenie całkowitej sumy
    private float calculateTotalSum(List<KategoriaSum> data) {
        float total = 0;
        for (KategoriaSum item : data) {
            total += Math.abs(item.suma);
        }
        return total;
    }

    // Formatowanie wartości: procenty i kwoty z PLN
    private static class PercentAndAmountFormatter extends ValueFormatter {
        private final float totalSum;

        public PercentAndAmountFormatter(float totalSum) {
            this.totalSum = totalSum;
        }

        @Override
        public String getPieLabel(float value, PieEntry pieEntry) {
            float percentage = (value / totalSum) * 100;
            return String.format("%.0f%% (%.0f PLN)", percentage, value); // Dodano PLN
        }
    }

    // Generowanie odcieni czerwieni dla wydatków
    private List<Integer> generateRedShades(int count) {
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            float fraction = (float) i / count;
            colors.add(Color.rgb(255, (int) (100 + 155 * fraction), (int) (100 + 155 * fraction)));
        }
        return colors;
    }

    // Generowanie odcieni zieleni dla przychodów
    private List<Integer> generateGreenShades(int count) {
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            float fraction = (float) i / count;
            colors.add(Color.rgb((int) (100 + 155 * fraction), 255, (int) (100 + 155 * fraction)));
        }
        return colors;
    }
}
