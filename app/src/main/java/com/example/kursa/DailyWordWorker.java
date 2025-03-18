package com.example.kursa;

import androidx.work.Worker;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

public class DailyWordWorker extends Worker {
    public DailyWordWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

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