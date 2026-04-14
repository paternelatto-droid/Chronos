package com.latto.chronos.views;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.latto.chronos.R;
import com.latto.chronos.api.ApiClient;
import com.latto.chronos.api.ApiService;
import com.latto.chronos.models.Member;
import com.latto.chronos.models.MemberWithUserRequest;
import com.latto.chronos.models.Role;
import com.latto.chronos.response.MemberWithUserResponse;
import com.latto.chronos.response.RoleResponse;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddMemberActivity extends AppCompatActivity {

    private EditText etFirstName,
            etLastName,
            etPhone,
            etEmail,
            etAddress,
            etDateOfBirth,
            etBaptismDate,
            inputUsername,
            inputPassword;
    private RadioGroup rgGender;

    private Button btnSave;
    SwitchCompat switchCreateUser;
    Spinner spinnerRoles;
    LinearLayout layoutUserFields;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);

        Toolbar toolbar = findViewById(R.id.toolbarAddMember);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

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

        // Charger la liste des rôles depuis l’API
        loadRoles();

        switchCreateUser.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutUserFields.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (isChecked) {
                autoFillUserFields();
            }
        });


        // Save button
        btnSave.setOnClickListener(v -> saveMember());
    }

    private void autoFillUserFields() {
        String first = etFirstName.getText().toString().trim().toLowerCase(Locale.ROOT);
        String last = etLastName.getText().toString().trim().toLowerCase(Locale.ROOT);

        Calendar c = Calendar.getInstance();
        String suffix = String.format(Locale.getDefault(), "%02d%02d%d",
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.YEAR));

        String generatedUsername = first + "." + last + "@" + suffix;
        inputUsername.setText(generatedUsername);
    }

    private void showDatePicker(EditText target) {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> target.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)),
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
                    ArrayAdapter<Role> adapter = new ArrayAdapter<>(AddMemberActivity.this,
                            android.R.layout.simple_spinner_item, roles);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerRoles.setAdapter(adapter);
                } else {
                    Toast.makeText(AddMemberActivity.this, "Erreur lors du chargement des rôles", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RoleResponse> call, Throwable t) {
                Toast.makeText(AddMemberActivity.this, "Impossible de charger les rôles", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void saveMember() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String dob = etDateOfBirth.getText().toString().trim();
        String baptism = etBaptismDate.getText().toString().trim();
        String gender = rgGender.getCheckedRadioButtonId() == R.id.rbMale ? "M" : "F";
        //String status = spinnerStatus.getSelectedItem().toString().toLowerCase();
        boolean createUser = switchCreateUser.isChecked();
        String username = inputUsername.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        int selectedRoleId = ((Role) spinnerRoles.getSelectedItem()).getId();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Prénom et nom sont requis", Toast.LENGTH_SHORT).show();
            return;
        }

        MemberWithUserRequest memberWithUserRequest = new MemberWithUserRequest(
                firstName, lastName, gender, phone, email, address, dob, baptism,"",
                createUser ? true : false,
                username,
                password,
                selectedRoleId
        );
        ApiService api = ApiClient.getService(this);
        btnSave.setEnabled(false);

        api.createMemberWithUser(memberWithUserRequest).enqueue(new Callback<MemberWithUserResponse>() {
            @Override
            public void onResponse(Call<MemberWithUserResponse> call, Response<MemberWithUserResponse> response) {
                btnSave.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    MemberWithUserResponse res = response.body();
                    Toast.makeText(AddMemberActivity.this, res.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(AddMemberActivity.this, "Erreur serveur", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MemberWithUserResponse> call, Throwable t) {
                btnSave.setEnabled(true);
                Toast.makeText(AddMemberActivity.this, "Connexion échouée: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
