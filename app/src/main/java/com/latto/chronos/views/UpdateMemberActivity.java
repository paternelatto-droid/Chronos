package com.latto.chronos.views;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.latto.chronos.R;
import com.latto.chronos.api.ApiClient;
import com.latto.chronos.api.ApiService;
import com.latto.chronos.models.MemberWithUserRequest;
import com.latto.chronos.models.Role;
import com.latto.chronos.response.MemberWithUserGetResponse;
import com.latto.chronos.response.MemberWithUserResponse;
import com.latto.chronos.response.RoleResponse;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateMemberActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etPhone, etEmail, etAddress, etDateOfBirth, etBaptismDate;
    private EditText inputUsername, inputPassword;
    private RadioGroup rgGender;
    private SwitchCompat switchCreateUser;
    private Spinner spinnerRoles;
    private LinearLayout layoutUserFields;
    private Button btnSave;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private int memberId;
    private int existingUserId = -1; // -1 si aucun user
    private boolean isUserActive = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member); // réutilisation layout

        Toolbar toolbar = findViewById(R.id.toolbarAddMember);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Récupérer memberId passé depuis liste
        memberId = getIntent().getIntExtra("member_id", 0);
        if (memberId == 0) {
            Toast.makeText(this, "Member ID invalide", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Bind views
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        etBaptismDate = findViewById(R.id.etBaptismDate);
        rgGender = findViewById(R.id.rgGender);
        btnSave = findViewById(R.id.btnSaveMember);
        switchCreateUser = findViewById(R.id.switchCreateUser);
        layoutUserFields = findViewById(R.id.layoutUserFields);
        inputUsername = findViewById(R.id.inputUsername);
        inputPassword = findViewById(R.id.inputPassword);
        spinnerRoles = findViewById(R.id.spinnerRoles);

        // Date pickers
        etDateOfBirth.setOnClickListener(v -> showDatePicker(etDateOfBirth));
        etBaptismDate.setOnClickListener(v -> showDatePicker(etBaptismDate));

        loadRoles();
        loadMemberData();

        switchCreateUser.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutUserFields.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        btnSave.setOnClickListener(v -> confirmUpdate());
    }

    private void showDatePicker(EditText target) {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) ->
                        target.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void loadRoles() {
        ApiService api = ApiClient.getService(this);
        api.getRoles().enqueue(new Callback<RoleResponse>() {
            @Override
            public void onResponse(Call<RoleResponse> call, Response<RoleResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Role> roles = response.body().getRoles();
                    ArrayAdapter<Role> adapter = new ArrayAdapter<>(getApplicationContext(),
                            android.R.layout.simple_spinner_item, roles);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerRoles.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<RoleResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Impossible de charger les rôles", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMemberData() {
        ApiService api = ApiClient.getService(this);
        api.getMemberWithUser(memberId).enqueue(new Callback<MemberWithUserGetResponse>() {
            @Override
            public void onResponse(Call<MemberWithUserGetResponse> call, Response<MemberWithUserGetResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Charger données membre
                    var member = response.body().getMember();
                    etFirstName.setText(member.getFirstName());
                    etLastName.setText(member.getLastName());
                    etPhone.setText(member.getPhone());
                    etEmail.setText(member.getEmail());
                    etAddress.setText(member.getAddress());
                    etDateOfBirth.setText(member.getDateOfBirth());
                    etBaptismDate.setText(member.getBaptismDate());
                    if ("M".equals(member.getGender())) rgGender.check(R.id.rbMale);
                    else rgGender.check(R.id.rbFemale);

                    // Charger user si existant
                    var user = response.body().getUser();
                    if (user != null) {
                        existingUserId = user.getId();
                        inputUsername.setText(user.getUsername());
                        spinnerRoles.setSelection(getRolePosition(user.getRoleId()));
                        switchCreateUser.setChecked(true);
                        isUserActive = user.isActive() == 1;
                    } else {
                        switchCreateUser.setChecked(false);
                        layoutUserFields.setVisibility(View.GONE);
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Erreur serveur", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<MemberWithUserGetResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Connexion échouée: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private int getRolePosition(int roleId) {
        ArrayAdapter<Role> adapter = (ArrayAdapter<Role>) spinnerRoles.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).getId() == roleId) return i;
        }
        return 0;
    }

    private void confirmUpdate() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Voulez-vous enregistrer les modifications ?")
                .setPositiveButton("Oui", (dialog, which) -> updateMember())
                .setNegativeButton("Non", null)
                .show();
    }

    private void updateMember() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String dob = etDateOfBirth.getText().toString().trim();
        String baptism = etBaptismDate.getText().toString().trim();
        String gender = rgGender.getCheckedRadioButtonId() == R.id.rbMale ? "M" : "F";

        boolean createUser = switchCreateUser.isChecked();
        String username = inputUsername.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        int selectedRoleId = ((Role) spinnerRoles.getSelectedItem()).getId();

        MemberWithUserRequest request = new MemberWithUserRequest(
                memberId,
                firstName, lastName, gender,
                phone, email, address,
                dob, baptism,"",
                createUser, username,
                password, selectedRoleId,
                existingUserId,
                isUserActive
        );
        btnSave.setEnabled(false);
        ApiService api = ApiClient.getService(this);
        api.updateMemberWithUser(request).enqueue(new Callback<MemberWithUserResponse>() {
            @Override
            public void onResponse(Call<MemberWithUserResponse> call, Response<MemberWithUserResponse> response) {
                btnSave.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getApplicationContext(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Erreur serveur", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MemberWithUserResponse> call, Throwable t) {
                btnSave.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Connexion échouée: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
