package com.example.timetracker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AppDao {
    @Insert
    long insertEmployer(Employer employer);

    @Insert
    long insertJob(Job job);

    @Insert
    long insertTime(TimeRecord timeRecord);

    @Update
    void updateJob(Job job);

    @Update
    void updateEmployer(Employer employer);

    @Update
    void updateTime(TimeRecord timeRecord);

    @Delete
    void deleteJob(Job job);

    @Delete
    void deleteTime(TimeRecord timeRecord);

    @Delete
    void deleteEmployer(Employer employer);

    @Transaction
    @Query("SELECT * FROM job_table")
    List<JobWithEmployer> getAllJobsWithEmployer();

    @Query("SELECT * FROM time_table WHERE jobId = :jobId")
    List<TimeRecord> getTimeRecordsForJob(int jobId);

    @Transaction
    @Query("SELECT * FROM job_table WHERE id = :jobId")
    JobWithEmployer getJobWithEmployerById(int jobId);

    @Query("SELECT * FROM job_table WHERE id = :jobId")
    Job getJobById(int jobId);

    @Query("SELECT * FROM employer_table WHERE id = :employerId")
    Employer getEmployerById(int employerId);

    @Query("SELECT * FROM employer_table")
    List<Employer> getAllEmployers();
}