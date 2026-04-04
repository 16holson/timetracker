package com.example.timetracker;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "employer_table")
public class Employer {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String email;

    public Employer(String name, String email) {
        this.name = name;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}