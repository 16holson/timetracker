package com.example.timetracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.timetracker.databinding.FragmentFirstBinding;

import java.util.List;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        binding.recyclerviewJobs.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshJobs();
    }

    public void refreshJobs() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<JobWithEmployer> jobs = AppDatabase.getDatabase(getContext()).appDao().getAllJobsWithEmployer();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    JobAdapter adapter = new JobAdapter(jobs, new JobAdapter.OnJobClickListener() {
                        @Override
                        public void onJobClick(JobWithEmployer job) {
                            Bundle bundle = new Bundle();
                            bundle.putInt("jobId", job.job.getId());
                            NavHostFragment.findNavController(FirstFragment.this)
                                    .navigate(R.id.action_FirstFragment_to_JobDetailFragment, bundle);
                        }

                        @Override
                        public void onDeleteClick(Job job) {
                            showDeleteConfirmation(job);
                        }
                    });
                    binding.recyclerviewJobs.setAdapter(adapter);
                });
            }
        });
    }

    private void showDeleteConfirmation(Job job) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Job")
                .setMessage("Are you sure you want to delete this job at " + job.getLocation() + "? This will also delete all associated time records.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        AppDatabase.getDatabase(getContext()).appDao().deleteJob(job);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Job deleted", Toast.LENGTH_SHORT).show();
                                refreshJobs();
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}