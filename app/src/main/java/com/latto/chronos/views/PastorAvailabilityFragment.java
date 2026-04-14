package com.latto.chronos.views;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.latto.chronos.R;
import com.latto.chronos.adapters.AvailabilityAdapter;
import com.latto.chronos.api.ApiClient;
import com.latto.chronos.api.ApiService;
import com.latto.chronos.models.IdRequest;
import com.latto.chronos.models.PastorAvailability;
import com.latto.chronos.models.PastorAvailabilityRequest;
import com.latto.chronos.response.AvailabilityResponse;
import com.latto.chronos.response.PastorAvailabilityResponse;
import com.latto.chronos.response.SimpleResp;
import com.latto.chronos.utils.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PastorAvailabilityFragment extends Fragment {

    private RecyclerView rv;
    private AvailabilityAdapter adapter;
    private List<PastorAvailability> items = new ArrayList<>();
    private ApiService api;
    private int userId;
    private FloatingActionButton fab;

    public PastorAvailabilityFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_pastor_availability, container, false);
        rv = v.findViewById(R.id.rvAvailability);
        fab = v.findViewById(R.id.fabAdd);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AvailabilityAdapter(getContext(), items, new AvailabilityAdapter.Callback() {
            @Override public void onDelete(PastorAvailability item) { confirmDelete(item); }
            @Override public void onItemClick(PastorAvailability item) { /* possible edit later */ }
        });
        rv.setAdapter(adapter);
        api = ApiClient.getService(getContext());
        SessionManager sm = new SessionManager(getContext());
        userId = sm.getUserId();

        fab.setOnClickListener(view -> showAddDialog());
        loadData();
        return v;
    }

    private void loadData() {
        api.getPastorAvailability(userId).enqueue(new Callback<PastorAvailabilityResponse>() {
            @Override
            public void onResponse(Call<PastorAvailabilityResponse> call, Response<PastorAvailabilityResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    items.clear();
                    items.addAll(response.body().getAvailability());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(),"Erreur de récupération",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PastorAvailabilityResponse> call, Throwable t) {
                Toast.makeText(getContext(),"Connexion échouée: "+t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDelete(PastorAvailability item) {
        new AlertDialog.Builder(getContext())
                .setTitle("Supprimer")
                .setMessage("Supprimer cette disponibilité ?")
                .setPositiveButton("Oui", (d,i) -> deleteItem(item))
                .setNegativeButton("Non", null)
                .show();
    }

    private void deleteItem(PastorAvailability item) {
        api.deletePastorAvailability(new IdRequest(item.getId())).enqueue(new Callback<SimpleResp>() {
            @Override public void onResponse(Call<SimpleResp> call, Response<SimpleResp> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), "Supprimé", Toast.LENGTH_SHORT).show();
                    loadData();
                } else Toast.makeText(getContext(),"Erreur suppression",Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailure(Call<SimpleResp> call, Throwable t) {
                Toast.makeText(getContext(),"Connexion échouée: "+t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        View form = getLayoutInflater().inflate(R.layout.dialog_add_availability, null);
        Spinner spinnerDay = form.findViewById(R.id.spinnerDayOfWeek);
        EditText etSpecificDate = form.findViewById(R.id.etSpecificDate);
        EditText etStart = form.findViewById(R.id.etStart);
        EditText etEnd = form.findViewById(R.id.etEnd);
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new String[]{"", "Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi","Dimanche"});
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(dayAdapter);

        etSpecificDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(getContext(), (dp, y,m,d) -> etSpecificDate.setText(String.format("%04d-%02d-%02d", y, m+1, d)), c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        etStart.setOnClickListener(v -> timePicker(etStart));
        etEnd.setOnClickListener(v -> timePicker(etEnd));

        b.setView(form);
        b.setTitle("Ajouter disponibilité");
        b.setPositiveButton("Ajouter", (dialog, which) -> {
            String day = spinnerDay.getSelectedItem().toString();
            if (TextUtils.isEmpty(day)) day = null;
            String spec = etSpecificDate.getText().toString().trim();
            if (spec.isEmpty()) spec = null;
            String start = etStart.getText().toString().trim();
            String end = etEnd.getText().toString().trim();

            if ((day==null && spec==null) || start.isEmpty() || end.isEmpty()) {
                Toast.makeText(getContext(),"Remplir jour/date et heures", Toast.LENGTH_SHORT).show();
                return;
            }
            PastorAvailabilityRequest r = new PastorAvailabilityRequest(userId, day, spec, start, end);
            api.addPastorAvailability(r).enqueue(new Callback<SimpleResp>() {
                @Override public void onResponse(Call<SimpleResp> call, Response<SimpleResp> response) {
                    if (response.isSuccessful() && response.body()!=null && response.body().isSuccess()) {
                        Toast.makeText(getContext(),"Ajouté",Toast.LENGTH_SHORT).show();
                        loadData();
                    } else Toast.makeText(getContext(),"Erreur ajout",Toast.LENGTH_SHORT).show();
                }
                @Override public void onFailure(Call<SimpleResp> call, Throwable t) {
                    Toast.makeText(getContext(),"Connexion échouée: "+t.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        });
        b.setNegativeButton("Annuler", null);
        b.show();
    }

    private void timePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(getContext(), (tp, hour, minute) -> target.setText(String.format("%02d:%02d:00", hour, minute)), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }
}
