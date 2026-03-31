package com.example.timetracker;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "time_table",
        foreignKeys = @ForeignKey(entity = Job.class,
                parentColumns = "id",
                childColumns = "jobId",
                onDelete = ForeignKey.CASCADE))
public class TimeRecord {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int jobId;
    private long startTime;
    private long endTime;

    public TimeRecord(int jobId, long startTime, long endTime) {
        this.jobId = jobId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}