package com.example.timetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private List<JobWithEmployer> jobs;
    private OnJobClickListener listener;

    public interface OnJobClickListener {
        void onJobClick(JobWithEmployer job);
        void onDeleteClick(Job job);
    }

    public JobAdapter(List<JobWithEmployer> jobs, OnJobClickListener listener) {
        this.jobs = jobs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        JobWithEmployer jobWithEmployer = jobs.get(position);
        holder.jobLocation.setText(jobWithEmployer.job.getLocation());
        holder.employerName.setText(jobWithEmployer.employer.getName());
        
        holder.itemView.setOnClickListener(v -> listener.onJobClick(jobWithEmployer));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onDeleteClick(jobWithEmployer.job);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return jobs == null ? 0 : jobs.size();
    }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView jobLocation;
        TextView employerName;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            jobLocation = itemView.findViewById(R.id.job_location);
            employerName = itemView.findViewById(R.id.employer_name);
        }
    }
}