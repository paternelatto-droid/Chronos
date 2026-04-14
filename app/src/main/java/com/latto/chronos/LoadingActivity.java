package com.latto.chronos;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.latto.chronos.api.ApiClient;
import com.latto.chronos.api.ApiService;
import com.latto.chronos.utils.SessionManager;
import com.latto.chronos.views.ConnectionErrorActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoadingActivity extends AppCompatActivity {
    private boolean serverConnectionFailed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading);

        SessionManager session = new SessionManager(getApplicationContext());

        // 🔹 Ping serveur
        ApiService api = ApiClient.getService(this);
        api.pingServer().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                serverConnectionFailed = !response.isSuccessful();
                proceed(session);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                serverConnectionFailed = true;
                Log.e("LoadingActivity", "Ping server failed", t);
                proceed(session);
            }
        });

    }

    private void proceed(SessionManager session) {
        new Handler(getMainLooper()).postDelayed(() -> {
            if (session.isLoggedIn()) {
                if (serverConnectionFailed) {
                    startActivity(new Intent(getApplicationContext(), ConnectionErrorActivity.class));
                } else {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            } else {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
            finish();
        }, 1000); // léger délai pour afficher le loading
    }

}