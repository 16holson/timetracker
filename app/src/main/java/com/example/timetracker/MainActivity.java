package com.example.timetracker;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.timetracker.databinding.ActivityMainBinding;
import com.example.timetracker.databinding.DialogAddJobBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private NavController navController;

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
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_time, null);
        EditText startInput = dialogView.findViewById(R.id.edit_start_minutes_ago);
        EditText durationInput = dialogView.findViewById(R.id.edit_duration_minutes);

        new AlertDialog.Builder(this)
                .setTitle("Add Time Entry")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    try {
                        int startMins = Integer.parseInt(startInput.getText().toString());
                        int durationMins = Integer.parseInt(durationInput.getText().toString());
                        
                        long endTime = System.currentTimeMillis();
                        long startTime = endTime - (startMins * 60000L);
                        long actualEndTime = startTime + (durationMins * 60000L);

                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            AppDatabase.getDatabase(getApplicationContext()).appDao()
                                    .insertTime(new TimeRecord(jobId, startTime, actualEndTime));
                            
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Time entry added", Toast.LENGTH_SHORT).show();
                                // Refresh current fragment
                                Bundle bundle = new Bundle();
                                bundle.putInt("jobId", jobId);
                                navController.navigate(R.id.JobDetailFragment, bundle);
                            });
                        });
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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