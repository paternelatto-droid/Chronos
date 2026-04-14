package com.latto.chronos.views;


import static com.latto.chronos.Utils.convertToMySQLDatetime;

import android.annotation.SuppressLint;
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
import com.latto.chronos.adapters.NotificationAdapter;
import com.latto.chronos.adapters.ReminderAdapter;
import com.latto.chronos.api.ApiClient;
import com.latto.chronos.api.ApiService;
import com.latto.chronos.models.Event;
import com.latto.chronos.models.EventType;
import com.latto.chronos.models.NotificationModel;
import com.latto.chronos.response.SimpleResp;
import com.latto.chronos.models.User;

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

public class NewEventActivity extends AppCompatActivity {
    private Button btnAddReminder;
    private RecyclerView recyclerReminders;
    private ReminderAdapter adapter;
    private List<Integer> reminderList = new ArrayList<>();
    private ProgressDialog progressDialog;
    private final int[] reminderOptions = {5, 10, 15, 30, 60, 120, 1440}; // en minutes
    private final String[] reminderTexts = {
            "5 minutes avant", "10 minutes avant", "15 minutes avant",
            "30 minutes avant", "1 heure avant", "2 heures avant", "1 jour avant"
    };

    private boolean[] checkedItems = new boolean[reminderOptions.length];


    private Toolbar toolbar;
    private TextInputLayout eventTitleTextInputLayout, eventNoteTextInputLayout, eventLocationTextInputLayout;
    //private SwitchCompat allDayEventSwitch;
    private LinearLayout setDateLinearLayout, setEndDateLinearLayout, containerUserSelection;
    private TextView setDateTextView, setEndDateTextView, setTimeTextView, pickNoteColorTextView;
    private RecyclerView notificationsRecyclerView;
    private RadioGroup radioGroupVisibility;
    private RadioButton radioPublic, radioPrivate, radioRestricted;
    private Button buttonSelectUsers;
    private TextView textViewParticipantsCount, textViewParticipants;

    private Spinner spinnerEventType;
    private List<EventType> eventTypes = new ArrayList<>();
    private EventTypeAdapter eventTypeAdapter;
    private Event event;
    private List<NotificationModel> notificationModels;
    private NotificationAdapter notificationAdapter;
    private int notColor;
    private String dbDateStart, dbEndDate;
    private int alarmYear, alarmMonth, alarmDay, alarmHour, alarmMinute;
    private List<User> allUsers = new ArrayList<>();
    private List<Integer> selectedParticipantIds = new ArrayList<>();
    private AlertDialog notificationAlertDialog, repetitionAlertDialog;
    private ApiService api;
    int selectedEventTypeId;
    int requiresPastor;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        api = ApiClient.getService(this);

        event = new Event();
        notificationModels = new ArrayList<>();

        defineViews();
        initViews();
        initVariables();
        defineListeners();
        handleVisibilityLogic();
        setSupportActionBar(toolbar);
        showReminderSection();

        // Couleur par défaut
        if (notColor == 0) {
            notColor = getResources().getColor(R.color.colorPrimary);
            GradientDrawable bgShape = (GradientDrawable) pickNoteColorTextView.getBackground();
            bgShape.setColor(notColor);
        }

