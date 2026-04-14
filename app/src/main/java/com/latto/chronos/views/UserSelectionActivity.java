package com.latto.chronos.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.latto.chronos.R;
import com.latto.chronos.adapters.UserSelectionAdapter;
import com.latto.chronos.api.ApiClient;
import com.latto.chronos.api.ApiService;
import com.latto.chronos.response.AvailabilityResponse;
import com.latto.chronos.models.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserSelectionActivity extends AppCompatActivity {
    public static final String EXTRA_SELECTED_USER_IDS = "extra_selected_user_ids";
    public static final String EXTRA_SELECTED_USERS = "extra_selected_users";
    public static final int REQUEST_CODE_USER_SELECTION = 1001;

    private ApiService apiService;
    private Toolbar toolbar;
    private TextInputEditText etSearchUsers;
    private RecyclerView rvUsers;
    private TextView tvSelectedCount, tvClearSelection;
    private LinearLayout layoutNoUsers, layoutLoading;
    private Button btnCancel, btnValidate;

    private List<User> allUsers = new ArrayList<>();
    private List<User> selectedUsers = new ArrayList<>();
    private List<Integer> selectedUserIds = new ArrayList<>();
    private UserSelectionAdapter adapter;

    private int requiresPastor = 0;
    private String eventDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_selection);

        apiService = ApiClient.getService(this);

        // Récupération du flag et de la date
        requiresPastor = getIntent().getIntExtra("requires_pastor", 0);
        eventDate = getIntent().getStringExtra("event_date");

        initViews();
        setupAdapter();
        setupListeners();

        loadUsers();
        updateSelectedCount();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etSearchUsers = findViewById(R.id.et_search_users);
        rvUsers = findViewById(R.id.rv_users);
        tvSelectedCount = findViewById(R.id.tv_selected_count);
        tvClearSelection = findViewById(R.id.tv_clear_selection);
        layoutNoUsers = findViewById(R.id.layout_no_users);
        layoutLoading = findViewById(R.id.layout_loading);
        btnCancel = findViewById(R.id.btn_cancel);
        btnValidate = findViewById(R.id.btn_validate);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        selectedUserIds = getIntent().getIntegerArrayListExtra(EXTRA_SELECTED_USER_IDS);
        if (selectedUserIds == null) selectedUserIds = new ArrayList<>();
    }

    private void setupAdapter() {
        adapter = new UserSelectionAdapter(this, allUsers, selectedUserIds, () -> {
            rebuildSelectedUsers();
            updateSelectedCount();
        });
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);
    }

    private void setupListeners() {
        tvClearSelection.setOnClickListener(v -> {
            selectedUserIds.clear();
            selectedUsers.clear();
            adapter.clearSelection();
            updateSelectedCount();
        });

        etSearchUsers.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) { filterUsers(s.toString()); }
            @Override public void afterTextChanged(Editable editable) {}
        });

        btnCancel.setOnClickListener(v -> finish());

        btnValidate.setOnClickListener(v -> {
            Intent data = new Intent();
            data.putIntegerArrayListExtra(EXTRA_SELECTED_USER_IDS, new ArrayList<>(selectedUserIds));
            data.putExtra(EXTRA_SELECTED_USERS, (Serializable) new ArrayList<>(selectedUsers));
            setResult(RESULT_OK, data);
            finish();
        });
    }

    private void loadUsers() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutNoUsers.setVisibility(View.GONE);
        rvUsers.setVisibility(View.GONE);

        apiService.getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                layoutLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allUsers.clear();
                    List<User> loadedUsers = response.body();
                    List<User> validUsers = new ArrayList<>();
                    AtomicInteger processedCount = new AtomicInteger(0);

                    if (requiresPastor == 1) {
                        // Filtrer les pasteurs (role_id = 4)
                        List<User> pastors = new ArrayList<>();
                        for (User u : loadedUsers) {
                            if (u.getRoleId() == 4 && u.isActive()) pastors.add(u);
                        }

                        for (User u : pastors) {
                            checkUserAvailability(u.getId(), eventDate, available -> {
                                if (available) validUsers.add(u);
                                if (processedCount.incrementAndGet() == pastors.size()) {
                                    updateUserList(validUsers);
                                }
                            });
                        }
                        // Si aucun pasteur => afficher vide
                        if (pastors.isEmpty()) updateUserList(validUsers);

                    } else {
                        // Tous les utilisateurs actifs
                        for (User u : loadedUsers) {
                            if (u.isActive()) validUsers.add(u);
                        }
                        updateUserList(validUsers);
                    }

                } else {
                    Toast.makeText(UserSelectionActivity.this, "Erreur lors du chargement des utilisateurs", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                layoutLoading.setVisibility(View.GONE);
                Toast.makeText(UserSelectionActivity.this, "Erreur API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserList(List<User> users) {
        allUsers.clear();
        allUsers.addAll(users);
        rebuildSelectedUsers();

        if (allUsers.isEmpty()) layoutNoUsers.setVisibility(View.VISIBLE);
        else {
            rvUsers.setVisibility(View.VISIBLE);
            adapter.updateUsers(allUsers);
        }
    }

    private void checkUserAvailability(int userId, String eventDate, AvailabilityCallback callback) {
        apiService.isUserAvailable(userId, eventDate).enqueue(new Callback<AvailabilityResponse>() {
            @Override
            public void onResponse(@NonNull Call<AvailabilityResponse> call, @NonNull Response<AvailabilityResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(response.body().isAvailable());
                } else callback.onResult(false);
            }

            @Override
            public void onFailure(@NonNull Call<AvailabilityResponse> call, @NonNull Throwable t) {
                callback.onResult(false);
            }
        });
    }

    interface AvailabilityCallback { void onResult(boolean available); }

    private void filterUsers(String query) {
        query = query.toLowerCase();
        List<User> filtered = new ArrayList<>();
        for (User user : allUsers) {
            if (user.getUsername().toLowerCase().contains(query)) filtered.add(user);
        }
        if (filtered.isEmpty()) {
            layoutNoUsers.setVisibility(View.VISIBLE);
            rvUsers.setVisibility(View.GONE);
        } else {
            layoutNoUsers.setVisibility(View.GONE);
            rvUsers.setVisibility(View.VISIBLE);
            adapter.updateUsers(filtered);
        }
    }

    private void rebuildSelectedUsers() {
        selectedUsers.clear();
        for (User u : allUsers) {
            if (selectedUserIds.contains(u.getId())) selectedUsers.add(u);
        }
    }

    private void updateSelectedCount() {
        int count = selectedUserIds.size();
        tvSelectedCount.setText(count + " participant(s) sélectionné(s)");
        tvClearSelection.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }
}
