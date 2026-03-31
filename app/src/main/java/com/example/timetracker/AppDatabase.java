package com.example.timetracker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Employer.class, Job.class, TimeRecord.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract AppDao appDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "time_tracker_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> {
                AppDao dao = INSTANCE.appDao();

                // Add sample data
                long employerId = dao.insertEmployer(new Employer("Acme Corp"));
                long jobId = dao.insertJob(new Job("Software Engineer", (int) employerId));
                dao.insertTime(new TimeRecord((int) jobId, System.currentTimeMillis() - 3600000, System.currentTimeMillis()));

                employerId = dao.insertEmployer(new Employer("Globex Corporation"));
                jobId = dao.insertJob(new Job("Project Manager", (int) employerId));
                dao.insertTime(new TimeRecord((int) jobId, System.currentTimeMillis() - 7200000, System.currentTimeMillis() - 3600000));
            });
        }
    };
}