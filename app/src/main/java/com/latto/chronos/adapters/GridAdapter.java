package com.latto.chronos.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.latto.chronos.R;
import com.latto.chronos.models.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GridAdapter extends BaseAdapter {

    private final Context context;
    private final List<Date> dates;
    private final Calendar currentMonth;
    private final int userId;
    private final List<Event> events;
    private Date selectedDate;

    private final SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public GridAdapter(Context context, List<Date> dates, Calendar currentMonth, int userId, List<Event> events, Date selectedDate) {
        this.context = context;
        this.dates = dates;
        this.currentMonth = currentMonth;
        this.userId = userId;
        this.events = events;
        this.selectedDate = selectedDate;
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return dates.size();
    }

    @Override
    public Object getItem(int position) {
        return dates.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_calendar_day, parent, false);
        }

        TextView dayText = convertView.findViewById(R.id.CalendarDay_TextView_Day);
        LinearLayout eventsContainer = convertView.findViewById(R.id.CalendarDay_LinearLayout_Dots);

        Date date = dates.get(position);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        // Affiche le jour du mois
        dayText.setText(dayFormat.format(date));

        // Fond pour jour sélectionné
        if (selectedDate != null) {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTime(selectedDate);
            if (cal.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR)
                    && cal.get(Calendar.MONTH) == selectedCal.get(Calendar.MONTH)
                    && cal.get(Calendar.DAY_OF_MONTH) == selectedCal.get(Calendar.DAY_OF_MONTH)) {
                convertView.setBackgroundResource(R.drawable.bg_selected_day);
            } else {
                // Fond jour actuel
                Calendar today = Calendar.getInstance();
                if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                        && cal.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                        && cal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                    convertView.setBackgroundResource(R.drawable.bg_today_highlight);
                } else {
                    convertView.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        }

        // Griser les jours hors mois
        if (cal.get(Calendar.MONTH) != currentMonth.get(Calendar.MONTH)) {
            dayText.setTextColor(Color.parseColor("#A0A0A0")); // gris clair
        } else {
            dayText.setTextColor(Color.BLACK);
        }

        // Supprime les anciennes pastilles
        eventsContainer.removeAllViews();

        // Ajoute pastilles pour les événements multi-journées
        for (Event e : events) {
            try {
                Date start = sdf.parse(e.dateDebut);
                Date end = e.dateFin != null ? sdf.parse(e.dateFin) : start;

                // Comparer uniquement les jours (ignorer les heures)
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(start);
                startCal.set(Calendar.HOUR_OF_DAY, 0);
                startCal.set(Calendar.MINUTE, 0);
                startCal.set(Calendar.SECOND, 0);
                startCal.set(Calendar.MILLISECOND, 0);

                Calendar endCal = Calendar.getInstance();
                endCal.setTime(end);
                endCal.set(Calendar.HOUR_OF_DAY, 0);
                endCal.set(Calendar.MINUTE, 0);
                endCal.set(Calendar.SECOND, 0);
                endCal.set(Calendar.MILLISECOND, 0);

                Calendar currentCal = Calendar.getInstance();
                currentCal.setTime(date);
                currentCal.set(Calendar.HOUR_OF_DAY, 0);
                currentCal.set(Calendar.MINUTE, 0);
                currentCal.set(Calendar.SECOND, 0);
                currentCal.set(Calendar.MILLISECOND, 0);

                if (!currentCal.before(startCal) && !currentCal.after(endCal)) {
                    View dot = new View(context);
                    int size = 15;
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                    params.setMargins(2, 2, 2, 2);
                    dot.setLayoutParams(params);
                    dot.setBackgroundColor(e.color != 0 ? e.color : Color.parseColor("#2196F3"));
                    eventsContainer.addView(dot);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return convertView;
    }
}
