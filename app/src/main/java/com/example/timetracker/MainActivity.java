package com.example.timetracker;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.timetracker.databinding.ActivityMainBinding;
import com.example.timetracker.databinding.DialogAddJobBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private NavController navController;

    private long startTime, endTime, lunchStart, lunchEnd, travelStart, travelEnd;
    private Calendar baseCalendar = Calendar.getInstance();
    private AlertDialog manageEmployersDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

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
            // Invalidate options menu to update icons when fragment changes
            invalidateOptionsMenu();
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
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Employer> employers = AppDatabase.getDatabase(getApplicationContext()).appDao().getAllEmployers();
            List<String> employerNames = new ArrayList<>();
            for (Employer e : employers) {
                employerNames.add(e.getName());
            }

            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, employerNames);
                dialogBinding.editEmployerName.setAdapter(adapter);
                
                // Show dropdown when focused
                dialogBinding.editEmployerName.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) dialogBinding.editEmployerName.showDropDown();
                });
                
                dialogBinding.editEmployerName.setOnClickListener(v -> dialogBinding.editEmployerName.showDropDown());

                dialogBinding.editEmployerName.setOnItemClickListener((parent, view, position, id) -> {
                    String selectedName = (String) parent.getItemAtPosition(position);
                    for (Employer e : employers) {
                        if (e.getName().equals(selectedName)) {
                            dialogBinding.editEmployerName.setText(e.getName());
                            dialogBinding.editEmployerEmail.setText(e.getEmail());
                            break;
                        }
                    }
                });
            });
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add New Job")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Create", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String employerName = dialogBinding.editEmployerName.getText().toString().trim();
            String employerEmail = dialogBinding.editEmployerEmail.getText().toString().trim();
            String location = dialogBinding.editJobLocation.getText().toString().trim();

            if (!employerName.isEmpty() && !employerEmail.isEmpty() && !location.isEmpty()) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    AppDao dao = AppDatabase.getDatabase(getApplicationContext()).appDao();
                    
                    List<Employer> existing = dao.getAllEmployers();
                    int empId = -1;
                    for (Employer e : existing) {
                        if (e.getName().equalsIgnoreCase(employerName)) {
                            empId = e.getId();
                            if (!e.getEmail().equals(employerEmail)) {
                                e.setEmail(employerEmail);
                                dao.updateEmployer(e);
                            }
                            break;
                        }
                    }
                    
                    if (empId == -1) {
                        empId = (int) dao.insertEmployer(new Employer(employerName, employerEmail));
                    }
                    
                    long jobId = dao.insertJob(new Job(location, empId));

                    runOnUiThread(() -> {
                        Bundle bundle = new Bundle();
                        bundle.putInt("jobId", (int) jobId);
                        navController.navigate(R.id.action_FirstFragment_to_JobDetailFragment, bundle);
                        dialog.dismiss();
                    });
                });
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddTimeDialog(int jobId) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_time, null);
        
        startTime = endTime = lunchStart = lunchEnd = travelStart = travelEnd = 0;
        baseCalendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        Button btnDate = dialogView.findViewById(R.id.btn_date);
        btnDate.setText("Date: " + dateFormat.format(baseCalendar.getTime()));

        btnDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                baseCalendar.set(Calendar.YEAR, year);
                baseCalendar.set(Calendar.MONTH, month);
                baseCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                btnDate.setText("Date: " + dateFormat.format(baseCalendar.getTime()));
                
                // Update all set times to the new date
                if (startTime != 0) startTime = updateDateOfTime(startTime, baseCalendar);
                if (endTime != 0) endTime = updateDateOfTime(endTime, baseCalendar);
                if (lunchStart != 0) lunchStart = updateDateOfTime(lunchStart, baseCalendar);
                if (lunchEnd != 0) lunchEnd = updateDateOfTime(lunchEnd, baseCalendar);
                if (travelStart != 0) travelStart = updateDateOfTime(travelStart, baseCalendar);
                if (travelEnd != 0) travelEnd = updateDateOfTime(travelEnd, baseCalendar);
            }, baseCalendar.get(Calendar.YEAR), baseCalendar.get(Calendar.MONTH), baseCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        setupTimeButton(dialogView.findViewById(R.id.btn_start_time), "Start", 0, t -> startTime = t);
        setupTimeButton(dialogView.findViewById(R.id.btn_end_time), "End", 0, t -> endTime = t);
        setupTimeButton(dialogView.findViewById(R.id.btn_lunch_start), "Lunch Start", 0, t -> lunchStart = t);
        setupTimeButton(dialogView.findViewById(R.id.btn_lunch_end), "Lunch End", 0, t -> lunchEnd = t);
        setupTimeButton(dialogView.findViewById(R.id.btn_travel_start), "Travel Start", 0, t -> travelStart = t);
        setupTimeButton(dialogView.findViewById(R.id.btn_travel_end), "Travel End", 0, t -> travelEnd = t);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Time Entry")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (TimeValidator.isValid(this, startTime, endTime, lunchStart, lunchEnd, travelStart, travelEnd)) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    AppDatabase.getDatabase(getApplicationContext()).appDao()
                            .insertTime(new TimeRecord(jobId, startTime, endTime, lunchStart, lunchEnd, travelStart, travelEnd));
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Time entry added", Toast.LENGTH_SHORT).show();
                        Bundle bundle = new Bundle();
                        bundle.putInt("jobId", jobId);
                        navController.navigate(R.id.JobDetailFragment, bundle);
                        dialog.dismiss();
                    });
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

    private void setupTimeButton(Button button, String label, long initialTime, TimeSelectedListener listener) {
        final long[] currentTime = {initialTime};
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        
        button.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(baseCalendar.getTimeInMillis()); // Start with base date
            if (currentTime[0] != 0) calendar.setTimeInMillis(currentTime[0]);
            
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                currentTime[0] = calendar.getTimeInMillis();
                listener.onTimeSelected(currentTime[0]);
                button.setText(label + ": " + timeFormat.format(calendar.getTime()));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
        });
    }

    private void showManageEmployersDialog() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Employer> employers = AppDatabase.getDatabase(getApplicationContext()).appDao().getAllEmployers();
            runOnUiThread(() -> {
                if (manageEmployersDialog != null && manageEmployersDialog.isShowing()) {
                    manageEmployersDialog.dismiss();
                }

                ListView listView = new ListView(this);
                EmployerManagementAdapter adapter = new EmployerManagementAdapter(this, employers);
                listView.setAdapter(adapter);

                manageEmployersDialog = new AlertDialog.Builder(this)
                        .setTitle("Manage Employers")
                        .setView(listView)
                        .setPositiveButton("Close", null)
                        .create();
                manageEmployersDialog.show();
            });
        });
    }

    private class EmployerManagementAdapter extends ArrayAdapter<Employer> {
        public EmployerManagementAdapter(Context context, List<Employer> employers) {
            super(context, 0, employers);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_manage_employer, parent, false);
            }

            Employer employer = getItem(position);
            TextView name = convertView.findViewById(R.id.employer_name_text);
            TextView email = convertView.findViewById(R.id.employer_email_text);
            ImageButton deleteBtn = convertView.findViewById(R.id.btn_delete_employer);

            name.setText(employer.getName());
            email.setText(employer.getEmail());
            deleteBtn.setOnClickListener(v -> showDeleteEmployerConfirmation(employer));

            return convertView;
        }
    }

    private void showDeleteEmployerConfirmation(Employer employer) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Employer")
                .setMessage("Are you sure you want to delete " + employer.getName() + "? This will also delete ALL jobs and time records associated with this employer.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        AppDatabase.getDatabase(getApplicationContext()).appDao().deleteEmployer(employer);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Employer deleted", Toast.LENGTH_SHORT).show();
                            
                            // Refresh FirstFragment if it's currently showing
                            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
                            if (currentFragment instanceof NavHostFragment) {
                                Fragment firstFragment = currentFragment.getChildFragmentManager().getFragments().get(0);
                                if (firstFragment instanceof FirstFragment) {
                                    ((FirstFragment) firstFragment).refreshJobs();
                                }
                            }

                            showManageEmployersDialog(); // Refresh the list
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem sendItem = menu.findItem(R.id.action_send);
        MenuItem manageItem = menu.findItem(R.id.action_manage_employers);
        
        if (navController != null && navController.getCurrentDestination() != null) {
            int destId = navController.getCurrentDestination().getId();
            if (sendItem != null) sendItem.setVisible(destId == R.id.JobDetailFragment);
            if (manageItem != null) manageItem.setVisible(destId == R.id.FirstFragment);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_send) {
            // Trigger navigation to SendFragment from JobDetailFragment
            Bundle args = navController.getCurrentBackStackEntry().getArguments();
            if (args != null && args.containsKey("jobId")) {
                int jobId = args.getInt("jobId");
                Bundle bundle = new Bundle();
                bundle.putInt("jobId", jobId);
                navController.navigate(R.id.action_JobDetailFragment_to_SendFragment, bundle);
            }
            return true;
        } else if (id == R.id.action_manage_employers) {
            showManageEmployersDialog();
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