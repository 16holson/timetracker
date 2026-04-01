package com.example.timetracker;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.timetracker.databinding.FragmentJobDetailBinding;

import java.util.List;

public class JobDetailFragment extends Fragment {

    private FragmentJobDetailBinding binding;
    private int jobId;
    private Job currentJob;

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

        AppDatabase.databaseWriteExecutor.execute(() -> {
            currentJob = AppDatabase.getDatabase(getContext()).appDao().getJobById(jobId);
            List<TimeRecord> records = AppDatabase.getDatabase(getContext()).appDao().getTimeRecordsForJob(jobId);
            
            getActivity().runOnUiThread(() -> {
                if (currentJob != null) {
                    binding.editJobTitle.setText(currentJob.getTitle());
                }
                binding.recyclerviewTimes.setAdapter(new TimeAdapter(records));
                setupTitleEditor();
            });
        });

        binding.buttonSend.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("jobId", jobId);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_JobDetailFragment_to_SendFragment, bundle);
        });
    }

    private void setupTitleEditor() {
        binding.editJobTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (currentJob != null) {
                    String newTitle = s.toString();
                    currentJob.setTitle(newTitle);
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        AppDatabase.getDatabase(getContext()).appDao().updateJob(currentJob);
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