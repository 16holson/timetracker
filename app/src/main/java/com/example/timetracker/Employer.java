package com.example.timetracker;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "employer_table")
public class Employer {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;

    public Employer(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}