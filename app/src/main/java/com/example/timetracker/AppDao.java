package com.example.timetracker;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AppDao {
    @Insert
    long insertEmployer(Employer employer);

    @Insert
    long insertJob(Job job);

    @Insert
    long insertTime(TimeRecord timeRecord);

    @Query("SELECT * FROM employer_table")
    List<Employer> getAllEmployers();

    @Query("SELECT * FROM job_table")
    List<Job> getAllJobs();

    @Query("SELECT * FROM time_table")
    List<TimeRecord> getAllTimeRecords();
}