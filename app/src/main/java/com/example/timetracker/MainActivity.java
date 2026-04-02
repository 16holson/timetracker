package com.example.timetracker;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.timetracker.databinding.ActivityMainBinding;
import com.example.timetracker.databinding.DialogAddJobBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private NavController navController;

    private long startTime, endTime, lunchStart, lunchEnd, travelStart, travelEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.FirstFragment || destination.getId() == R.id.JobDetailFragment) {
                binding.fab.show();
            } else {
                binding.fab.hide();
            }
        });

        binding.fab.setOnClickListener(view -> {
            int currentDest = navController.getCurrentDestination().getId();
            if (currentDest == R.id.FirstFragment) {
                showAddJobDialog();
            } else if (currentDest == R.id.JobDetailFragment) {
                Bundle args = navController.getCurrentBackStackEntry().getArguments();
                if (args != null) {
                    int jobId = args.getInt("jobId");
                    showAddTimeDialog(jobId);
                }
            }
        });
    }

    private void showAddJobDialog() {
        DialogAddJobBinding dialogBinding = DialogAddJobBinding.inflate(getLayoutInflater());
        new AlertDialog.Builder(this)
                .setTitle("Add New Job")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Create", (dialog, which) -> {
                    String employerName = dialogBinding.editEmployerName.getText().toString();
                    String jobTitle = dialogBinding.editJobTitle.getText().toString();
                    
                    if (!employerName.isEmpty() && !jobTitle.isEmpty()) {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            AppDao dao = AppDatabase.getDatabase(getApplicationContext()).appDao();
                            long empId = dao.insertEmployer(new Employer(employerName));
                            long jobId = dao.insertJob(new Job(jobTitle, (int) empId));
                            
                            runOnUiThread(() -> {
                                Bundle bundle = new Bundle();
                                bundle.putInt("jobId", (int) jobId);
                                navController.navigate(R.id.action_FirstFragment_to_JobDetailFragment, bundle);
                            });
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddTimeDialog(int jobId) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_time, null);
        
        // Reset times for new dialog
        startTime = endTime = lunchStart = lunchEnd = travelStart = travelEnd = 0;

        setupTimeButton(dialogView.findViewById(R.id.btn_start_time), "Start", t -> startTime = t);
        setupTimeButton(dialogView.findViewById(R.id.btn_end_time), "End", t -> endTime = t);
        setupTimeButton(dialogView.findViewById(R.id.btn_lunch_start), "Lunch Start", t -> lunchStart = t);
        setupTimeButton(dialogView.findViewById(R.id.btn_lunch_end), "Lunch End", t -> lunchEnd = t);
        setupTimeButton(dialogView.findViewById(R.id.btn_travel_start), "Travel Start", t -> travelStart = t);
        setupTimeButton(dialogView.findViewById(R.id.btn_travel_end), "Travel End", t -> travelEnd = t);

        new AlertDialog.Builder(this)
                .setTitle("Add Time Entry")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        AppDatabase.getDatabase(getApplicationContext()).appDao()
                                .insertTime(new TimeRecord(jobId, startTime, endTime, lunchStart, lunchEnd, travelStart, travelEnd));
                        
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Time entry added", Toast.LENGTH_SHORT).show();
                            Bundle bundle = new Bundle();
                            bundle.putInt("jobId", jobId);
                            navController.navigate(R.id.JobDetailFragment, bundle);
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupTimeButton(Button button, String label, TimeSelectedListener listener) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        button.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selected.set(Calendar.MINUTE, minute);
                long timeMillis = selected.getTimeInMillis();
                listener.onTimeSelected(timeMillis);
                button.setText(label + ": " + timeFormat.format(selected.getTime()));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
        });
    }

    interface TimeSelectedListener {
        void onTimeSelected(long timeMillis);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}