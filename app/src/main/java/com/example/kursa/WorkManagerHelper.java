package com.example.kursa;

import android.content.Context;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class WorkManagerHelper {
    public static void scheduleDailyTask(Context context) {
        PeriodicWorkRequest dailyWorkRequest = new PeriodicWorkRequest.Builder(
                DailyWordWorker.class, 24, TimeUnit.HOURS)
                .build();

        WorkManager.getInstance(context).enqueue(dailyWorkRequest);
    }
}
