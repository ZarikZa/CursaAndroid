
package com.example.kursa;

import androidx.work.Worker;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;
/**
 * DailyWordWorker — фоновый рабочий процесс для обновления ежедневного слова.
 * Выполняет парсинг данных, выбор слова и обновление данных в Firestore.
 * Используется WorkManager для планирования и выполнения задачи.
 */
public class DailyWordWorker extends Worker {
    /**
     * Конструктор для инициализации рабочего процесса.
     *
     * @param context Контекст приложения
     * @param params  Параметры выполнения задачи
     */
    public DailyWordWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    /**
     * Выполняет основную логику задачи: парсит данные, выбирает слово и обновляет Firestore.
     *
     * @return Result.success() при успешном выполнении, Result.failure() при ошибке
     */
    @NonNull
    @Override
    public Result doWork() {
        try {
            Parser parser = new Parser();
            WordSelector wordSelector = new WordSelector();
            FirestoreHelper firestoreHelper = new FirestoreHelper();

            firestoreHelper.checkAndUpdateData(parser, wordSelector);

            return Result.success();
        } catch (Exception e) {
            return Result.failure();
        }
    }
}