        // Charger event types (API -> fallback)
        eventTypes = new ArrayList<>();
        eventTypeAdapter = new EventTypeAdapter(this, eventTypes);
        spinnerEventType.setAdapter(eventTypeAdapter);
        loadEventTypes();
    }

    private void showReminderSection() {
        btnAddReminder = findViewById(R.id.btnAddReminder);
        recyclerReminders = findViewById(R.id.recyclerReminders);

        adapter = new ReminderAdapter(this, reminderList, position -> {
            reminderList.remove(position);
            adapter.notifyDataSetChanged();
        });
        recyclerReminders.setLayoutManager(new LinearLayoutManager(this));
        recyclerReminders.setAdapter(adapter);

        btnAddReminder.setOnClickListener(v -> showReminderDialog());
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
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    // Méthode pour récupérer la liste JSON à envoyer à l'API
    public List<Integer> getSelectedReminders() {
        return new ArrayList<>(reminderList);
    }

    private void initVariables() {
        Calendar mCal = Calendar.getInstance();
        mCal.setTimeZone(TimeZone.getDefault());
        alarmHour = mCal.get(Calendar.HOUR_OF_DAY);
        alarmMinute = mCal.get(Calendar.MINUTE);

        try {
            mCal.setTime(Utils.eventDateFormat.parse(getIntent().getStringExtra("date")));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        alarmYear = mCal.get(Calendar.YEAR);
        alarmMonth = mCal.get(Calendar.MONTH);
        alarmDay = mCal.get(Calendar.DAY_OF_MONTH);

    }

    private void defineViews() {
        toolbar = findViewById(R.id.AddNewEventActivity_Toolbar);
        eventTitleTextInputLayout = findViewById(R.id.AddNewEventActivity_TextInputLayout_EventTitle);
        eventNoteTextInputLayout = findViewById(R.id.AddNewEventActivity_TextInputLayout_Note);
        eventLocationTextInputLayout = findViewById(R.id.AddNewEventActivity_TextInputLayout_Location);

        //allDayEventSwitch = findViewById(R.id.AddNewEventActivity_Switch_AllDayEvent);
        setDateLinearLayout = findViewById(R.id.AddNewEventActivity_LinearLayout_SetDate);
        setEndDateLinearLayout = findViewById(R.id.AddNewEventActivity_LinearLayout_SetEndDate);

        setDateTextView = findViewById(R.id.AddNewEventActivity_TexView_SetDate);
        setEndDateTextView = findViewById(R.id.AddNewEventActivity_TextView_SetEndDate);

        pickNoteColorTextView = findViewById(R.id.AddNewEventActivity_TextView_PickNoteColor);

        notificationsRecyclerView = findViewById(R.id.recyclerReminders);

        radioGroupVisibility = findViewById(R.id.radioGroupVisibility);
        radioPublic = findViewById(R.id.radioPublic);
        radioPrivate = findViewById(R.id.radioPrivate);
        radioRestricted = findViewById(R.id.radioRestricted);

        containerUserSelection = findViewById(R.id.containerUserSelection);
        buttonSelectUsers = findViewById(R.id.buttonSelectUsers);
        textViewParticipantsCount = findViewById(R.id.textViewParticipantsCount);
        textViewParticipants = findViewById(R.id.textViewParticipants);

        spinnerEventType = findViewById(R.id.spinnerEventType);

    }

    @SuppressLint("SimpleDateFormat")
    private void initViews() {
        // Date initiale
        Calendar calendar = Calendar.getInstance();
        String formattedDisplay = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.FRENCH).format(calendar.getTime());
        setDateTextView.setText(formattedDisplay);
        dbDateStart = formattedDisplay;
        dbEndDate = formattedDisplay;

        // Couleur par défaut
        GradientDrawable bgShape = (GradientDrawable) pickNoteColorTextView.getBackground();
        bgShape.setColor(getResources().getColor(R.color.colorPrimary));

        // Préparer adapter spinner vide (sera rempli après appel API)
        eventTypeAdapter = new EventTypeAdapter(this, eventTypes);
        spinnerEventType.setAdapter(eventTypeAdapter);

        spinnerEventType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= eventTypes.size()) return;
                EventType chosen = eventTypes.get(position);
                // Assigner l'id au event pour envoi
                event.setEventTypeId(chosen.getId());
                // Si has_duration == 1 => afficher dateFin, sinon le cacher
                boolean hasDuration = chosen.getHasDuration() == 1;
                setEndDateLinearLayout.setVisibility(hasDuration ? View.VISIBLE : View.GONE);
                // Si on cache dateFin, on peut réinitialiser dbEndDate -> dbDate
                if (!hasDuration) {
                    dbEndDate = null;
                    setEndDateTextView.setText("");
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

    }

    private void defineListeners() {

        setDateLinearLayout.setOnClickListener(this::setDateStart);
        setEndDateLinearLayout.setOnClickListener(this::setDateEnd);

        pickNoteColorTextView.setOnClickListener(this::pickNoteColor);

        radioGroupVisibility.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioRestricted) containerUserSelection.setVisibility(View.VISIBLE);
            else containerUserSelection.setVisibility(View.GONE);

            buttonSelectUsers.setOnClickListener(v -> openUserSelectionDialog());
        });
    }
    private void openUserSelectionDialog() {
        Intent intent = new Intent(this, UserSelectionActivity.class);
        intent.putIntegerArrayListExtra(UserSelectionActivity.EXTRA_SELECTED_USER_IDS,
                new ArrayList<>(selectedParticipantIds));
        // NOUVEAU : indiquer si un pasteur est requis
        intent.putExtra("requires_pastor", requiresPastor);
        intent.putExtra("event_date", dbDateStart); // pour vérifier la disponibilité

        startActivityForResult(intent, UserSelectionActivity.REQUEST_CODE_USER_SELECTION);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UserSelectionActivity.REQUEST_CODE_USER_SELECTION &&
                resultCode == RESULT_OK && data != null) {

            // Récupérer la liste complète des utilisateurs sélectionnés
            List<User> selectedUsersFromActivity =
                    (List<User>) data.getSerializableExtra(UserSelectionActivity.EXTRA_SELECTED_USERS);

            if (selectedUsersFromActivity != null) {
                // Mettre à jour la liste complète
                allUsers.clear();
                allUsers.addAll(selectedUsersFromActivity);

                // Mettre à jour la liste des IDs pour l'envoi à l'API
                selectedParticipantIds.clear();
                for (User u : selectedUsersFromActivity) {
                    selectedParticipantIds.add(u.getId());
                }

                // Mettre à jour l'événement
                if (event != null) {
                    event.setUserIds(new ArrayList<>(selectedParticipantIds));
                }

                // Rafraîchir l'affichage
                updateParticipantsDisplay();
            }
        }
    }

    private void updateParticipantsDisplay() {
        int selectedCount = selectedParticipantIds.size();

        // Mettre à jour le compteur
        textViewParticipantsCount.setText(selectedCount + " participant(s) sélectionné(s)");

        // Mettre à jour la liste des participants
        if (selectedCount == 0) {
            textViewParticipants.setText("Aucun participant sélectionné");
            textViewParticipants.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        } else {
            // Créer une liste des noms des participants sélectionnés
            StringBuilder participantNames = new StringBuilder();
            int displayedCount = 0;
            int maxDisplay = 3; // Nombre maximum de noms à afficher

            for (User user : allUsers) {
                if (selectedParticipantIds.contains(user.getId())) {
                    if (displayedCount > 0) {
                        participantNames.append(", ");
                    }
                    participantNames.append(user.getUsername());
                    displayedCount++;

                    if (displayedCount >= maxDisplay) {
                        break;
                    }
                }
            }

            // Ajouter "et X autres" si nécessaire
            if (selectedCount > maxDisplay) {
                int remainingCount = selectedCount - maxDisplay;
                participantNames.append(" et ").append(remainingCount).append(" autre(s)");
            }

            textViewParticipants.setText(participantNames.toString());
            textViewParticipants.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
    }

    private void loadEventTypes() {
        api.getEventTypes().enqueue(new Callback<List<EventType>>() {
            @Override
            public void onResponse(Call<List<EventType>> call, Response<List<EventType>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes.clear();
                    eventTypes.addAll(response.body());
                    eventTypeAdapter.notifyDataSetChanged();
                    event.setEventTypeId(eventTypes.get(0).getId());//defaut
                } else {
                    Log.e("TAG", "Response error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<EventType>> call, Throwable t) {
                Log.e("TAG", "loadEventTypes failure", t);
            }
        });


        // Listener pour récupérer la sélection
        spinnerEventType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                EventType selected = eventTypes.get(position);

                // Afficher ou cacher la date de fin selon has_duration
                if (selected.getHasDuration() == 1) {
                    setEndDateLinearLayout.setVisibility(View.VISIBLE);
                } else {
                    setEndDateLinearLayout.setVisibility(View.GONE);
                }

                // Récupérer l'ID de l'événement
                selectedEventTypeId = selected.getId();

                requiresPastor = selected.getRequiresPastor();


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setEndDateLinearLayout.setVisibility(View.GONE);
            }
        });


    }

    private void handleVisibilityLogic() {
        if (radioGroupVisibility.getCheckedRadioButtonId() == R.id.radioRestricted) containerUserSelection.setVisibility(View.VISIBLE);
        else containerUserSelection.setVisibility(View.GONE);
    }

    public void setDateStart(View view) {
        Calendar calendar = Calendar.getInstance();

        // 1️⃣ Sélection de la date
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (dp, year, month, day) -> {
            Calendar aCal = Calendar.getInstance();
            aCal.set(year, month, day);

            // 2️⃣ Après avoir choisi la date, on ouvre le TimePickerDialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (tp, hourOfDay, minute) -> {
                aCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                aCal.set(Calendar.MINUTE, minute);

                // Affichage formaté pour l'utilisateur
                setDateTextView.setText(new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.FRENCH).format(aCal.getTime()));

                // Format pour la base de données (ISO)
                dbDateStart = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.US).format(aCal.getTime());

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

            timePickerDialog.show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }


    public void setDateEnd(View view) {
        Calendar calendar = Calendar.getInstance();

        // 1️⃣ Sélection de la date
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (dp, year, month, day) -> {
            Calendar aCal = Calendar.getInstance();
            aCal.set(year, month, day);

            // 2️⃣ Après avoir choisi la date, on ouvre le TimePickerDialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (tp, hourOfDay, minute) -> {
                aCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                aCal.set(Calendar.MINUTE, minute);

                // Affichage formaté pour l'utilisateur
                setEndDateTextView.setText(new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.FRENCH).format(aCal.getTime()));

                // Format pour la base de données (ISO)
                dbEndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(aCal.getTime());

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

            timePickerDialog.show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }


    public void pickNoteColor(View view) {
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

    private void getViewValues() {
        event.setUserId(Utils.getCurrentUserId(this));
        event.setTitle(eventTitleTextInputLayout.getEditText().getText().toString());
        event.setDateDebut(convertToMySQLDatetime(dbDateStart));
        event.setDateFin(convertToMySQLDatetime(dbEndDate) != null ? convertToMySQLDatetime(dbEndDate) : convertToMySQLDatetime(dbDateStart));
        event.setDescription(eventNoteTextInputLayout.getEditText().getText().toString());
        event.setColor(notColor);
        event.setLocation(eventLocationTextInputLayout.getEditText().getText().toString());
        event.setVisibility(getVisibilityFromRadioButton());
        event.setReminders(getSelectedReminders()); // setter à ajouter dans ton modèle Event
    }

    private String getVisibilityFromRadioButton() {
        int id = radioGroupVisibility.getCheckedRadioButtonId();
        if (id == R.id.radioPublic) return "public";
        if (id == R.id.radioPrivate) return "private";
        return "restricted";
    }

    private boolean confirmInputs() {
        if (eventTitleTextInputLayout.getEditText().getText().toString().isEmpty()) {
            eventTitleTextInputLayout.setError("Entrez le titre de l'événement");
            return false;
        }
        eventTitleTextInputLayout.setError(null);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.ToolBar_Item_Save) {
            getViewValues();
            if (!confirmInputs()) return true;
            saveEventToApi();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveEventToApi() {

        // Définir user_id pour le créateur


        // Si visibility = restricted, assure que event.setUserIds(...) est rempli
        if ("restricted".equals(event.getVisibility()) && event.getUserIds() == null) {
            event.setUserIds(new ArrayList<>()); // vide si aucun utilisateur choisi
        }


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sauvegarde en cours...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Call<SimpleResp> call = api.createEvent(event);
        call.enqueue(new Callback<SimpleResp>() {
            @Override
            public void onResponse(@NonNull Call<SimpleResp> call, @NonNull Response<SimpleResp> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    int eventId = response.body().getId();
                    Toast.makeText(NewEventActivity.this, "Événement enregistré", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String msg = "Erreur enregistrement";
                    if (response.errorBody() != null) {
                        try {
                            msg = response.errorBody().string();
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(NewEventActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SimpleResp> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(NewEventActivity.this, "Impossible de contacter le serveur: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


}
