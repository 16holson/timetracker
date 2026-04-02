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
    private long lunchStartTime;
    private long lunchEndTime;
    private long travelStartTime;
    private long travelEndTime;

    public TimeRecord(int jobId, long startTime, long endTime, long lunchStartTime, long lunchEndTime, long travelStartTime, long travelEndTime) {
        this.jobId = jobId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lunchStartTime = lunchStartTime;
        this.lunchEndTime = lunchEndTime;
        this.travelStartTime = travelStartTime;
        this.travelEndTime = travelEndTime;
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

    public long getLunchStartTime() {
        return lunchStartTime;
    }

    public void setLunchStartTime(long lunchStartTime) {
        this.lunchStartTime = lunchStartTime;
    }

    public long getLunchEndTime() {
        return lunchEndTime;
    }

    public void setLunchEndTime(long lunchEndTime) {
        this.lunchEndTime = lunchEndTime;
    }

    public long getTravelStartTime() {
        return travelStartTime;
    }

    public void setTravelStartTime(long travelStartTime) {
        this.travelStartTime = travelStartTime;
    }

    public long getTravelEndTime() {
        return travelEndTime;
    }

    public void setTravelEndTime(long travelEndTime) {
        this.travelEndTime = travelEndTime;
    }
}