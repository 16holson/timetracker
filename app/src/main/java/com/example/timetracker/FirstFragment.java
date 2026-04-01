package com.example.timetracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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

        binding.recyclerviewJobs.setLayoutManager(new LinearLayoutManager(getContext()));

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<JobWithEmployer> jobs = AppDatabase.getDatabase(getContext()).appDao().getAllJobsWithEmployer();
            getActivity().runOnUiThread(() -> {
                JobAdapter adapter = new JobAdapter(jobs, job -> {
                    Bundle bundle = new Bundle();
                    bundle.putInt("jobId", job.job.getId());
                    NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_JobDetailFragment, bundle);
                });
                binding.recyclerviewJobs.setAdapter(adapter);
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}