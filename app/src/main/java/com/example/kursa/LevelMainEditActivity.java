package com.example.kursa;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * LevelMainEditActivity — активность для редактирования или удаления существующего уровня слов.
 * Позволяет изменить название уровня, 10 английских слов с переводами, сохраняет изменения
 * в Firestore в коллекции "levelsAll" и обновляет данные у всех пользователей в коллекции "levels".
 * Также поддерживает удаление уровня с последующей перенумерацией оставшихся уровней.
 */
public class LevelMainEditActivity extends AppCompatActivity {

    private EditText levelNameEditText;
    private EditText[] englishWords = new EditText[10];
    private EditText[] translations = new EditText[10];
    private Button saveLevelButton, deleteLevelButton;
    private FirebaseFirestore db;
    private String levelId;

    /**
     * Инициализирует активность, устанавливает layout, связывает элементы интерфейса,
     * загружает данные уровня по ID и настраивает обработчики для кнопок.
     *
     * @param savedInstanceState Сохраненное состояние активности
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_main_edit);

        db = FirebaseFirestore.getInstance();
        initializeViews();

        levelId = getIntent().getStringExtra("LEVEL_ID");
        if (levelId != null) {
            loadLevelData();
        }

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
        saveLevelButton.setOnClickListener(v -> saveLevelToFirestore());
        deleteLevelButton.setOnClickListener(v -> deleteLevel());
    }

    /**
     * Инициализирует элементы интерфейса (поля ввода и кнопки).
     */
    private void initializeViews() {
        levelNameEditText = findViewById(R.id.levelNameEditText);

        englishWords[0] = findViewById(R.id.englishWord1);
        englishWords[1] = findViewById(R.id.englishWord2);
        englishWords[2] = findViewById(R.id.englishWord3);
        englishWords[3] = findViewById(R.id.englishWord4);
        englishWords[4] = findViewById(R.id.englishWord5);
        englishWords[5] = findViewById(R.id.englishWord6);
        englishWords[6] = findViewById(R.id.englishWord7);
        englishWords[7] = findViewById(R.id.englishWord8);
        englishWords[8] = findViewById(R.id.englishWord9);
        englishWords[9] = findViewById(R.id.englishWord10);

        translations[0] = findViewById(R.id.translation1);
        translations[1] = findViewById(R.id.translation2);
        translations[2] = findViewById(R.id.translation3);
        translations[3] = findViewById(R.id.translation4);
        translations[4] = findViewById(R.id.translation5);
        translations[5] = findViewById(R.id.translation6);
        translations[6] = findViewById(R.id.translation7);
        translations[7] = findViewById(R.id.translation8);
        translations[8] = findViewById(R.id.translation9);
        translations[9] = findViewById(R.id.translation10);

        saveLevelButton = findViewById(R.id.saveLevelButton);
        deleteLevelButton = findViewById(R.id.deleteLevelButton);
    }

    /**
     * Загружает данные уровня из Firestore.
     */
    private void loadLevelData() {
        db.collection("levelsAll").document(levelId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        populateFields(documentSnapshot);
                    } else {
                        Toast.makeText(this, "Уровень не найден", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки уровня: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Load error: ", e);
                });
    }

    /**
     * Заполняет поля ввода данными из документа Firestore.
     *
     * @param documentSnapshot Документ с данными уровня
     */
    private void populateFields(DocumentSnapshot documentSnapshot) {
        String levelName = documentSnapshot.getString("levelName");
        Map<String, Object> details = (Map<String, Object>) documentSnapshot.get("details");

        if (levelName != null) {
            levelNameEditText.setText(levelName.replaceFirst("Уровень \\d+: ", ""));
        }

        if (details != null) {
            Map<String, String> words = (Map<String, String>) details.get("words");
            if (words != null) {
                int i = 0;
                for (Map.Entry<String, String> entry : words.entrySet()) {
                    if (i < 10) {
                        englishWords[i].setText(entry.getKey());
                        translations[i].setText(entry.getValue());
                        i++;
                    }
                }
            }
        }
    }

