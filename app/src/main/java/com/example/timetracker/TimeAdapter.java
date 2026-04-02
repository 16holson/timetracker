package com.example.timetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimeAdapter extends RecyclerView.Adapter<TimeAdapter.TimeViewHolder> {

    private List<TimeRecord> records;
    private SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    public TimeAdapter(List<TimeRecord> records) {
        this.records = records;
    }

    @NonNull
    @Override
    public TimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time, parent, false);
        return new TimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeViewHolder holder, int position) {
        TimeRecord record = records.get(position);
        
        holder.dateDisplay.setText(dayFormat.format(new Date(record.getStartTime())));
        holder.startTime.setText("Start: " + timeFormat.format(new Date(record.getStartTime())));
        holder.endTime.setText("End: " + timeFormat.format(new Date(record.getEndTime())));
        
        if (record.getLunchStartTime() != 0 && record.getLunchEndTime() != 0) {
            holder.lunchDisplay.setVisibility(View.VISIBLE);
            holder.lunchDisplay.setText("Lunch: " + timeFormat.format(new Date(record.getLunchStartTime())) + 
                    " - " + timeFormat.format(new Date(record.getLunchEndTime())));
        } else {
            holder.lunchDisplay.setVisibility(View.GONE);
        }

        if (record.getTravelStartTime() != 0 && record.getTravelEndTime() != 0) {
            holder.travelDisplay.setVisibility(View.VISIBLE);
            holder.travelDisplay.setText("Travel: " + timeFormat.format(new Date(record.getTravelStartTime())) + 
                    " - " + timeFormat.format(new Date(record.getTravelEndTime())));
        } else {
            holder.travelDisplay.setVisibility(View.GONE);
        }
        
        long diff = record.getEndTime() - record.getStartTime();
        // Subtract lunch if exists
        if (record.getLunchStartTime() != 0 && record.getLunchEndTime() != 0) {
            diff -= (record.getLunchEndTime() - record.getLunchStartTime());
        }
        
        long hours = diff / 3600000;
        long minutes = (diff % 3600000) / 60000;
        holder.duration.setText(String.format(Locale.getDefault(), "Work Duration: %d hours, %d minutes", hours, minutes));
    }

    @Override
    public int getItemCount() {
        return records == null ? 0 : records.size();
    }

    static class TimeViewHolder extends RecyclerView.ViewHolder {
        TextView dateDisplay;
        TextView startTime;
        TextView endTime;
        TextView lunchDisplay;
        TextView travelDisplay;
        TextView duration;

        public TimeViewHolder(@NonNull View itemView) {
            super(itemView);
            dateDisplay = itemView.findViewById(R.id.date_display);
            startTime = itemView.findViewById(R.id.start_time);
            endTime = itemView.findViewById(R.id.end_time);
            lunchDisplay = itemView.findViewById(R.id.lunch_display);
            travelDisplay = itemView.findViewById(R.id.travel_display);
            duration = itemView.findViewById(R.id.duration);
        }
    }
}