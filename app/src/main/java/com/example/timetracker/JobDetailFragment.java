package com.example.timetracker;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.timetracker.databinding.FragmentJobDetailBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class JobDetailFragment extends Fragment {

    private FragmentJobDetailBinding binding;
    private int jobId;
    private Job currentJob;
    private Employer currentEmployer;

    private long tempStartTime, tempEndTime, tempLunchStart, tempLunchEnd, tempTravelStart, tempTravelEnd;
    private Calendar baseCalendar = Calendar.getInstance();

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
        binding = FragmentJobDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerviewTimes.setLayoutManager(new LinearLayoutManager(getContext()));

        refreshData();
    }

    private void refreshData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            currentJob = AppDatabase.getDatabase(getContext()).appDao().getJobById(jobId);
            if (currentJob != null) {
                currentEmployer = AppDatabase.getDatabase(getContext()).appDao().getEmployerById(currentJob.getEmployerId());
            }
            List<TimeRecord> records = AppDatabase.getDatabase(getContext()).appDao().getTimeRecordsForJob(jobId);
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (currentJob != null) {
                        binding.editJobLocation.setText(currentJob.getLocation());
                    }
                    if (currentEmployer != null) {
                        binding.editEmployerName.setText(currentEmployer.getName());
                        binding.editEmployerEmail.setText(currentEmployer.getEmail());
                    }
                    binding.recyclerviewTimes.setAdapter(new TimeAdapter(records, new TimeAdapter.OnTimeClickListener() {
                        @Override
                        public void onTimeClick(TimeRecord record) {
                            showEditTimeDialog(record);
                        }

                        @Override
                        public void onDeleteTimeClick(TimeRecord record) {
                            showDeleteTimeConfirmation(record);
                        }
                    }));
                    setupEditors();
                });
            }
        });
    }

    private void showEditTimeDialog(TimeRecord record) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_time, null);
        
        tempStartTime = record.getStartTime();
        tempEndTime = record.getEndTime();
        tempLunchStart = record.getLunchStartTime();
        tempLunchEnd = record.getLunchEndTime();
        tempTravelStart = record.getTravelStartTime();
        tempTravelEnd = record.getTravelEndTime();

        long existingDate = tempStartTime != 0 ? tempStartTime : tempTravelStart;
        baseCalendar = Calendar.getInstance();
        if (existingDate != 0) {
            baseCalendar.setTimeInMillis(existingDate);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        Button btnDate = dialogView.findViewById(R.id.btn_date);
        btnDate.setText("Date: " + dateFormat.format(baseCalendar.getTime()));

        btnDate.setOnClickListener(v -> {
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                baseCalendar.set(Calendar.YEAR, year);
                baseCalendar.set(Calendar.MONTH, month);
                baseCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                btnDate.setText("Date: " + dateFormat.format(baseCalendar.getTime()));
                
                if (tempStartTime != 0) tempStartTime = updateDateOfTime(tempStartTime, baseCalendar);
                if (tempEndTime != 0) tempEndTime = updateDateOfTime(tempEndTime, baseCalendar);
                if (tempLunchStart != 0) tempLunchStart = updateDateOfTime(tempLunchStart, baseCalendar);
                if (tempLunchEnd != 0) tempLunchEnd = updateDateOfTime(tempLunchEnd, baseCalendar);
                if (tempTravelStart != 0) tempTravelStart = updateDateOfTime(tempTravelStart, baseCalendar);
                if (tempTravelEnd != 0) tempTravelEnd = updateDateOfTime(tempTravelEnd, baseCalendar);
            }, baseCalendar.get(Calendar.YEAR), baseCalendar.get(Calendar.MONTH), baseCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        setupTimeButton(dialogView.findViewById(R.id.btn_start_time), "Start", tempStartTime, t -> tempStartTime = t);
        setupTimeButton(dialogView.findViewById(R.id.btn_end_time), "End", tempEndTime, t -> tempEndTime = t);
        setupTimeButton(dialogView.findViewById(R.id.btn_lunch_start), "Lunch Start", tempLunchStart, t -> tempLunchStart = t);
        setupTimeButton(dialogView.findViewById(R.id.btn_lunch_end), "Lunch End", tempLunchEnd, t -> tempLunchEnd = t);
        setupTimeButton(dialogView.findViewById(R.id.btn_travel_start), "Travel Start", tempTravelStart, t -> tempTravelStart = t);
        setupTimeButton(dialogView.findViewById(R.id.btn_travel_end), "Travel End", tempTravelEnd, t -> tempTravelEnd = t);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Edit Time Entry")
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (TimeValidator.isValid(getContext(), tempStartTime, tempEndTime, tempLunchStart, tempLunchEnd, tempTravelStart, tempTravelEnd)) {
                record.setStartTime(tempStartTime);
                record.setEndTime(tempEndTime);
                record.setLunchStartTime(tempLunchStart);
                record.setLunchEndTime(tempLunchEnd);
                record.setTravelStartTime(tempTravelStart);
                record.setTravelEndTime(tempTravelEnd);

                AppDatabase.databaseWriteExecutor.execute(() -> {
                    AppDatabase.getDatabase(getContext()).appDao().updateTime(record);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Time entry updated", Toast.LENGTH_SHORT).show();
                            refreshData();
                            dialog.dismiss();
                        });
                    }
                });
            }
        });
    }

    private long updateDateOfTime(long time, Calendar date) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        c.set(Calendar.YEAR, date.get(Calendar.YEAR));
        c.set(Calendar.MONTH, date.get(Calendar.MONTH));
        c.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
        return c.getTimeInMillis();
    }

    private void showDeleteTimeConfirmation(TimeRecord record) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Time Entry")
                .setMessage("Are you sure you want to delete this time entry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        AppDatabase.getDatabase(getContext()).appDao().deleteTime(record);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Time entry deleted", Toast.LENGTH_SHORT).show();
                                refreshData();
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupTimeButton(Button button, String label, long initialTime, MainActivity.TimeSelectedListener listener) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        if (initialTime != 0) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(initialTime);
            button.setText(label + ": " + timeFormat.format(c.getTime()));
        }

        button.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(baseCalendar.getTimeInMillis());
            if (initialTime != 0) calendar.setTimeInMillis(initialTime);
            
            new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long timeMillis = calendar.getTimeInMillis();
                listener.onTimeSelected(timeMillis);
                button.setText(label + ": " + timeFormat.format(calendar.getTime()));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
        });
    }

    private void setupEditors() {
        binding.editJobLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (currentJob != null) {
                    currentJob.setLocation(s.toString());
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        AppDatabase.getDatabase(getContext()).appDao().updateJob(currentJob);
                    });
                }
            }
        });

        binding.editEmployerName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (currentEmployer != null) {
                    currentEmployer.setName(s.toString());
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        AppDatabase.getDatabase(getContext()).appDao().updateEmployer(currentEmployer);
                    });
                }
            }
        });

        binding.editEmployerEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (currentEmployer != null) {
                    currentEmployer.setEmail(s.toString());
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        AppDatabase.getDatabase(getContext()).appDao().updateEmployer(currentEmployer);
                    });
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
