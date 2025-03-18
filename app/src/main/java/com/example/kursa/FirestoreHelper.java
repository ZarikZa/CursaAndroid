package com.example.kursa;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreHelper {
    private final FirebaseFirestore db;

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public void checkAndUpdateData(Parser parser, WordSelector wordSelector) {
        String todayDate = DateHelper.getTodayDate();

        Task<DocumentSnapshot> task = db.collection("dailyWords")
                .document("current")
                .get();

        task.addOnCompleteListener(taskResult -> {
            if (taskResult.isSuccessful()) {
                DocumentSnapshot document = taskResult.getResult();
                if (document != null && document.exists()) {
                    String savedDate = document.getString("date");
                    if (savedDate != null && todayDate.equals(savedDate)) {
                        Log.d("FirestoreHelper", "Данные за сегодня уже обновлены.");
                    } else {
                        Log.d("FirestoreHelper", "Обновление данных...");
                        updateDailyWords(parser, wordSelector, todayDate);
                    }
                } else {
                    Log.d("FirestoreHelper", "Документ current не найден. Создание нового...");
                    updateDailyWords(parser, wordSelector, todayDate);
                }
            } else {
                Log.e("FirestoreHelper", "Ошибка при проверке даты: ", taskResult.getException());
            }
        });
    }

    private void updateDailyWords(Parser parser, WordSelector wordSelector, String todayDate) {
        new Thread(() -> {
            try {
                List<Word> words = parser.parseSkyengWords();

                List<Word> selectedWords = wordSelector.getRandomWords(words, 10);

                saveDailyWords(selectedWords, todayDate);
            } catch (IOException e) {
                Log.e("FirestoreHelper", "Ошибка при парсинге: ", e);
            }
        }).start();
    }

    private void saveDailyWords(List<Word> words, String todayDate) {
        Map<String, Object> data = new HashMap<>();
        data.put("date", todayDate);
        data.put("words", words);

        db.collection("dailyWords").document("current").set(data)
                .addOnSuccessListener(aVoid -> Log.d("FirestoreHelper", "Данные успешно обновлены."))
                .addOnFailureListener(e -> Log.e("FirestoreHelper", "Ошибка при обновлении: ", e));
    }
}