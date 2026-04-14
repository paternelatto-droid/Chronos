package com.latto.chronos.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.latto.chronos.R;
import com.latto.chronos.Utils;
import com.latto.chronos.adapters.UpcomingEventAdapter;
import com.latto.chronos.api.ApiClient;
import com.latto.chronos.api.ApiService;
import com.latto.chronos.models.Event;
import com.latto.chronos.response.EventResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpcomingEventsFragment extends Fragment {
    private boolean isDataLoaded = false; // ✅ Flag anti-double chargement

    private final String TAG = this.getClass().getSimpleName();
    private ImageButton changePeriodImageButton;
    public TextView periodTextView;
    private RecyclerView eventsRecyclerView;
    private List<Event> events = new ArrayList<>();
    private ApiService apiService;
    private int currentUserId;
    private TextView emptyTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upcoming_events, container, false);

        defineViews(view);
        initViews();
        defineListeners();

        apiService = ApiClient.getService(getContext());
        currentUserId = Utils.getCurrentUserId(getContext()); // tu dois créer cette méthode
        //fetchEventsFromApi();

        return view;
    }
    // ✅ Nouvelle méthode à appeler UNIQUEMENT via MainActivity
    public void loadDataIfNeeded() {
        fetchEventsFromApi();
        /*if (!isDataLoaded) {
            isDataLoaded = true;
            fetchEventsFromApi();
        }*/
    }
    private void defineViews(View view) {
        changePeriodImageButton = view.findViewById(R.id.UpcomingEventsFragment_ImageButton_Period);
        periodTextView = view.findViewById(R.id.UpcomingEventsFragment_TextView_Period);
        eventsRecyclerView = view.findViewById(R.id.UpcomingEventsFragment_RecyclerView_Events);
        emptyTextView = view.findViewById(R.id.UpcomingEventsFragment_TextView_Empty);

    }

    private void initViews() {
        periodTextView.setText(Utils.CURRENT_FILTER);
        setUpRecyclerView();
    }

    private void defineListeners() {
        changePeriodImageButton.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(getActivity(), view);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.popup_period, popup.getMenu());
            popup.setOnMenuItemClickListener(menuItem -> {

                int id = menuItem.getItemId();

                if (id == R.id.PopupPeriod_Item_Today) {
                    Utils.CURRENT_FILTER = Utils.TODAY;
                }
                else if (id == R.id.PopupPeriod_Item_Next7Days) {
                    Utils.CURRENT_FILTER = Utils.NEXT_7_DAYS;
                }
                else if (id == R.id.PopupPeriod_Item_Next30Days) {
                    Utils.CURRENT_FILTER = Utils.NEXT_30_DAYS;
                }

                periodTextView.setText(Utils.CURRENT_FILTER);
                // relancer le filtrage
                filterEvents(Utils.CURRENT_FILTER);


                return true;
            });
            popup.show();
        });
    }


    public void setUpRecyclerView() {
        eventsRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setMeasurementCacheEnabled(false);
        eventsRecyclerView.setLayoutManager(layoutManager);
        UpcomingEventAdapter upcomingEventAdapter = new UpcomingEventAdapter(getActivity(), events, this);
        eventsRecyclerView.setAdapter(upcomingEventAdapter);
    }

    private void fetchEventsFromApi() {
        apiService.getEvents(currentUserId).enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EventResponse res = response.body();
                    events.clear();
                    events.addAll(res.events);
                    filterEvents(Utils.TODAY); // appliquer le filtre courant
                } else {
                    Log.d(TAG, "Failed to fetch events, response not successful");
                    Toast.makeText(getContext(), "Échec de la récupération des événements !", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                Log.e(TAG, "API call error: ", t);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void filterEvents(String CURRENT_FILTER) {
        List<Event> filteredEvents = new ArrayList<>();
        Calendar today = Calendar.getInstance();
        Date now = today.getTime();

        // Formatter la date pour correspondre au format de e.getDate()
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        now = Utils.parseDate(sdf.format(now)); // maintenant à minuit, même format que e.getDate()


        switch (CURRENT_FILTER) {
            case Utils.TODAY:
                for (Event e : events) {
                    if (Utils.isSameDay(Utils.parseDate(e.getDateDebut()), now)) filteredEvents.add(e);
                }
                break;
            case Utils.NEXT_7_DAYS:
                Calendar next7 = (Calendar) today.clone();
                next7.add(Calendar.DAY_OF_MONTH, 7);
                for (Event e : events) {
                    Date eventDate = Utils.parseDate(e.getDateDebut());
                    if (!eventDate.before(now) && !eventDate.after(next7.getTime())) filteredEvents.add(e);
                }
                break;
            case Utils.NEXT_30_DAYS:
                Calendar next30 = (Calendar) today.clone();
                next30.add(Calendar.DAY_OF_MONTH, 30);
                for (Event e : events) {
                    Date eventDate = Utils.parseDate(e.getDateDebut());
                    if (!eventDate.before(now) && !eventDate.after(next30.getTime())) filteredEvents.add(e);
                }
                break;
        }

        // ✅ Gestion d'affichage vide
        if (filteredEvents.isEmpty()) {
            eventsRecyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            eventsRecyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
            eventsRecyclerView.setAdapter(new UpcomingEventAdapter(getActivity(), filteredEvents, this));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            fetchEventsFromApi(); // recharger depuis API
            Toast.makeText(getActivity(), "Evenement mise à jour!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            // Le fragment vient d'être affiché → on applique le filtre actif
            filterEvents(Utils.CURRENT_FILTER);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchEventsFromApi();
    }



}
