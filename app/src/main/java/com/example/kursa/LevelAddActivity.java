package com.example.kursa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SentenceBuilderAdminFragment extends Fragment {

    private EditText levelIdEditText;
    private EditText russian1EditText, english1EditText;
    private EditText russian2EditText, english2EditText;
    private EditText russian3EditText, english3EditText;
    private EditText russian4EditText, english4EditText;
    private EditText russian5EditText, english5EditText;
    private Button saveButton;
    private FirebaseFirestore db;

    private static final List<String> EXTRA_WORDS = Arrays.asList(
            "eat", "run", "big", "small", "go", "see", "play", "house", "car", "yes",
            "no", "and", "or", "but", "with", "on", "in", "at", "from", "by"
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_sentence_builder_admin, container, false);

        // Инициализация элементов
        levelIdEditText = view.findViewById(R.id.levelIdEditText);
        russian1EditText = view.findViewById(R.id.russian1EditText);
        english1EditText = view.findViewById(R.id.english1EditText);
        russian2EditText = view.findViewById(R.id.russian2EditText);
        english2EditText = view.findViewById(R.id.english2EditText);
        russian3EditText = view.findViewById(R.id.russian3EditText);
        english3EditText = view.findViewById(R.id.english3EditText);
        russian4EditText = view.findViewById(R.id.russian4EditText);
        english4EditText = view.findViewById(R.id.english4EditText);
        russian5EditText = view.findViewById(R.id.russian5EditText);
        english5EditText = view.findViewById(R.id.english5EditText);
        saveButton = view.findViewById(R.id.saveButton);
        db = FirebaseFirestore.getInstance();

        saveButton.setOnClickListener(v -> saveLevel());

        return view;
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
                english1EditText.getText().toString().trim(),
                english2EditText.getText().toString().trim(),
                english3EditText.getText().toString().trim(),
                english4EditText.getText().toString().trim(),
                english5EditText.getText().toString().trim()
        };

        // Проверка обязательных полей
        if (levelId.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in Level ID", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0; i < 5; i++) {
            if (russianSentences[i].isEmpty() || englishSentences[i].isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all Russian and English fields", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Формируем список предложений
        List<Map<String, Object>> sentences = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String russian = russianSentences[i];
            String english = englishSentences[i];

            // Формируем список слов из английского перевода
            List<String> words = new ArrayList<>(Arrays.asList(english.split("\\s+")));

            // Добавляем случайные лишние слова
            int extraWordsCount = 2 + new Random().nextInt(3); // 2–4 лишних слова
            List<String> shuffledExtraWords = new ArrayList<>(EXTRA_WORDS);
            Collections.shuffle(shuffledExtraWords);
            for (int j = 0; j < extraWordsCount && j < shuffledExtraWords.size(); j++) {
                String extraWord = shuffledExtraWords.get(j);
                if (!words.contains(extraWord)) {
                    words.add(extraWord);
                }
            }
            Collections.shuffle(words); // Перемешиваем слова

            // Создаём объект предложения
            Map<String, Object> sentence = new HashMap<>();
            sentence.put("russian", russian);
            sentence.put("english", english);
            sentence.put("words", words);
            sentences.add(sentence);
        }

        // Формируем данные уровня
        Map<String, Object> levelData = new HashMap<>();
        levelData.put("sentences", sentences);

        // Сохранение в Firestore в коллекцию sentenceLevels
        db.collection("sentenceLevels")
                .document(levelId)
                .set(levelData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Level saved successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error saving level: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearFields() {
        levelIdEditText.setText("");
        russian1EditText.setText("");
        english1EditText.setText("");
        russian2EditText.setText("");
        english2EditText.setText("");
        russian3EditText.setText("");
        english3EditText.setText("");
        russian4EditText.setText("");
        english4EditText.setText("");
        russian5EditText.setText("");
        english5EditText.setText("");
    }
}