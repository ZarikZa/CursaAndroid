package com.example.kursa;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * FirestoreHelper — утилитный класс для работы с Firestore.
 * Проверяет и обновляет ежедневные слова в коллекции "dailyWords",
 * используя парсер и селектор слов. Поддерживает асинхронное обновление
 * и уведомление о результате через UpdateListener.
 */
public class FirestoreHelper {
    private static final String TAG = "FirestoreHelper";
    private final FirebaseFirestore db;
    private UpdateListener updateListener;

    /**
     * Интерфейс для уведомления о завершении обновления данных.
     */
    public interface UpdateListener {
        void onUpdateComplete(boolean success);
    }

    /**
     * Инициализирует FirestoreHelper и подключается к Firestore.
     */
    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
        Log.d(TAG, "FirestoreHelper initialized");
    }

    /**
     * Устанавливает слушатель для получения уведомлений об обновлении.
     *
     * @param listener Слушатель обновления
     */
    public void setUpdateListener(UpdateListener listener) {
        this.updateListener = listener;
    }

    /**
     * Проверяет необходимость обновления ежедневных слов и запускает процесс,
     * если данные устарели или отсутствуют.
     *
     * @param parser      Парсер для получения слов
     * @param wordSelector Селектор для выбора случайных слов
     */
    public void checkAndUpdateData(Parser parser, WordSelector wordSelector) {
        String todayDate = DateHelper.getTodayDate();
        Log.d(TAG, "Checking data for date: " + todayDate);

        db.collection("dailyWords")
                .document("current")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String savedDate = document.getString("date");
                            if (savedDate != null && todayDate.equals(savedDate)) {
                                Log.d(TAG, "Data already updated for today");
                                if (updateListener != null) {
                                    updateListener.onUpdateComplete(true);
                                }
                            } else {
                                Log.d(TAG, "Data needs update");
                                updateDailyWords(parser, wordSelector, todayDate);
                            }
                        } else {
                            Log.d(TAG, "Document doesn't exist");
                            updateDailyWords(parser, wordSelector, todayDate);
                        }
                    } else {
                        Log.e(TAG, "Error checking date", task.getException());
                        if (updateListener != null) {
                            updateListener.onUpdateComplete(false);
                        }
                    }
                });
    }

    /**
     * Выполняет парсинг слов и обновляет данные в Firestore в отдельном потоке.
     *
     * @param parser      Парсер для получения слов
     * @param wordSelector Селектор для выбора слов
     * @param todayDate   Текущая дата
     */
    private void updateDailyWords(Parser parser, WordSelector wordSelector, String todayDate) {
        new Thread(() -> {
            try {
                List<Word> words = parser.parseSkyengWords();
                List<Word> selectedWords = wordSelector.getRandomWords(words, 10);
                saveDailyWords(selectedWords, todayDate);
            } catch (IOException e) {
                Log.e(TAG, "Parsing error", e);
                if (updateListener != null) {
                    updateListener.onUpdateComplete(false);
                }
            }
        }).start();
    }

    /**
     * Сохраняет список слов и дату в Firestore.
     *
     * @param words     Список слов для сохранения
     * @param todayDate Текущая дата
     */
    private void saveDailyWords(List<Word> words, String todayDate) {
        Map<String, Object> data = new HashMap<>();
        data.put("date", todayDate);
        data.put("words", words);

        db.collection("dailyWords")
                .document("current")
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Data successfully updated");
                    if (updateListener != null) {
                        updateListener.onUpdateComplete(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Update failed", e);
                    if (updateListener != null) {
                        updateListener.onUpdateComplete(false);
                    }
                });
    }
}