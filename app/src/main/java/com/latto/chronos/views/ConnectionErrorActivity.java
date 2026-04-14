package com.latto.chronos.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.latto.chronos.LoadingActivity;
import com.latto.chronos.R;
import com.latto.chronos.api.ApiClient;
import com.latto.chronos.api.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConnectionErrorActivity extends AppCompatActivity {

    private Handler handler = new Handler();
    private Runnable countdownRunnable;
    private boolean retrying = false;
    private int countdown = 5;
    private TextView tvCountdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_error);

        TextView tvMessage = findViewById(R.id.tvErrorMessage);
        tvCountdown = findViewById(R.id.tvCountdown);
        Button btnRetry = findViewById(R.id.btnRetry);

        tvMessage.setText("Impossible de se connecter au serveur");

        btnRetry.setOnClickListener(v -> {
            countdown = 5; // reset timer manual retry
            attemptPing();
        });

        startCountdown(); // Démarrage automatique
    }

    private void startCountdown() {
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                if (countdown > 0) {
                    tvCountdown.setText("Nouvelle tentative dans " + countdown + "s...");
                    countdown--;
                    handler.postDelayed(this, 1000);
                } else {
                    if (!retrying) {
                        countdown = 5; // reset timer
                        attemptPing();
                        startCountdown(); // restart countdown
                    }
                }
            }
        };
        handler.post(countdownRunnable);
    }

    private void attemptPing() {
        retrying = true;
        tvCountdown.setText("Vérification du serveur...");

        ApiService api = ApiClient.getService(this);
        api.pingServer().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                retrying = false;
                if (response.isSuccessful()) {
                    startActivity(new Intent(ConnectionErrorActivity.this, LoadingActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                retrying = false;
                Log.e("ConnectionError", "Ping failed", t);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(countdownRunnable);
    }
}