    /**
     * Сохраняет измененные данные уровня в Firestore.
     */
    private void saveLevelToFirestore() {
        String userLevelName = levelNameEditText.getText().toString().trim();

        if (userLevelName.isEmpty()) {
            levelNameEditText.setError("Введите название уровня");
            return;
        }

        Map<String, String> wordsMap = validateAndGetWords();
        if (wordsMap == null) return;

        try {
            int levelNumber = Integer.parseInt(levelId.replace("level", ""));
            String levelName = "Уровень " + levelNumber + ": " + userLevelName;

            Map<String, Object> level = createLevelData(levelName, wordsMap, levelNumber);
            updateLevelInFirestore(level);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Неверный формат номера уровня", Toast.LENGTH_SHORT).show();
            Log.e("Firestore", "Parse error: ", e);
        }
    }

    /**
     * Проверяет и собирает данные слов и переводов.
     *
     * @return Карта слов с переводами или null при ошибке
     */
    private Map<String, String> validateAndGetWords() {
        Map<String, String> wordsMap = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            String englishWord = englishWords[i].getText().toString().trim();
            String translation = translations[i].getText().toString().trim();

            if (englishWord.isEmpty() || translation.isEmpty()) {
                Toast.makeText(this, "Заполните все поля слов и переводов", Toast.LENGTH_SHORT).show();
                return null;
            }

            if (!englishWord.matches("^[a-zA-Z\\s.,!?'-]+$")) {
                Toast.makeText(this, "Слово " + (i + 1) + " должно быть на английском", Toast.LENGTH_SHORT).show();
                return null;
            }

            if (!translation.matches("^[а-яА-ЯёЁ\\s.,!?'-]+$")) {
                Toast.makeText(this, "Перевод " + (i + 1) + " должен быть на русском", Toast.LENGTH_SHORT).show();
                return null;
            }

            wordsMap.put(englishWord, translation);
        }
        return wordsMap;
    }

    /**
     * Создает структуру данных уровня.
     *
     * @param levelName   Название уровня
     * @param wordsMap    Карта слов с переводами
     * @param levelNumber Номер уровня
     * @return Данные уровня
     */
    private Map<String, Object> createLevelData(String levelName, Map<String, String> wordsMap, int levelNumber) {
        Map<String, Object> levelDetails = new HashMap<>();
        levelDetails.put("isUnlocked", levelNumber == 1);
        levelDetails.put("words", wordsMap);

        Map<String, Object> level = new HashMap<>();
        level.put("details", levelDetails);
        level.put("levelName", levelName);
        level.put("levelId", levelId);

        return level;
    }

    /**
     * Обновляет уровень в Firestore для всех пользователей.
     *
     * @param level Данные уровня
     */
    private void updateLevelInFirestore(Map<String, Object> level) {
        WriteBatch batch = db.batch();
        batch.set(db.collection("levelsAll").document(levelId), level);

        db.collection("users").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Task<?>> tasks = new ArrayList<>();
                    for (DocumentSnapshot userDoc : queryDocumentSnapshots) {
                        String nickname = userDoc.getString("nickname");
                        if (nickname != null && !nickname.isEmpty()) {
                            tasks.add(updateUserLevel(nickname, level));
                        }
                    }

                    Tasks.whenAllComplete(tasks)
                            .addOnCompleteListener(task -> {
                                batch.commit()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Уровень успешно обновлён", Toast.LENGTH_SHORT).show();
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Ошибка при обновлении: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            Log.e("Firestore", "Commit error: ", e);
                                        });
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка при получении пользователей", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Users fetch error: ", e);
                });
    }

    /**
     * Обновляет уровень для конкретного пользователя.
     *
     * @param nickname Никнейм пользователя
     * @param level    Данные уровня
     * @return Задача Firestore
     */
    private Task<Void> updateUserLevel(String nickname, Map<String, Object> level) {
        return db.collection("levels").document(nickname).get()
                .continueWithTask(task -> {
                    DocumentSnapshot doc = task.getResult();
                    WriteBatch userBatch = db.batch();

                    if (doc.exists()) {
                        List<Map<String, Object>> levels = getLevelsListFromDoc(doc);
                        updateOrAddLevel(levels, level);
                        userBatch.update(db.collection("levels").document(nickname), "levels", levels);
                    } else {
                        createNewUserLevel(userBatch, nickname, level);
                    }

                    return userBatch.commit();
                });
    }

    /**
     * Получает список уровней из документа.
     *
     * @param doc Документ Firestore
     * @return Список уровней
     */
    private List<Map<String, Object>> getLevelsListFromDoc(DocumentSnapshot doc) {
        List<Map<String, Object>> levels = new ArrayList<>();
        if (doc.contains("levels")) {
            levels = (List<Map<String, Object>>) doc.get("levels");
        }
        return levels != null ? levels : new ArrayList<>();
    }

    /**
     * Обновляет или добавляет уровень в список.
     *
     * @param levels   Список уровней
     * @param newLevel Новый уровень
     */
    private void updateOrAddLevel(List<Map<String, Object>> levels, Map<String, Object> newLevel) {
        boolean found = false;
        for (int i = 0; i < levels.size(); i++) {
            Map<String, Object> existingLevel = levels.get(i);
            if (existingLevel != null && levelId.equals(existingLevel.get("levelId"))) {
                levels.set(i, newLevel);
                found = true;
                break;
            }
        }
        if (!found) {
            levels.add(newLevel);
        }
    }

    /**
     * Создает новый документ уровней для пользователя.
     *
     * @param userBatch Батч для записи
     * @param nickname  Никнейм пользователя
     * @param level     Данные уровня
     */
    private void createNewUserLevel(WriteBatch userBatch, String nickname, Map<String, Object> level) {
        List<Map<String, Object>> levels = new ArrayList<>();
        levels.add(level);
        Map<String, Object> newData = new HashMap<>();
        newData.put("levels", levels);
        userBatch.set(db.collection("levels").document(nickname), newData);
    }

    /**
     * Удаляет уровень из Firestore и запускает перенумерацию.
     */
    private void deleteLevel() {
        if (levelId == null) {
            Toast.makeText(this, "Нет уровня для удаления", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("levelsAll").document(levelId).delete()
                .addOnSuccessListener(aVoid -> {
                    removeLevelFromAllUsers()
                            .addOnSuccessListener(aVoid1 -> {
                                renumberLevels();
                                Toast.makeText(this, "Уровень удален. Перенумерация...", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Ошибка удаления у пользователей", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка удаления уровня", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Удаляет уровень у всех пользователей.
     *
     * @return Задача Firestore
     */
    private Task<Void> removeLevelFromAllUsers() {
        return db.collection("users").get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    List<Task<Void>> tasks = new ArrayList<>();
                    for (DocumentSnapshot userDoc : task.getResult()) {
                        String nickname = userDoc.getString("nickname");
                        if (nickname != null) {
                            tasks.add(removeLevelFromUser(nickname));
                        }
                    }

                    return Tasks.whenAll(tasks);
                });
    }

    /**
     * Удаляет уровень у конкретного пользователя.
     *
     * @param nickname Никнейм пользователя
     * @return Задача Firestore
     */
    private Task<Void> removeLevelFromUser(String nickname) {
        return db.collection("levels").document(nickname).get()
                .continueWithTask(task -> {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        List<Map<String, Object>> levels = getLevelsListFromDoc(doc);
                        List<Map<String, Object>> updatedLevels = new ArrayList<>();

                        for (Map<String, Object> level : levels) {
                            if (level != null) {
                                String currentLevelId = (String) level.get("levelId");
                                if (currentLevelId != null && !currentLevelId.equals(levelId)) {
                                    updatedLevels.add(level);
                                }
                            }
                        }

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("levels", updatedLevels);
                        return db.collection("levels").document(nickname).set(updates);
                    }
                    return Tasks.forResult(null);
                });
    }

    /**
     * Перенумеровывает уровни после удаления.
     */
    private void renumberLevels() {
        db.collection("levelsAll").orderBy("levelId").get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DocumentSnapshot> levels = querySnapshot.getDocuments();
                    WriteBatch batch = db.batch();
                    Map<String, String> idMapping = new HashMap<>();

                    for (int i = 0; i < levels.size(); i++) {
                        String oldId = levels.get(i).getId();
                        String newId = "level" + (i + 1);

                        if (!oldId.equals(newId)) {
                            idMapping.put(oldId, newId);

                            Map<String, Object> levelData = new HashMap<>(levels.get(i).getData());
                            levelData.put("levelId", newId);

                            Map<String, Object> details = (Map<String, Object>) levelData.get("details");
                            if (details != null) {
                                details.put("isUnlocked", (i + 1) == 1); // Только первый уровень разблокирован
                            }

                            String oldName = (String) levelData.get("levelName");
                            if (oldName != null) {
                                String newName = "Уровень " + (i + 1) + ": " +
                                        oldName.replaceFirst("Уровень \\d+: ", "");
                                levelData.put("levelName", newName);
                            }

                            batch.set(db.collection("levelsAll").document(newId), levelData);
                            batch.delete(db.collection("levelsAll").document(oldId));
                        }
                    }

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                if (!idMapping.isEmpty()) {
                                    updateAllUsersLevels(idMapping);
                                }
                                Toast.makeText(this, "Перенумерация завершена", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Ошибка перенумерации", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка получения уровней", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Обновляет ID уровней у всех пользователей.
     *
     * @param idMapping Карта соответствия старых и новых ID
     */
    private void updateAllUsersLevels(Map<String, String> idMapping) {
        db.collection("users").get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot userDoc : querySnapshot) {
                        String nickname = userDoc.getString("nickname");
                        if (nickname != null) {
                            updateUserLevels(nickname, idMapping);
                        }
                    }
                });
    }

    /**
     * Обновляет уровни конкретного пользователя.
     *
     * @param nickname  Никнейм пользователя
     * @param idMapping Карта соответствия ID
     */
    private void updateUserLevels(String nickname, Map<String, String> idMapping) {
        db.collection("levels").document(nickname).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("levels")) {
                        List<Map<String, Object>> levels = (List<Map<String, Object>>) doc.get("levels");
                        List<Map<String, Object>> updatedLevels = new ArrayList<>();

                        for (Map<String, Object> level : levels) {
                            if (level != null) {
                                String oldId = (String) level.get("levelId");
                                String newId = idMapping.get(oldId);

                                if (newId != null) {
                                    Map<String, Object> updatedLevel = new HashMap<>(level);
                                    updatedLevel.put("levelId", newId);

                                    Map<String, Object> details = (Map<String, Object>) updatedLevel.get("details");
                                    if (details != null) {
                                        int levelNum = extractLevelNumber(newId);
                                        details.put("isUnlocked", levelNum == 1); // Только первый уровень
                                    }

                                    String oldName = (String) updatedLevel.get("levelName");
                                    if (oldName != null) {
                                        int newNum = extractLevelNumber(newId);
                                        String newName = "Уровень " + newNum + ": " +
                                                oldName.replaceFirst("Уровень \\d+: ", "");
                                        updatedLevel.put("levelName", newName);
                                    }

                                    updatedLevels.add(updatedLevel);
                                } else {
                                    updatedLevels.add(level);
                                }
                            }
                        }

                        db.collection("levels").document(nickname)
                                .update("levels", updatedLevels);
                    }
                });
    }

    /**
     * Извлекает номер уровня из ID.
     *
     * @param levelId ID уровня
     * @return Номер уровня
     */
    private int extractLevelNumber(String levelId) {
        try {
            return Integer.parseInt(levelId.replace("level", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}