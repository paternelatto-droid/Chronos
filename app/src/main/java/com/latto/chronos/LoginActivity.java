package com.latto.chronos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.latto.chronos.api.ApiClient;
import com.latto.chronos.api.ApiService;
import com.latto.chronos.models.LoginRequest;
import com.latto.chronos.models.UserSession;
import com.latto.chronos.response.LoginResponse;
import com.latto.chronos.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUser, etPass;
    private Button btnLogin;
    private ApiService api;
    private ProgressDialog progressDialog;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Init API
        api = ApiClient.getService(this);
        session = new SessionManager(this);

        // Init UI
        etUser = findViewById(R.id.etUser);
        etPass = findViewById(R.id.etPass);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String username = etUser.getText().toString().trim();
        String password = etPass.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest request = new LoginRequest(username, password);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Connexion en cours...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        api.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if (loginResponse.success && loginResponse.user != null) {
                        // Sauvegarde session
                        session.saveSession(
                                loginResponse.user.getId(),
                                loginResponse.user.getUsername(),
                                loginResponse.token,
                                loginResponse.token_expires_in
                        );

                        // Enregistrer permissions pour usage global
                        UserSession.setPermissions(getApplication(),loginResponse.user.getPermissions());

                        Toast.makeText(LoginActivity.this, "Bienvenue " + loginResponse.user.getUsername(), Toast.LENGTH_SHORT).show();

                        // Ouvrir MainActivity
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Nom d'utilisateur ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Erreur: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Impossible de contacter le serveur", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
