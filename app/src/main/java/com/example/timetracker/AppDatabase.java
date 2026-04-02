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
                            .fallbackToDestructiveMigration()
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

                // Existing sample data
                long acmeId = dao.insertEmployer(new Employer("Acme Corp"));
                long engJobId = dao.insertJob(new Job("Software Engineer", (int) acmeId));
                dao.insertTime(new TimeRecord((int) engJobId, System.currentTimeMillis() - 3600000, System.currentTimeMillis(), 0, 0, 0, 0));

                long globexId = dao.insertEmployer(new Employer("Globex Corporation"));
                long pmJobId = dao.insertJob(new Job("Project Manager", (int) globexId));
                dao.insertTime(new TimeRecord((int) pmJobId, System.currentTimeMillis() - 7200000, System.currentTimeMillis() - 3600000, 0, 0, 0, 0));

                // New sample data: 1 Employer, 1 Job, 2 Time entries
                long wayneId = dao.insertEmployer(new Employer("Wayne Enterprises"));
                long securityJobId = dao.insertJob(new Job("Security Consultant", (int) wayneId));
                
                // Time entry 1: 3 hours ago to 2 hours ago
                dao.insertTime(new TimeRecord((int) securityJobId, 
                        System.currentTimeMillis() - 10800000, 
                        System.currentTimeMillis() - 7200000, 0, 0, 0, 0));
                
                // Time entry 2: 1 hour ago to now
                dao.insertTime(new TimeRecord((int) securityJobId, 
                        System.currentTimeMillis() - 3600000, 
                        System.currentTimeMillis(), 0, 0, 0, 0));
            });
        }
    };
}