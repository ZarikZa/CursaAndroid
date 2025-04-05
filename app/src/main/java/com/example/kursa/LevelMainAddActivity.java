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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelMainAddActivity extends AppCompatActivity {

    private EditText levelNameEditText;
    private EditText[] englishWords = new EditText[10];
    private EditText[] translations = new EditText[10];
    private Button addLevelButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_main_add);
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
        db = FirebaseFirestore.getInstance();
        initializeViews();
        addLevelButton.setOnClickListener(v -> addLevelToFirestore());
    }

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

        addLevelButton = findViewById(R.id.addLevelButton);
    }

    private void addLevelToFirestore() {
        String levelName = levelNameEditText.getText().toString().trim();
        if (levelName.isEmpty()) {
            levelNameEditText.setError("Введите название уровня");
            return;
        }

        Map<String, String> wordsMap = collectWordsData();
        if (wordsMap == null) return;

        generateLevelId(levelId -> {
            createAndSaveLevel(levelId, levelName, wordsMap);
        });
    }

    private Map<String, String> collectWordsData() {
        Map<String, String> wordsMap = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            String englishWord = englishWords[i].getText().toString().trim();
            String translation = translations[i].getText().toString().trim();

            // Проверка на пустые поля
            if (englishWord.isEmpty() || translation.isEmpty()) {
                Toast.makeText(this, "Заполните все поля слов и переводов", Toast.LENGTH_SHORT).show();
                return null;
            }

            // Проверка английского слова
            if (!englishWord.matches("^[a-zA-Z\\s.,!?'-]+$")) {
                Toast.makeText(this, "Слово " + (i + 1) + " должно быть на английском", Toast.LENGTH_SHORT).show();
                return null;
            }

            // Проверка русского перевода
            if (!translation.matches("^[а-яА-ЯёЁ\\s.,!?'-]+$")) {
                Toast.makeText(this, "Перевод " + (i + 1) + " должен быть на русском", Toast.LENGTH_SHORT).show();
                return null;
            }

            wordsMap.put(englishWord, translation);
        }
        return wordsMap;
    }

    private void createAndSaveLevel(String levelId, String levelName, Map<String, String> wordsMap) {
        int levelNumber = Integer.parseInt(levelId.replace("level", ""));
        String fullLevelName = "Уровень " + levelNumber + ": " + levelName;

        Map<String, Object> levelData = new HashMap<>();
        levelData.put("levelName", fullLevelName);
        levelData.put("levelId", levelId);

        Map<String, Object> details = new HashMap<>();
        details.put("isUnlocked", levelNumber == 1);
        details.put("words", wordsMap);
        levelData.put("details", details);

        db.collection("levelsAll").document(levelId)
                .set(levelData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Уровень создан", Toast.LENGTH_SHORT).show();
                    distributeLevelToUsers(levelId, levelData);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка создания уровня", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error: ", e);
                });
    }

    private void distributeLevelToUsers(String levelId, Map<String, Object> levelData) {
        db.collection("users").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Task<Void>> tasks = new ArrayList<>();
                    for (DocumentSnapshot userDoc : queryDocumentSnapshots) {
                        String nickname = userDoc.getString("nickname");
                        if (nickname != null && !nickname.isEmpty()) {
                            tasks.add(addLevelToUser(nickname, levelData));
                        }
                    }

                    Tasks.whenAll(tasks)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Firestore", "Уровень добавлен всем пользователям");
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Ошибка добавления уровня", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка получения пользователей", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error: ", e);
                });
    }

    private Task<Void> addLevelToUser(String nickname, Map<String, Object> levelData) {
        return db.collection("levels").document(nickname)
                .update("levels", FieldValue.arrayUnion(levelData))
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        return task;
                    }
                    if (task.getException() != null &&
                            task.getException().getMessage() != null &&
                            task.getException().getMessage().contains("No document to update")) {

                        Map<String, Object> newUserLevels = new HashMap<>();
                        List<Map<String, Object>> levelsList = new ArrayList<>();
                        levelsList.add(levelData);
                        newUserLevels.put("levels", levelsList);

                        return db.collection("levels").document(nickname).set(newUserLevels);
                    }
                    return task;
                });
    }

    private void generateLevelId(OnLevelIdGeneratedListener listener) {
        db.collection("levelsAll").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int maxLevel = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            String docId = doc.getId();
                            if (docId.startsWith("level")) {
                                int levelNum = Integer.parseInt(docId.substring(5));
                                if (levelNum > maxLevel) {
                                    maxLevel = levelNum;
                                }
                            }
                        } catch (NumberFormatException e) {
                            Log.w("Firestore", "Invalid level ID format", e);
                        }
                    }
                    listener.onLevelIdGenerated("level" + (maxLevel + 1));
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting levels", e);
                    listener.onLevelIdGenerated("level1");
                });
    }

    interface OnLevelIdGeneratedListener {
        void onLevelIdGenerated(String levelId);
    }
}