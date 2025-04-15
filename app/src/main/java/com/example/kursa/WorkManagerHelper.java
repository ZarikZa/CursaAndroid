package com.example.kursa;

import android.content.Context;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * Вспомогательный класс для настройки периодических фоновых задач
 */
public class WorkManagerHelper {
    /**
     * Запланировать ежедневную фоновую задачу
     * @param context контекст приложения
     */
    public static void scheduleDailyTask(Context context) {
        PeriodicWorkRequest dailyWorkRequest = new PeriodicWorkRequest.Builder(
                DailyWordWorker.class, 24, TimeUnit.HOURS)
                .build();

        WorkManager.getInstance(context).enqueue(dailyWorkRequest);
    }
}
