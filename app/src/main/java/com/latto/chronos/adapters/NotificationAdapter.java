package com.latto.chronos.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.latto.chronos.R;
import com.latto.chronos.models.NotificationModel;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context context;
    private List<NotificationModel> notificationModels;

    public NotificationAdapter(Context context, List<NotificationModel> notificationModels) {
        this.context = context;
        this.notificationModels = notificationModels;

    }

    public List<NotificationModel> getNotifications() {
        return notificationModels;
    }

    public void setNotifications(List<NotificationModel> notificationModels) {
        this.notificationModels = notificationModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_remainder_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final NotificationModel notificationModel = notificationModels.get(position);

        holder.cardView.setVisibility(View.VISIBLE);
        holder.reminderTimeTextView.setText(notificationModel.getDisplayLabel());
        //holder.reminderTimeTextView.setText(notificationModel.getTime());

        holder.cancelNotificationImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    notificationModels.remove(adapterPosition);
                    notifyItemRemoved(adapterPosition);
                    notifyItemRangeChanged(adapterPosition, notificationModels.size());
                }
            }
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.layout_alert_dialog_notification, null, false);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();

        final RadioGroup notificationPreferenceRadioGroup = dialogView.findViewById(R.id.AlertDialogLayout_RadioGroup);
        Button backButton = dialogView.findViewById(R.id.AlertDialogLayout_Button_Back);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        // Ouvrir la popup quand on clique sur le label
        holder.reminderTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notificationPreferenceRadioGroup.check(getIdOfRadioButton(holder.reminderTimeTextView.getText().toString()));
                alertDialog.show();
            }
        });

// Gestion du choix
        notificationPreferenceRadioGroup.setOnCheckedChangeListener((group, buttonId) -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                RadioButton selectedPreferenceRadioButton = dialogView.findViewById(buttonId);
                String selectedLabel = selectedPreferenceRadioButton.getText().toString();

                NotificationModel updatedNotificationModel = new NotificationModel();
                updatedNotificationModel.setId(notificationModels.get(adapterPosition).getId());
                updatedNotificationModel.setEventId(notificationModels.get(adapterPosition).getEventId());
                updatedNotificationModel.setMessage(notificationModels.get(adapterPosition).getMessage());
                updatedNotificationModel.setTimeFromLabel(selectedLabel);

                notificationModels.set(adapterPosition, updatedNotificationModel);
                notifyItemChanged(adapterPosition);
                alertDialog.dismiss();
            }
        });
    }


    private int getIdOfRadioButton(String text) {
        switch (text) {
            case "10 minutes avant":
                return R.id.AlertDialogLayout_Notification_RadioButton_10minBefore;
            case "1 heure avant":
                return R.id.AlertDialogLayout_Notification_RadioButton_1hourBefore;
            case "1 jour avant":
                return R.id.AlertDialogLayout_Notification_RadioButton_1dayBefore;
            case "Au moment de l'événement":
                return R.id.AlertDialogLayout_Notification_RadioButton_AtTheTimeOfEvent;
            default:
                return R.id.AlertDialogLayout_Notification_RadioButton_AtTheTimeOfEvent;
        }
    }


    @Override
    public int getItemCount() {
        return notificationModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView reminderTimeTextView;
        private ImageButton cancelNotificationImageButton;
        private LinearLayout rootLinearLayout;
        private CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            reminderTimeTextView = (TextView) itemView.findViewById(R.id.ReminderListLayout_TextView_Notification);
            cancelNotificationImageButton = (ImageButton) itemView.findViewById(R.id.ReminderListLayout_ImageButton_Cancel);
            rootLinearLayout = (LinearLayout) itemView.findViewById(R.id.ReminderListLayout_LinearLayout_Root);
            cardView = (CardView) itemView.findViewById(R.id.ReminderListLayout_CardView);
        }

    }


}
