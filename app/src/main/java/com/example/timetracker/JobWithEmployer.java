package com.example.timetracker;

import androidx.room.Embedded;
import androidx.room.Relation;

public class JobWithEmployer {
    @Embedded
    public Job job;

    @Relation(
            parentColumn = "employerId",
            entityColumn = "id"
    )
    public Employer employer;
}