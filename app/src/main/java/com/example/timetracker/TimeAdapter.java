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
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

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
        holder.startTime.setText("Start: " + dateFormat.format(new Date(record.getStartTime())));
        holder.endTime.setText("End: " + dateFormat.format(new Date(record.getEndTime())));
        
        long diff = record.getEndTime() - record.getStartTime();
        long hours = diff / 3600000;
        long minutes = (diff % 3600000) / 60000;
        holder.duration.setText(String.format(Locale.getDefault(), "Duration: %d hours, %d minutes", hours, minutes));
    }

    @Override
    public int getItemCount() {
        return records == null ? 0 : records.size();
    }

    static class TimeViewHolder extends RecyclerView.ViewHolder {
        TextView startTime;
        TextView endTime;
        TextView duration;

        public TimeViewHolder(@NonNull View itemView) {
            super(itemView);
            startTime = itemView.findViewById(R.id.start_time);
            endTime = itemView.findViewById(R.id.end_time);
            duration = itemView.findViewById(R.id.duration);
        }
    }
}