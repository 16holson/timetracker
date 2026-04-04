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
    private OnTimeClickListener listener;
    private SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    public interface OnTimeClickListener {
        void onTimeClick(TimeRecord record);
    }

    public TimeAdapter(List<TimeRecord> records, OnTimeClickListener listener) {
        this.records = records;
        this.listener = listener;
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
        
        long displayDate = record.getStartTime() != 0 ? record.getStartTime() : record.getTravelStartTime();
        holder.dateDisplay.setText(dayFormat.format(new Date(displayDate)));
        
        if (record.getStartTime() != 0) {
            holder.workTimeDisplay.setVisibility(View.VISIBLE);
            String workText = "Work: " + timeFormat.format(new Date(record.getStartTime()));
            if (record.getEndTime() != 0) {
                workText += " - " + timeFormat.format(new Date(record.getEndTime()));
            } else {
                workText += " - (In Progress)";
            }
            holder.workTimeDisplay.setText(workText);
        } else {
            holder.workTimeDisplay.setVisibility(View.GONE);
        }
        
        if (record.getLunchStartTime() != 0) {
            holder.lunchDisplay.setVisibility(View.VISIBLE);
            String lunchText = "Lunch: " + timeFormat.format(new Date(record.getLunchStartTime()));
            if (record.getLunchEndTime() != 0) {
                lunchText += " - " + timeFormat.format(new Date(record.getLunchEndTime()));
            } else {
                lunchText += " - (In Progress)";
            }
            holder.lunchDisplay.setText(lunchText);
        } else {
            holder.lunchDisplay.setVisibility(View.GONE);
        }

        if (record.getTravelStartTime() != 0) {
            holder.travelDisplay.setVisibility(View.VISIBLE);
            String travelText = "Travel: " + timeFormat.format(new Date(record.getTravelStartTime()));
            if (record.getTravelEndTime() != 0) {
                travelText += " - " + timeFormat.format(new Date(record.getTravelEndTime()));
            } else {
                travelText += " - (In Progress)";
            }
            holder.travelDisplay.setText(travelText);
        } else {
            holder.travelDisplay.setVisibility(View.GONE);
        }
        
        if (record.getStartTime() != 0 && record.getEndTime() != 0) {
            long diff = record.getEndTime() - record.getStartTime();
            if (record.getLunchStartTime() != 0 && record.getLunchEndTime() != 0) {
                diff -= (record.getLunchEndTime() - record.getLunchStartTime());
            }
            
            long hours = diff / 3600000;
            long minutes = (diff % 3600000) / 60000;
            holder.duration.setText(String.format(Locale.getDefault(), "Work Duration: %d hours, %d minutes", hours, minutes));
            holder.duration.setVisibility(View.VISIBLE);
        } else {
            holder.duration.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTimeClick(record);
            }
        });
    }

    @Override
    public int getItemCount() {
        return records == null ? 0 : records.size();
    }

    static class TimeViewHolder extends RecyclerView.ViewHolder {
        TextView dateDisplay;
        TextView workTimeDisplay;
        TextView lunchDisplay;
        TextView travelDisplay;
        TextView duration;

        public TimeViewHolder(@NonNull View itemView) {
            super(itemView);
            dateDisplay = itemView.findViewById(R.id.date_display);
            workTimeDisplay = itemView.findViewById(R.id.work_time_display);
            lunchDisplay = itemView.findViewById(R.id.lunch_display);
            travelDisplay = itemView.findViewById(R.id.travel_display);
            duration = itemView.findViewById(R.id.duration);
        }
    }
}