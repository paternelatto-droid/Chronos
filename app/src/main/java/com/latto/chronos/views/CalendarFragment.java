package com.latto.chronos.views;

import static com.latto.chronos.Utils.getCurrentUserId;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.latto.chronos.R;
import com.latto.chronos.Utils;
import com.latto.chronos.adapters.EventListAdapter;
import com.latto.chronos.adapters.GridAdapter;
import com.latto.chronos.api.ApiClient;
import com.latto.chronos.api.ApiService;
import com.latto.chronos.models.Event;
import com.latto.chronos.response.EventResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalendarFragment extends Fragment {

    private static final int ADD_NEW_EVENT_ACTIVITY_REQUEST_CODE = 0;
    private static final int EDIT_EVENT_ACTIVITY_REQUEST_CODE = 1;

    public static final Calendar calendar = Calendar.getInstance(Locale.FRENCH);

    private List<Date> dates = new ArrayList<>();
    private List<Event> events = new ArrayList<>();

    private ImageButton previousMonthImageButton, nextMonthImageButton;
    private TextView currentDateTextView;
    private GridView datesGridView;

    private RecyclerView eventsRecyclerView;
    private EventListAdapter eventListAdapter;
    private ApiService apiService;
    private int userId;
    private TextView noEventsTextView;
    Date selectedDate;
    GridAdapter gridAdapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);

        apiService = ApiClient.getService(getActivity());
        userId = getCurrentUserId(getContext());
        selectedDate = new Date();

        previousMonthImageButton = rootView.findViewById(R.id.CalenderFragment_Button_Prev);
        nextMonthImageButton = rootView.findViewById(R.id.CalenderFragment_Button_Next);
        currentDateTextView = rootView.findViewById(R.id.CalenderFragment_TextView_CurrentDate);
        datesGridView = rootView.findViewById(R.id.CalenderFragment_GridView_Dates);
        noEventsTextView = rootView.findViewById(R.id.CalenderFragment_TextView_NoEvents);

        eventsRecyclerView = rootView.findViewById(R.id.CalenderFragment_RecyclerView_Events);

        // Initialisation RecyclerView
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventListAdapter = new EventListAdapter(getContext(), events);
        eventsRecyclerView.setAdapter(eventListAdapter);


        previousMonthImageButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            setUpCalendar();
        });

        nextMonthImageButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            setUpCalendar();
        });

        datesGridView.setOnItemClickListener((parent, view, position, id) -> {
            // Récupère la date sélectionnée
            selectedDate = dates.get(position);
            if (gridAdapter != null) gridAdapter.setSelectedDate(selectedDate);
            // Met à jour la RecyclerView avec les événements de cette date
            updateEventListForDate(selectedDate);
        });



        // Dans ton GridView longClick, pour créer un nouvel event
        // Ajout du long click pour création d'événement
        datesGridView.setOnItemLongClickListener((parent, view, position, id) -> {
            selectedDate = dates.get(position);
            Calendar today = Calendar.getInstance();
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTime(selectedDate);

            // Bloquer création si date passée
            if (selectedCal.before(today)) {
                Toast.makeText(getContext(), "Impossible de créer un événement dans le passé", Toast.LENGTH_SHORT).show();
                return true; // on consomme l'événement
            }

            // Sinon lancer création
            Intent intent = new Intent(getContext(), NewEventActivity.class);
            intent.putExtra("date", Utils.eventDateFormat.format(selectedDate));
            startActivityForResult(intent, ADD_NEW_EVENT_ACTIVITY_REQUEST_CODE);
            return true;
        });

        FloatingActionButton fabAddEvent = rootView.findViewById(R.id.CalendarFragment_FAB_AddEvent);
        // FAB crée un événement pour la date sélectionnée
        fabAddEvent.setOnClickListener(v -> {
            // Récupère la date sélectionnée dans le calendrier
            selectedDate = selectedDate != null ? selectedDate : new Date();

            // Vérifie si la date est passée
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTime(selectedDate);
            selectedCal.set(Calendar.HOUR_OF_DAY, 0);
            selectedCal.set(Calendar.MINUTE, 0);
            selectedCal.set(Calendar.SECOND, 0);
            selectedCal.set(Calendar.MILLISECOND, 0);

            if (selectedCal.before(today)) {
                Toast.makeText(getContext(), "Impossible de créer un événement sur une date passée", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ouvre NewEventActivity pour la date sélectionnée
            Intent intent = new Intent(getContext(), NewEventActivity.class);
            intent.putExtra("date", Utils.eventDateFormat.format(selectedDate));
            startActivityForResult(intent, ADD_NEW_EVENT_ACTIVITY_REQUEST_CODE);
        });



        loadEventsFromApi();
// Affiche par défaut les events du jour
        updateEventListForDate(selectedDate);
        setUpCalendar();

        return rootView;
    }
    /** Récupération des événements depuis l'API */
    private void loadEventsFromApi() {
        apiService.getEvents(userId).enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EventResponse res = response.body();
                    if (res.success) {
                        events.clear();
                        events.addAll(res.events);
                        Utils.sortEventsByDate(events);
                        setUpCalendar();
                        updateEventListForDate(selectedDate != null ? selectedDate : new Date());
                    }
                }
            }

            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                Log.v("CalendarFragment", t.getMessage());
                Toast.makeText(getActivity(), "Erreur récupération événements", Toast.LENGTH_SHORT).show();
                setUpCalendar();
            }
        });
    }

    /** Filtrage des événements pour une date donnée */
    private void updateEventListForDate(Date date) {
        List<Event> eventsForDate = filterEventsByDate(events, date);

        if (eventsForDate.isEmpty()) {
            eventsRecyclerView.setVisibility(View.GONE);
            noEventsTextView.setVisibility(View.VISIBLE);
        } else {
            eventsRecyclerView.setVisibility(View.VISIBLE);
            noEventsTextView.setVisibility(View.GONE);
            eventsRecyclerView.setHasFixedSize(true);
            eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            EventListAdapter adapter = new EventListAdapter(getContext(), eventsForDate);
            eventsRecyclerView.setAdapter(adapter);
        }
    }

    /** Filtrage multi-journée et gestion des horaires */
    public static List<Event> filterEventsByDate(List<Event> events, Date date) {
        List<Event> result = new ArrayList<>();
        Calendar currentCal = Calendar.getInstance();
        currentCal.setTime(date);
        currentCal.set(Calendar.HOUR_OF_DAY, 0);
        currentCal.set(Calendar.MINUTE, 0);
        currentCal.set(Calendar.SECOND, 0);
        currentCal.set(Calendar.MILLISECOND, 0);

        SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        for (Event e : events) {
            try {
                Date start = sdfFull.parse(e.dateDebut);
                Date end = e.dateFin != null ? sdfFull.parse(e.dateFin) : start;

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

                if (!currentCal.before(startCal) && !currentCal.after(endCal)) {
                    result.add(e);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    /** Remplit le calendrier et configure le GridAdapter */
    public void setUpCalendar() {
        currentDateTextView.setText(Utils.dateFormat.format(calendar.getTime()));
        dates.clear();

        Calendar monthCalendar = (Calendar) calendar.clone();
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfMonth = monthCalendar.get(Calendar.DAY_OF_WEEK) - 2; // lundi = 0
        monthCalendar.add(Calendar.DAY_OF_MONTH, -firstDayOfMonth);

        while (dates.size() < Utils.MAX_CALENDAR_DAYS) {
            dates.add(monthCalendar.getTime());
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        gridAdapter = new GridAdapter(getContext(), dates, calendar, userId, events, selectedDate);
        datesGridView.setAdapter(gridAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == ADD_NEW_EVENT_ACTIVITY_REQUEST_CODE || requestCode == EDIT_EVENT_ACTIVITY_REQUEST_CODE)
                && getActivity() != null && resultCode == getActivity().RESULT_OK) {
            loadEventsFromApi(); // recharge les événements depuis API
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEventsFromApi();
    }
}
