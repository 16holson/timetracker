package com.example.timetracker;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "job_table",
        foreignKeys = @ForeignKey(entity = Employer.class,
                parentColumns = "id",
                childColumns = "employerId",
                onDelete = ForeignKey.CASCADE))
public class Job {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private int employerId;

    public Job(String title, int employerId) {
        this.title = title;
        this.employerId = employerId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getEmployerId() {
        return employerId;
    }

    public void setEmployerId(int employerId) {
        this.employerId = employerId;
    }
}