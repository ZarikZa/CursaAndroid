package com.example.kursa;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LevelAddActivity extends AppCompatActivity {

    private TextInputEditText levelIdEditText;
    private TextInputEditText russian1EditText, english1EditText;
    private TextInputEditText russian2EditText, english2EditText;
    private TextInputEditText russian3EditText, english3EditText;
    private TextInputEditText russian4EditText, english4EditText;
    private TextInputEditText russian5EditText, english5EditText;
    private Button saveButton;
    private FirebaseFirestore db;

    private static final List<String> EXTRA_WORDS = Arrays.asList(
            "eat", "run", "big", "small", "go", "see", "play", "house", "car", "yes",
            "no", "and", "or", "but", "with", "on", "in", "at", "from", "by"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_add);

        // Инициализация элементов
        levelIdEditText = findViewById(R.id.levelIdEditText);
        russian1EditText = findViewById(R.id.russian1EditText);
        english1EditText = findViewById(R.id.english1EditText);
        russian2EditText = findViewById(R.id.russian2EditText);
        english2EditText = findViewById(R.id.english2EditText);
        russian3EditText = findViewById(R.id.russian3EditText);
        english3EditText = findViewById(R.id.english3EditText);
        russian4EditText = findViewById(R.id.russian4EditText);
        english4EditText = findViewById(R.id.english4EditText);
        russian5EditText = findViewById(R.id.russian5EditText);
        english5EditText = findViewById(R.id.english5EditText);
        saveButton = findViewById(R.id.saveButton);
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
        db = FirebaseFirestore.getInstance();

        saveButton.setOnClickListener(v -> saveLevel());
    }

    private void saveLevel() {
        String levelId = levelIdEditText.getText().toString().trim();
        String[] russianSentences = {
                russian1EditText.getText().toString().trim(),
                russian2EditText.getText().toString().trim(),
                russian3EditText.getText().toString().trim(),
                russian4EditText.getText().toString().trim(),
                russian5EditText.getText().toString().trim()
        };
        String[] englishSentences = {
                english1EditText.getText().toString().trim().replaceAll("\\s+", " "),
                english2EditText.getText().toString().trim().replaceAll("\\s+", " "),
                english3EditText.getText().toString().trim().replaceAll("\\s+", " "),
                english4EditText.getText().toString().trim().replaceAll("\\s+", " "),
                english5EditText.getText().toString().trim().replaceAll("\\s+", " ")
        };

        // Проверка на пустые поля
        if (levelId.isEmpty()) {
            Toast.makeText(this, "Please fill in Level ID", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0; i < 5; i++) {
            if (russianSentences[i].isEmpty() || englishSentences[i].isEmpty()) {
                Toast.makeText(this, "Please fill in all Russian and English fields", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Проверка русских предложений (только русские символы)
        for (int i = 0; i < 5; i++) {
            if (!russianSentences[i].matches("^[а-яА-ЯёЁ\\s.,!?'-]+$")) {
                Toast.makeText(this, "Sentence " + (i + 1) + " (Russian) must contain only Russian characters", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Проверка английских предложений (только английские символы)
        for (int i = 0; i < 5; i++) {
            if (!englishSentences[i].matches("^[a-zA-Z\\s.,!?'-]+$")) {
                Toast.makeText(this, "Sentence " + (i + 1) + " (English) must contain only English characters", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Проверка существования ID в Firestore
        db.collection("sentenceLevels").document(levelId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Toast.makeText(this, "Level ID already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        List<Map<String, Object>> sentences = new ArrayList<>();
                        for (int i = 0; i < 5; i++) {
                            String russian = russianSentences[i];
                            String english = englishSentences[i];

                            List<String> words = new ArrayList<>(Arrays.asList(english.split("\\s+")));
                            int extraWordsCount = 2 + new Random().nextInt(3);
                            List<String> shuffledExtraWords = new ArrayList<>(EXTRA_WORDS);
                            Collections.shuffle(shuffledExtraWords);
                            for (int j = 0; j < extraWordsCount && j < shuffledExtraWords.size(); j++) {
                                String extraWord = shuffledExtraWords.get(j);
                                if (!words.contains(extraWord)) {
                                    words.add(extraWord);
                                }
                            }
                            Collections.shuffle(words);

                            Map<String, Object> sentence = new HashMap<>();
                            sentence.put("russian", russian);
                            sentence.put("english", english);
                            sentence.put("words", words);
                            sentences.add(sentence);
                        }

                        Map<String, Object> levelData = new HashMap<>();
                        levelData.put("sentences", sentences);

                        db.collection("sentenceLevels")
                                .document(levelId)
                                .set(levelData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Level added successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error adding level: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking level ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}