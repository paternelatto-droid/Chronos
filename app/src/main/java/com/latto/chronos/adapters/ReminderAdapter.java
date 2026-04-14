package com.latto.chronos.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.latto.chronos.R;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    private Context context;
    private List<Integer> reminders; // minutes avant
    private OnRemoveClickListener removeListener;

    public ReminderAdapter(Context context, List<Integer> reminders, OnRemoveClickListener listener) {
        this.context = context;
        this.reminders = reminders;
        this.removeListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int minutes = reminders.get(position);
        String text;
        if (minutes < 60) text = minutes + " minute" + (minutes > 1 ? "s" : "") + " avant";
        else if (minutes % 60 == 0) text = (minutes / 60) + " heure" + (minutes / 60 > 1 ? "s" : "") + " avant";
        else text = (minutes / 60) + " h " + (minutes % 60) + " min avant";

        holder.tvReminderText.setText(text);
        holder.btnRemoveReminder.setOnClickListener(v -> {
            if (removeListener != null) removeListener.onRemoveClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvReminderText;
        ImageButton btnRemoveReminder;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReminderText = itemView.findViewById(R.id.tvReminderText);
            btnRemoveReminder = itemView.findViewById(R.id.btnRemoveReminder);
        }
    }

    public interface OnRemoveClickListener {
        void onRemoveClick(int position);
    }
}

