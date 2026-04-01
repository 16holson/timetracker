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
        JobWithEmployer job = jobs.get(position);
        holder.jobTitle.setText(job.job.getTitle());
        holder.employerName.setText(job.employer.getName());
        holder.itemView.setOnClickListener(v -> listener.onJobClick(job));
    }

    @Override
    public int getItemCount() {
        return jobs == null ? 0 : jobs.size();
    }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView jobTitle;
        TextView employerName;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            jobTitle = itemView.findViewById(R.id.job_title);
            employerName = itemView.findViewById(R.id.employer_name);
        }
    }
}