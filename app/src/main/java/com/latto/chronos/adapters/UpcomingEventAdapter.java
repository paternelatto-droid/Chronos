package com.latto.chronos.adapters;

import static com.latto.chronos.Utils.getCurrentUserId;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.latto.chronos.R;
import com.latto.chronos.models.Event;
import com.latto.chronos.api.ApiClient;
import com.latto.chronos.api.ApiService;
import com.latto.chronos.models.UserSession;
import com.latto.chronos.response.SimpleResp;
import com.latto.chronos.views.EditEventActivity;
import com.latto.chronos.views.UpcomingEventsFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpcomingEventAdapter extends RecyclerView.Adapter<UpcomingEventAdapter.ViewHolder> {

    private static final int EDIT_EVENT_ACTIVITY_REQUEST_CODE = 1;
    private final String TAG = getClass().getSimpleName();

    private final Context context;
    private final List<Event> eventList;
    private final UpcomingEventsFragment fragment;
    private final ApiService apiService;

    public UpcomingEventAdapter(Context context, List<Event> events, UpcomingEventsFragment fragment) {
        this.context = context;
        this.eventList = events;
        this.fragment = fragment;
        this.apiService = ApiClient.getService(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_events, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);

        // ✅ Couleur du marqueur
        int color = event.color != 0 ? event.color : context.getColor(R.color.darkIndigo);
        holder.eventColor.setBackgroundColor(color);

        // ✅ Titre avec fallback
        holder.eventTitle.setText(event.title != null ? event.title : "Sans titre");
// ✅ Format de la date (yyyy-MM-dd -> dd-MM-yyyy)
        try {
            if (event.getDateDebut() != null && event.getDateDebut().contains(" ")) {
                // Si dateDebut contient aussi l'heure -> on split
                String onlyDate = event.getDateDebut().split(" ")[0]; // "2025-10-12"
                Date parsedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(onlyDate);
                if (parsedDate != null) {
                    String formattedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.FRENCH).format(parsedDate);
                    holder.eventDate.setText(formattedDate);
                }
            }
        } catch (Exception e) {
            holder.eventDate.setText("Date invalide");
        }

        // ✅ Extraction de l'heure
        String startTime = "??";
        String endTime = "??";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            Date dateStart = inputFormat.parse(event.getDateDebut());
            Date dateEnd = inputFormat.parse(event.getDateFin());

            if (dateStart != null) startTime = outputFormat.format(dateStart);
            if (dateEnd != null) endTime = outputFormat.format(dateEnd);
        } catch (Exception e) {
            // En cas d'erreur on laisse ??
        }

        // ✅ Affichage dans le TextView
        holder.eventTime.setText(startTime + " - " + endTime);

        // ✅ Note / description
        holder.eventNote.setText(event.description != null ? event.description : "");

        // ✅ Gestion du bouton options
        holder.optionButton.setOnClickListener(v -> showPopupMenu(holder.optionButton, position));

    }

    private void showPopupMenu(View view, int position) {
        PopupMenu popup = new PopupMenu(context, view);

        popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());

// 🔽 Cacher les actions non autorisées
        if (!UserSession.hasPermission("event.update")) {
            if (eventList.get(position).userId != getCurrentUserId(context)){
                popup.getMenu().findItem(R.id.Popup_Item_Edit).setVisible(false);
            }
        }

        if (!UserSession.hasPermission("event.delete")) {
            if (eventList.get(position).userId != getCurrentUserId(context)) {
                popup.getMenu().findItem(R.id.Popup_Item_Delete).setVisible(false);
            }
        }

        popup.setOnMenuItemClickListener(new UpcomingEventAdapter.MyMenuItemClickListener(position));
        popup.show();

    }

    private class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        private final int position;
        private final Event event;

        public MyMenuItemClickListener(int position) {
            this.position = position;
            this.event = eventList.get(position);
        }

        public boolean onMenuItemClick(MenuItem menuItem) {
            int id = menuItem.getItemId();
            Intent intent = new Intent(context, EditEventActivity.class);

            // 🔐 Vérifier les permissions AVANT d'exécuter l'action
            if (id == R.id.Popup_Item_Edit) {
                if (!UserSession.hasPermission("event.update")) {
                    Toast.makeText(context, "Vous n'avez pas la permission de modifier", Toast.LENGTH_SHORT).show();
                    return true; // Empêche l'action
                }
                // Lancer EditEventActivity en passant l'objet event
                intent.putExtra("event", event); // event doit être Serializable/Parcelable
                // si appelé depuis fragment, startActivityForResult via fragment possible :
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, EDIT_EVENT_ACTIVITY_REQUEST_CODE);
                } else {
                    context.startActivity(intent);
                }
                return true;
            }
            else if (id == R.id.Popup_Item_Delete) {
                if (!UserSession.hasPermission("event.delete")) {
                    Toast.makeText(context, "Vous n'avez pas la permission de supprimer", Toast.LENGTH_SHORT).show();
                    return true;
                }
                deleteEvent(position,event);
                return true;
            }

            return false;
        }

    }
    private void deleteEvent(int position, Event event) {
        new AlertDialog.Builder(context)
                .setTitle("Supprimer")
                .setMessage("Voulez-vous vraiment supprimer cet événement ?")
                .setPositiveButton("Oui", (dialog, which) -> {

                    apiService.deleteEvent(event.getId()).enqueue(new Callback<SimpleResp>() {
                        @Override
                        public void onResponse(Call<SimpleResp> call, Response<SimpleResp> response) {
                            if (response.isSuccessful() && response.body() != null) {

                                // ✅ Retirer de la liste localement
                                eventList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, eventList.size());

                                Toast.makeText(context, "Événement supprimé", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<SimpleResp> call, Throwable t) {
                            Toast.makeText(context, "Erreur réseau: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                })
                .setNegativeButton("Annuler", null)
                .show();
    }


    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView eventColor;
        TextView eventTitle,eventDate, eventTime, eventNote;
        ImageButton optionButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventColor = itemView.findViewById(R.id.eventColor);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventTime = itemView.findViewById(R.id.eventTime);
            eventNote = itemView.findViewById(R.id.eventNote);
            optionButton = itemView.findViewById(R.id.optionButton);
        }
    }
}
