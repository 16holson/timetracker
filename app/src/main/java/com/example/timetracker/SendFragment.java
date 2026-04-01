package com.example.timetracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.timetracker.databinding.FragmentSendBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SendFragment extends Fragment {

    private FragmentSendBinding binding;
    private int jobId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            jobId = getArguments().getInt("jobId");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSendBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            JobWithEmployer job = AppDatabase.getDatabase(getContext()).appDao().getJobWithEmployerById(jobId);
            List<TimeRecord> records = AppDatabase.getDatabase(getContext()).appDao().getTimeRecordsForJob(jobId);
            
            StringBuilder report = new StringBuilder();
            report.append("Job Report\n");
            report.append("Employer: ").append(job.employer.getName()).append("\n");
            report.append("Job Title: ").append(job.job.getTitle()).append("\n\n");
            report.append("Time Records:\n");
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            long totalMillis = 0;
            
            for (TimeRecord record : records) {
                long diff = record.getEndTime() - record.getStartTime();
                totalMillis += diff;
                report.append("- ").append(dateFormat.format(new Date(record.getStartTime())))
                      .append(" to ").append(dateFormat.format(new Date(record.getEndTime())))
                      .append(" (").append(diff / 3600000).append("h ")
                      .append((diff % 3600000) / 60000).append("m)\n");
            }
            
            report.append("\nTotal Time: ").append(totalMillis / 3600000).append(" hours, ")
                  .append((totalMillis % 3600000) / 60000).append(" minutes");

            getActivity().runOnUiThread(() -> {
                binding.edittextReport.setText(report.toString());
            });
        });

        binding.buttonFinalSend.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Report sent to " + (binding.edittextReport.getText().toString().contains("Employer:") ? "Employer" : "recipient"), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}