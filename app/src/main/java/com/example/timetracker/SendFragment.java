package com.example.timetracker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.timetracker.databinding.FragmentSendBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SendFragment extends Fragment {

    private FragmentSendBinding binding;
    private int jobId;
    private JobWithEmployer currentJobWithEmployer;

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

        // Handle system bar insets to prevent the button from being covered by navigation buttons
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        AppDatabase.databaseWriteExecutor.execute(() -> {
            currentJobWithEmployer = AppDatabase.getDatabase(getContext()).appDao().getJobWithEmployerById(jobId);
            List<TimeRecord> records = AppDatabase.getDatabase(getContext()).appDao().getTimeRecordsForJob(jobId);
            
            StringBuilder report = new StringBuilder();
            report.append("Job Report\n");
            report.append("Employer: ").append(currentJobWithEmployer.employer.getName()).append("\n");
            report.append("Location: ").append(currentJobWithEmployer.job.getLocation()).append("\n\n");
            report.append("Time Records:\n");
            
            SimpleDateFormat dayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            long totalWorkMillis = 0;
            long totalTravelMillis = 0;
            
            for (TimeRecord record : records) {
                long displayDate = record.getStartTime() != 0 ? record.getStartTime() : record.getTravelStartTime();
                String datePart = "- " + dayFormat.format(new Date(displayDate)) + ": ";
                report.append(datePart);
                
                boolean hasWork = record.getStartTime() != 0;

                // Work section
                if (hasWork) {
                    report.append("Work: ").append(timeFormat.format(new Date(record.getStartTime())));
                    if (record.getEndTime() != 0) {
                        long workDiff = record.getEndTime() - record.getStartTime();
                        if (record.getLunchStartTime() != 0 && record.getLunchEndTime() != 0) {
                            workDiff -= (record.getLunchEndTime() - record.getLunchStartTime());
                        }
                        totalWorkMillis += workDiff;
                        report.append(" to ").append(timeFormat.format(new Date(record.getEndTime())));
                    } else {
                        report.append(" to In Progress");
                    }

                    // Lunch section
                    if (record.getLunchStartTime() != 0) {
                        report.append(" (Lunch: ").append(timeFormat.format(new Date(record.getLunchStartTime()))).append(" - ");
                        if (record.getLunchEndTime() != 0) {
                            report.append(timeFormat.format(new Date(record.getLunchEndTime())));
                        } else {
                            report.append("In Progress");
                        }
                        report.append(")");
                    }
                }

                // Travel section
                if (record.getTravelStartTime() != 0) {
                    if (hasWork) {
                        report.append("\n");
                        // Add spaces to align exactly with the start of "Work:"
                        report.append("                "); // 16 spaces for alignment
                    }
                    report.append("Travel: ").append(timeFormat.format(new Date(record.getTravelStartTime()))).append(" - ");
                    if (record.getTravelEndTime() != 0) {
                        long travelDiff = record.getTravelEndTime() - record.getTravelStartTime();
                        totalTravelMillis += travelDiff;
                        report.append(timeFormat.format(new Date(record.getTravelEndTime())));
                    } else {
                        report.append("In Progress");
                    }
                }
                
                report.append("\n");
            }
            
            report.append("\nTotal Work Time: ").append(totalWorkMillis / 3600000).append(" hours, ")
                  .append((totalWorkMillis % 3600000) / 60000).append(" minutes\n");
            
            report.append("Total Travel Time: ").append(totalTravelMillis / 3600000).append(" hours, ")
                  .append((totalTravelMillis % 3600000) / 60000).append(" minutes");

            getActivity().runOnUiThread(() -> {
                binding.edittextReport.setText(report.toString());
            });
        });

        binding.buttonFinalSend.setOnClickListener(v -> {
            String reportText = binding.edittextReport.getText().toString();
            String email = (currentJobWithEmployer != null) ? currentJobWithEmployer.employer.getEmail() : "";
            String location = (currentJobWithEmployer != null) ? currentJobWithEmployer.job.getLocation() : "";
            String subject = "Job Report - " + location;

            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, reportText);

            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // Fallback for devices without a default mailto handler or resolveActivity limitations
                try {
                    startActivity(Intent.createChooser(intent, "Send email..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getContext(), "No email apps installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}