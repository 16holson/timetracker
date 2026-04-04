package com.example.timetracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetracker.databinding.FragmentFirstBinding;

import java.util.List;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private List<JobWithEmployer> jobList;
    private JobAdapter adapter;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerviewJobs.setLayoutManager(new LinearLayoutManager(getContext()));
        
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(requireContext()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                JobWithEmployer jobWithEmployer = jobList.get(position);
                showDeleteConfirmation(jobWithEmployer.job, position);
            }
        });
        itemTouchHelper.attachToRecyclerView(binding.recyclerviewJobs);

        refreshJobs();
    }

    public void refreshJobs() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            jobList = AppDatabase.getDatabase(getContext()).appDao().getAllJobsWithEmployer();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter = new JobAdapter(jobList, new JobAdapter.OnJobClickListener() {
                        @Override
                        public void onJobClick(JobWithEmployer job) {
                            Bundle bundle = new Bundle();
                            bundle.putInt("jobId", job.job.getId());
                            NavHostFragment.findNavController(FirstFragment.this)
                                    .navigate(R.id.action_FirstFragment_to_JobDetailFragment, bundle);
                        }

                        @Override
                        public void onDeleteClick(Job job) {
                            // Find position for current job to handle swipe if needed, 
                            // but this is used for long click now.
                            showDeleteConfirmation(job, -1);
                        }
                    });
                    binding.recyclerviewJobs.setAdapter(adapter);
                });
            }
        });
    }

    private void showDeleteConfirmation(Job job, int position) {
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
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (position != -1 && adapter != null) {
                        adapter.notifyItemChanged(position);
                    }
                })
                .setOnCancelListener(dialog -> {
                    if (position != -1 && adapter != null) {
                        adapter.notifyItemChanged(position);
                    }
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}