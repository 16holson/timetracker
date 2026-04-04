package com.example.timetracker;

import android.content.Context;
import android.widget.Toast;

public class TimeValidator {

    public static boolean isValid(Context context, long start, long end, long lunchStart, long lunchEnd, long travelStart, long travelEnd) {
        // Either Work Start or Travel Start must be provided
        if (start == 0 && travelStart == 0) {
            Toast.makeText(context, "At least a Work Start or Travel Start time is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Work time validation
        if (start != 0) {
            if (end != 0 && start >= end) {
                Toast.makeText(context, "Work start time must be before end time", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            if (end != 0) {
                Toast.makeText(context, "Work end time requires a work start time", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (lunchStart != 0 || lunchEnd != 0) {
                Toast.makeText(context, "Lunch times require a work start time", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Lunch validation
        if (lunchStart != 0 && lunchEnd != 0) {
            if (lunchStart >= lunchEnd) {
                Toast.makeText(context, "Lunch start must be before lunch end", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (lunchStart < start || (end != 0 && lunchEnd > end)) {
                Toast.makeText(context, "Lunch must be within work hours", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if (lunchEnd != 0 && lunchStart == 0) {
             Toast.makeText(context, "Lunch start time is required if end time is set", Toast.LENGTH_SHORT).show();
             return false;
        }

        // Travel validation
        if (travelStart != 0) {
            if (travelEnd != 0 && travelStart >= travelEnd) {
                Toast.makeText(context, "Travel start must be before travel end", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if (travelEnd != 0) {
            Toast.makeText(context, "Travel end time requires a travel start time", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}