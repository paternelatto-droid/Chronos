package com.latto.chronos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.latto.chronos.models.Event;
import com.latto.chronos.utils.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Utils {

    public static final int MAX_CALENDAR_DAYS = 42;

    public static final String DAILY = "Repeat Daily";
    public static final String WEEKLY = "Repeat Weekly";
    public static final String MONTHLY = "Repeat Monthly";
    public static final String YEARLY = "Repeat Yearly";

    public static String CURRENT_FILTER = "Aujourd'hui";
    public static final String TODAY = "Aujourd'hui";
    public static final String NEXT_7_DAYS = "7 prochain jours";
    public static final String NEXT_30_DAYS = "30 prochain jours";
    public static final String THIS_YEAR = "This Year";

    public enum NotificationPreference {
        TEN_MINUTES_BEFORE,
        ONE_HOUR_BEFORE,
        ONE_DAY_BEFORE,
        AT_THE_TIME_OF_EVENT
    }

    public enum AppTheme {
        INDIGO,
        DARK,
    }

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.FRENCH);
    public static final SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);
    public static final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.ENGLISH);
    public static final SimpleDateFormat eventDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static Date convertStringToDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String convertToMySQLDatetime(String displayDate) {
        if (displayDate == null || displayDate.trim().isEmpty()) return null;

        try {
            // Parse le texte français avec mois en lettres abrégées
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.FRENCH);
            Date date = displayFormat.parse(displayDate);

            // Formate en datetime MySQL
            SimpleDateFormat mysqlFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return mysqlFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // ou displayDate si tu veux retourner la valeur brute
        }
    }


    @SuppressLint("ResourceType")
    public static ArrayList<String> getColors(Context context) {
        ArrayList<String> colors = new ArrayList<>();
        colors.add(context.getResources().getString(R.color.darkIndigo));
        colors.add(context.getResources().getString(R.color.yellow));
        colors.add(context.getResources().getString(R.color.deepPurple));
        colors.add(context.getResources().getString(R.color.pink));
        colors.add(context.getResources().getString(R.color.Grey));
        colors.add(context.getResources().getString(R.color.cyan));
        colors.add(context.getResources().getString(R.color.green));
        colors.add(context.getResources().getString(R.color.lime));
        colors.add(context.getResources().getString(R.color.lightIndigo));
        colors.add(context.getResources().getString(R.color.black));
        //colors.add(context.getResources().getString(R.color.color9));
        //colors.add(context.getResources().getString(R.color.lite_blue));
        colors.add(context.getResources().getString(R.color.red));
        colors.add(context.getResources().getString(R.color.brown));
        //colors.add(context.getResources().getString(R.color.color11));
        return colors;
    }



    /**
     * Récupère le token utilisateur stocké après le login (optionnel)
     */
    public static String getUserToken(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString("token", "");
    }

    /**
     * Trie les événements par date_debut puis par time (si disponible)
     * @param events liste d'événements à trier
     */
    public static void sortEventsByDate(List<Event> events) {
        if (events == null || events.isEmpty()) return;

        Collections.sort(events, new Comparator<Event>() {
            @Override
            public int compare(Event e1, Event e2) {
                try {
                    // Maintenant dateDebut contient déjà la date ET l'heure -> Exemple : "2025-10-13 10:37:30"
                    long d1 = eventDateFormat.parse(e1.getDateDebut()).getTime();
                    long d2 = eventDateFormat.parse(e2.getDateDebut()).getTime();
                    return Long.compare(d1, d2);

                } catch (ParseException ex) {
                    ex.printStackTrace();
                    return 0;
                }
            }
        });
    }

    /**
     * Filtre les événements qui se produisent à une date spécifique.
     * Gère les événements d'une journée et les événements sur plusieurs jours.
     *
     * @param events liste de tous les événements
     * @param date   date à vérifier
     * @return liste filtrée des événements du jour
     */
    public static List<Event> filterEventsByDate(List<Event> events, Date date) {
        List<Event> filtered = new ArrayList<>();
        if (events == null || events.isEmpty() || date == null) return filtered;

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        for (Event event : events) {
            try {
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(Objects.requireNonNull(eventDateFormat.parse(event.getDateDebut())));

                Calendar endCal = Calendar.getInstance();
                if (event.getDateFin() != null && !event.getDateFin().isEmpty()) {
                    endCal.setTime(Objects.requireNonNull(eventDateFormat.parse(event.getDateFin())));
                } else {
                    endCal.setTime(startCal.getTime());
                }

                // Vérifie si la date est comprise entre date_debut et date_fin
                if (!date.before(startCal.getTime()) && !date.after(endCal.getTime())) {
                    filtered.add(event);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Trie les événements du jour
        sortEventsByDate(filtered);

        return filtered;
    }

    public static int getCurrentUserId(Context context) {
        SessionManager session = new SessionManager(context);
        int userId = session.getUserId();
       return userId;
    }

    private static final String DATE_FORMAT = "dd-MM-yyyy"; // adapte au format de tes events

    /**
     * Convertit une String en Date
     */
    public static Date parseDate(String dateStr) {
        if (dateStr == null) return null;
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Vérifie si deux dates sont le même jour
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

}
