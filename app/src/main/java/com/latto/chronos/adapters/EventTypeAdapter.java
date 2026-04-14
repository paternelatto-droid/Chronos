package com.latto.chronos.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.latto.chronos.R;
import com.latto.chronos.models.EventType;

import java.util.List;

public class EventTypeAdapter extends ArrayAdapter<EventType> {

    private final LayoutInflater inflater;
    private final List<EventType> eventTypes;

    public EventTypeAdapter(Context context, List<EventType> eventTypes) {
        super(context, 0, eventTypes);
        this.eventTypes = eventTypes;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.spinner_event_type_item, parent, false);

        ImageView icon = view.findViewById(R.id.imgEventTypeIcon);
        TextView name = view.findViewById(R.id.txtEventTypeName);

        EventType item = eventTypes.get(position);

        // Nom
        name.setText(item.getName());

        // Icône locale avec fallback
        int iconRes = getContext().getResources().getIdentifier(
                (item.getIcon() != null ? item.getIcon().replace(".png","") : "ic_event_default"),
                "drawable",
                getContext().getPackageName()
        );
        icon.setImageResource(iconRes != 0 ? iconRes : R.drawable.dot_circle);

        // Appliquer la couleur à l'icône si définie
        if (item.getColorHex() != null && !item.getColorHex().isEmpty()) {
            try {
                int color = Color.parseColor(item.getColorHex());
                icon.setColorFilter(color);
            } catch (Exception e) {
                icon.setColorFilter(Color.GRAY); // fallback
            }
        } else {
            icon.setColorFilter(Color.GRAY); // couleur par défaut
        }

        return view;
    }
}
