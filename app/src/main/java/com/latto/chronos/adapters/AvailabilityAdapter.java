package com.latto.chronos.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.latto.chronos.R;
import com.latto.chronos.models.PastorAvailability;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AvailabilityAdapter extends RecyclerView.Adapter<AvailabilityAdapter.VH> {

    public interface Callback { void onDelete(PastorAvailability item); void onItemClick(PastorAvailability item); }

    private final Context ctx;
    private final List<PastorAvailability> items;
    private final Callback callback;
    private final SimpleDateFormat timeIn = new SimpleDateFormat("HH:mm:ss");

    public AvailabilityAdapter(Context ctx, List<PastorAvailability> items, Callback callback){
        this.ctx=ctx;
        this.items=items;
        this.callback=callback;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent,int viewType){
        return new VH(LayoutInflater.from(ctx).inflate(R.layout.item_availability,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h,int pos){
        PastorAvailability it = items.get(pos);
        String line1;
        if (it.getSpecific_date() != null && !it.getSpecific_date().isEmpty()) {
            line1 = it.getSpecific_date();
        } else {
            line1 = it.getDay_of_week();
        }
        String line2 = formatTime(it.getStart_time()) + " - " + formatTime(it.getEnd_time());
        h.tv1.setText(line1);
        h.tv2.setText(line2);

        h.itemView.setOnClickListener(v -> {
            if (callback!=null) callback.onItemClick(it);
            else Toast.makeText(ctx, line1 + " " + line2, Toast.LENGTH_SHORT).show();
        });

        h.itemView.setOnLongClickListener(v -> {
            if (callback!=null) callback.onDelete(it);
            return true;
        });
    }

    @Override public int getItemCount(){ return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv1, tv2;
        VH(@NonNull View iv){ super(iv); tv1=iv.findViewById(R.id.tvLine1); tv2=iv.findViewById(R.id.tvLine2); }
    }

    private String formatTime(String t) {
        if (t==null) return "";
        try {
            Date d = timeIn.parse(t);
            SimpleDateFormat out = new SimpleDateFormat("HH:mm");
            return out.format(d);
        } catch (ParseException e) {
            return t.length()>=5 ? t.substring(0,5) : t;
        }
    }
}
