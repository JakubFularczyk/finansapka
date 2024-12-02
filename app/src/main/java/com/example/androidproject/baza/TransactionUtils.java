package com.example.androidproject.baza;

import java.util.Calendar;
import java.util.Date;

public class TransactionUtils {

    /**
     * Calculates the next date based on the current date and interval.
     *
     * @param currentDate Current date of the transaction.
     * @param interval    Recurrence interval (Dzienna, Tygodniowa, Miesięczna, Roczna).
     * @return The next calculated date.
     */
    public static Date calculateNextDate(Date currentDate, String interval) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        switch (interval) {
            case "Dzienna":
                calendar.add(Calendar.SECOND, 1);
                break;
            case "Tygodniowa":
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case "Miesięczna":
                calendar.add(Calendar.MONTH, 1);
                break;
            case "Roczna":
                calendar.add(Calendar.YEAR, 1);
                break;
            default:
                throw new IllegalArgumentException("Invalid interval: " + interval);
        }

        return calendar.getTime();
    }
}