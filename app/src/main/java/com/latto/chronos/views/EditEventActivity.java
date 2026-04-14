package com.latto.chronos.views;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.latto.chronos.R;
import com.latto.chronos.Utils;
import com.latto.chronos.adapters.EventTypeAdapter;
import com.latto.chronos.adapters.ReminderAdapter;
import com.latto.chronos.api.ApiClient;
import com.latto.chronos.api.ApiService;
import com.latto.chronos.models.Event;
import com.latto.chronos.models.EventType;
import com.latto.chronos.response.SimpleResp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditEventActivity extends AppCompatActivity {

    public static final int RESULT_UPDATED = 200;

    private Toolbar toolbar;
    private TextInputLayout eventTitleTextInputLayout, eventNoteTextInputLayout, eventLocationTextInputLayout;
    private LinearLayout setDateLinearLayout, setEndDateLinearLayout, containerUserSelection;
    private TextView setDateTextView, setEndDateTextView, pickNoteColorTextView;
    private RecyclerView recyclerReminders;
    private ReminderAdapter reminderAdapter;
    private List<Integer> reminderList = new ArrayList<>();
    private boolean[] checkedItems;
    private int notColor;

    private Spinner spinnerEventType;
    private List<EventType> eventTypes = new ArrayList<>();
    private EventTypeAdapter eventTypeAdapter;

    private RadioGroup radioGroupVisibility;
    private RadioButton radioPublic, radioPrivate, radioRestricted;
    private Button buttonSelectUsers;
    private TextView textViewParticipantsCount, textViewParticipants;

    private ApiService api;
    private Event event; // objet à éditer
    private ProgressDialog progressDialog;

    // variables date/time internal
    private String dbDateStart, dbEndDate;
    private int selectedEventTypeId;
    private int requiresPastor;

    // reminders options (same as NewEventActivity)
    private final int[] reminderOptions = {5, 10, 15, 30, 60, 120, 1440};
    private final String[] reminderTexts = {
            "5 minutes avant", "10 minutes avant", "15 minutes avant",
            "30 minutes avant", "1 heure avant", "2 heures avant", "1 jour avant"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        api = ApiClient.getService(this);

        // Recevoir l'event passé
        Intent it = getIntent();
        event = (Event) it.getSerializableExtra("event");
        if (event == null) {
            Toast.makeText(this, "Événement introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        defineViews();
        initViews();
        defineListeners();

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Modifier l'événement");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // charger eventTypes et pré-remplir champs
        eventTypeAdapter = new EventTypeAdapter(this, eventTypes);
        spinnerEventType.setAdapter(eventTypeAdapter);
        loadEventTypesAndPrefill();
        showReminderSection();
    }

    private void defineViews() {
        toolbar = findViewById(R.id.AddNewEventActivity_Toolbar);
        eventTitleTextInputLayout = findViewById(R.id.AddNewEventActivity_TextInputLayout_EventTitle);
        eventNoteTextInputLayout = findViewById(R.id.AddNewEventActivity_TextInputLayout_Note);
        eventLocationTextInputLayout = findViewById(R.id.AddNewEventActivity_TextInputLayout_Location);

        setDateLinearLayout = findViewById(R.id.AddNewEventActivity_LinearLayout_SetDate);
        setEndDateLinearLayout = findViewById(R.id.AddNewEventActivity_LinearLayout_SetEndDate);

        setDateTextView = findViewById(R.id.AddNewEventActivity_TexView_SetDate);
        setEndDateTextView = findViewById(R.id.AddNewEventActivity_TextView_SetEndDate);

        pickNoteColorTextView = findViewById(R.id.AddNewEventActivity_TextView_PickNoteColor);

        spinnerEventType = findViewById(R.id.spinnerEventType);

        radioGroupVisibility = findViewById(R.id.radioGroupVisibility);
        radioPublic = findViewById(R.id.radioPublic);
        radioPrivate = findViewById(R.id.radioPrivate);
        radioRestricted = findViewById(R.id.radioRestricted);

        containerUserSelection = findViewById(R.id.containerUserSelection);
        buttonSelectUsers = findViewById(R.id.buttonSelectUsers);
        textViewParticipantsCount = findViewById(R.id.textViewParticipantsCount);
        textViewParticipants = findViewById(R.id.textViewParticipants);

        recyclerReminders = findViewById(R.id.recyclerReminders);
    }

    private void initViews() {
        // Pré-remplir valeurs venant de l'event
        eventTitleTextInputLayout.getEditText().setText(event.getTitle());
        eventNoteTextInputLayout.getEditText().setText(event.getDescription());
        eventLocationTextInputLayout.getEditText().setText(event.getLocation());
        notColor = event.getColor();

        // set color preview
        GradientDrawable bgShape = (GradientDrawable) pickNoteColorTextView.getBackground();
        bgShape.setColor(notColor != 0 ? notColor : getResources().getColor(R.color.colorPrimary));

        // date strings
        dbDateStart = event.getDateDebut(); // format MySQL "yyyy-MM-dd HH:mm:ss"
        dbEndDate = event.getDateFin();

        // display date format for user
        try {
            SimpleDateFormat src = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat dst = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.FRENCH);
            setDateTextView.setText(dst.format(src.parse(dbDateStart)));
            if (dbEndDate != null && !dbEndDate.isEmpty()) {
                setEndDateTextView.setText(dst.format(src.parse(dbEndDate)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // visibility
        if ("public".equals(event.getVisibility())) {
            radioPublic.setChecked(true);
        } else if ("private".equals(event.getVisibility())) {
            radioPrivate.setChecked(true);
        } else {
            radioRestricted.setChecked(true);
            containerUserSelection.setVisibility(View.VISIBLE);
        }

        // reminders (assume Event has getReminders() returning List<Integer>)
        if (event.getReminders() != null) {
            reminderList.clear();
            reminderList.addAll(event.getReminders());
        }
        checkedItems = new boolean[reminderOptions.length];
        for (int i = 0; i < reminderOptions.length; i++) {
            checkedItems[i] = reminderList.contains(reminderOptions[i]);
        }

        // participants count (if Event contains userIds)
        if (event.getUserIds() != null) {
            textViewParticipantsCount.setText(event.getUserIds().size() + " participant(s) sélectionné(s)");
            // text list truncated
            StringBuilder names = new StringBuilder();
            int c = 0;
            for (Integer id : event.getUserIds()) {
                if (c > 0) names.append(", ");
                names.append("ID:").append(id); // si tu veux les noms, il faut charger users via API
                c++;
                if (c >= 3) break;
            }
            if (event.getUserIds().size() > 3) names.append(" et ").append(event.getUserIds().size()-3).append(" autres");
            textViewParticipants.setText(names.toString());
        } else {
            textViewParticipantsCount.setText("0 participant(s) sélectionné(s)");
            textViewParticipants.setText("Aucun participant sélectionné");
            textViewParticipants.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        }
    }

    private void defineListeners() {
        setDateLinearLayout.setOnClickListener(this::setDateStart);
        setEndDateLinearLayout.setOnClickListener(this::setDateEnd);
        pickNoteColorTextView.setOnClickListener(this::pickNoteColor);

        radioGroupVisibility.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioRestricted) containerUserSelection.setVisibility(View.VISIBLE);
            else containerUserSelection.setVisibility(View.GONE);
        });

        buttonSelectUsers.setOnClickListener(v -> openUserSelectionDialog());
    }

    private void openUserSelectionDialog() {
        Intent intent = new Intent(this, UserSelectionActivity.class);
        // pass selected ids if any
        intent.putIntegerArrayListExtra(UserSelectionActivity.EXTRA_SELECTED_USER_IDS, new ArrayList<>(event.getUserIds() != null ? event.getUserIds() : new ArrayList<>()));
        intent.putExtra("requires_pastor", requiresPastor);
        intent.putExtra("event_date", dbDateStart);
        startActivityForResult(intent, UserSelectionActivity.REQUEST_CODE_USER_SELECTION);
    }

    private void showReminderSection() {
        reminderAdapter = new ReminderAdapter(this, reminderList, position -> {
            reminderList.remove(position);
            reminderAdapter.notifyDataSetChanged();
        });
        recyclerReminders.setLayoutManager(new LinearLayoutManager(this));
        recyclerReminders.setAdapter(reminderAdapter);

        findViewById(R.id.btnAddReminder).setOnClickListener(v -> showReminderDialog());
    }

    private void showReminderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choisir les rappels")
                .setMultiChoiceItems(reminderTexts, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked)
                .setPositiveButton("OK", (dialog, id) -> {
                    reminderList.clear();
                    for (int i = 0; i < reminderOptions.length; i++) {
                        if (checkedItems[i]) reminderList.add(reminderOptions[i]);
                    }
                    reminderAdapter.notifyDataSetChanged();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    // date/time pickers (similar to NewEventActivity)
    public void setDateStart(View view) {
        Calendar calendar = Calendar.getInstance();
        try {
            SimpleDateFormat src = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            calendar.setTime(src.parse(dbDateStart));
        } catch (Exception ignored) {}
        DatePickerDialog dp = new DatePickerDialog(this, (dpw, year, month, day) -> {
            Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            TimePickerDialog tp = new TimePickerDialog(this, (tp2, hour, minute) -> {
                c.set(Calendar.HOUR_OF_DAY, hour);
                c.set(Calendar.MINUTE, minute);
                setDateTextView.setText(new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.FRENCH).format(c.getTime()));
                dbDateStart = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(c.getTime());
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            tp.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dp.show();
    }

    public void setDateEnd(View view) {
        Calendar calendar = Calendar.getInstance();
        if (dbEndDate != null) {
            try {
                SimpleDateFormat src = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                calendar.setTime(src.parse(dbEndDate));
            } catch (Exception ignored) {}
        }
        DatePickerDialog dp = new DatePickerDialog(this, (dpw, year, month, day) -> {
            Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            TimePickerDialog tp = new TimePickerDialog(this, (tp2, hour, minute) -> {
                c.set(Calendar.HOUR_OF_DAY, hour);
                c.set(Calendar.MINUTE, minute);
                setEndDateTextView.setText(new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.FRENCH).format(c.getTime()));
                dbEndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(c.getTime());
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            tp.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dp.show();
    }

    public void pickNoteColor(View v) {
        ColorPickerDialogBuilder.with(this)
                .setTitle("Pick a color")
                .initialColor(notColor != 0 ? notColor : getResources().getColor(R.color.colorPrimary))
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton("OK", (dialog, selectedColor, allColors) -> {
                    notColor = selectedColor;
                    GradientDrawable bgShape = (GradientDrawable) pickNoteColorTextView.getBackground();
                    bgShape.setColor(selectedColor);
                })
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss())
                .build()
                .show();
    }

    private void loadEventTypesAndPrefill() {
        api.getEventTypes().enqueue(new Callback<List<EventType>>() {
            @Override
            public void onResponse(Call<List<EventType>> call, Response<List<EventType>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes.clear();
                    eventTypes.addAll(response.body());
                    eventTypeAdapter.notifyDataSetChanged();

                    // sélectionner le type courant
                    for (int i = 0; i < eventTypes.size(); i++) {
                        if (eventTypes.get(i).getId() == event.getEventTypeId()) {
                            spinnerEventType.setSelection(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<EventType>> call, Throwable t) {
                Log.e("EditEventActivity", "loadEventTypes failed", t);
            }
        });

        // spinner listener
        spinnerEventType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedEventTypeId = eventTypes.get(position).getId();
                requiresPastor = eventTypes.get(position).getRequiresPastor();
                // show/hide end date
                boolean hasDuration = eventTypes.get(position).getHasDuration() == 1;
                setEndDateLinearLayout.setVisibility(hasDuration ? View.VISIBLE : View.GONE);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // menu: save => update
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu); // garde la même icône save
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.ToolBar_Item_Save) {
            if (!confirmInputs()) return true;
            collectValuesToEvent();
            doUpdateEvent();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean confirmInputs() {
        if (eventTitleTextInputLayout.getEditText().getText().toString().trim().isEmpty()) {
            eventTitleTextInputLayout.setError("Entrez le titre de l'événement");
            return false;
        }
        eventTitleTextInputLayout.setError(null);
        return true;
    }

    private void collectValuesToEvent() {
        event.setTitle(eventTitleTextInputLayout.getEditText().getText().toString().trim());
        event.setDescription(eventNoteTextInputLayout.getEditText().getText().toString());
        event.setLocation(eventLocationTextInputLayout.getEditText().getText().toString());
        event.setColor(notColor);
        event.setEventTypeId(selectedEventTypeId);
        event.setDateDebut(dbDateStart);
        event.setDateFin(dbEndDate != null ? dbEndDate : dbDateStart);
        event.setReminders(new ArrayList<>(reminderList));
        event.setVisibility(getVisibilityFromRadio());
        // userIds will be managed by UserSelectionActivity result (if changed)
    }

    private String getVisibilityFromRadio() {
        int id = radioGroupVisibility.getCheckedRadioButtonId();
        if (id == R.id.radioPublic) return "public";
        if (id == R.id.radioPrivate) return "private";
        return "restricted";
    }

    private void doUpdateEvent() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mise à jour en cours...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Call<SimpleResp> call = api.updateEvent(event.getId(), event);
        call.enqueue(new Callback<SimpleResp>() {
            @Override
            public void onResponse(Call<SimpleResp> call, Response<SimpleResp> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(EditEventActivity.this, "Événement mis à jour", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_UPDATED);
                    finish();
                } else {
                    String msg = "Erreur lors de la mise à jour";
                    if (response.errorBody() != null) {
                        try { msg = response.errorBody().string(); } catch (Exception ignored) {}
                    }
                    Toast.makeText(EditEventActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SimpleResp> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(EditEventActivity.this, "Impossible de contacter le serveur: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // récupérer résultat sélection utilisateurs
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UserSelectionActivity.REQUEST_CODE_USER_SELECTION && resultCode == RESULT_OK && data != null) {
            List<Integer> ids = data.getIntegerArrayListExtra(UserSelectionActivity.EXTRA_SELECTED_USER_IDS);
            if (ids != null) {
                event.setUserIds(new ArrayList<>(ids));
                // mise à jour affichage participants
                textViewParticipantsCount.setText(ids.size() + " participant(s) sélectionné(s)");
                StringBuilder sb = new StringBuilder();
                int c = 0;
                for (Integer id : ids) {
                    if (c > 0) sb.append(", ");
                    sb.append("ID:").append(id);
                    c++;
                    if (c >= 3) break;
                }
                if (ids.size() > 3) sb.append(" et ").append(ids.size()-3).append(" autres");
                textViewParticipants.setText(sb.toString());
            }
        }
    }
}
