package com.example.kursa;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SentenceBuilderActivity extends AppCompatActivity {

    private TextView russianSentenceTextView;
    private RecyclerView selectedWordsRecyclerView, availableWordsRecyclerView;
    private Button checkButton;
    private WordAdapterBuild selectedWordsAdapter, availableWordsAdapter;
    private FirebaseFirestore db;
    private List<WordBuild> availableWords = new ArrayList<>();
    private List<WordBuild> selectedWords = new ArrayList<>();
    private String correctTranslation;
    private String levelId;
    private String nickname;
    private List<Map<String, Object>> sentences;
    private int currentSentenceIndex = 0;
    private ImageButton backBtm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sentence_builder);

        // Инициализация элементов
        russianSentenceTextView = findViewById(R.id.russianSentenceTextView);
        selectedWordsRecyclerView = findViewById(R.id.selectedWordsRecyclerView);
        availableWordsRecyclerView = findViewById(R.id.availableWordsRecyclerView);
        checkButton = findViewById(R.id.checkButton);
        backBtm = findViewById(R.id.backButton);
        db = FirebaseFirestore.getInstance();

        // Настройка RecyclerView
        selectedWordsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        availableWordsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        selectedWordsAdapter = new WordAdapterBuild(selectedWords, this::removeWord);
        availableWordsAdapter = new WordAdapterBuild(availableWords, this::addWord);

        selectedWordsRecyclerView.setAdapter(selectedWordsAdapter);
        availableWordsRecyclerView.setAdapter(availableWordsAdapter);

        // Получение данных из Intent
        levelId = getIntent().getStringExtra("levelId");
        nickname = getIntent().getStringExtra("nickname");
        if (levelId == null) {
            Toast.makeText(this, "Level ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backBtm.setOnClickListener(v -> {
            finish();
        });

        // Загрузка уровня
        loadLevel();

        checkButton.setOnClickListener(v -> checkTranslation());
    }

    private void loadLevel() {
        db.collection("sentenceLevels")
                .document(levelId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        sentences = (List<Map<String, Object>>) documentSnapshot.get("sentences");
                        if (sentences != null && sentences.size() == 5) {
                            Toast.makeText(this, "Sentences loaded: " + sentences.size(), Toast.LENGTH_SHORT).show();
                            loadSentence(currentSentenceIndex);
                        } else {
                            Toast.makeText(this, "Invalid level data: " + (sentences == null ? "null" : sentences.size()), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Level not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadSentence(int index) {
        Map<String, Object> sentence = sentences.get(index);
        String russianText = (String) sentence.get("russian");
        correctTranslation = (String) sentence.get("english");
        List<String> words = (List<String>) sentence.get("words");

        russianSentenceTextView.setText(russianText);
        availableWords.clear();
        selectedWords.clear();
        if (words != null && !words.isEmpty()) {
            for (String word : words) {
                availableWords.add(new WordBuild(word));
            }
            Collections.shuffle(availableWords);
            Toast.makeText(this, "Words added: " + availableWords.size(), Toast.LENGTH_SHORT).show();
            Log.d("SentenceBuilder", "Available words: " + availableWords.toString());
        } else {
            Toast.makeText(this, "No words available for sentence " + index, Toast.LENGTH_SHORT).show();
        }
        Log.d("SentenceBuilder", "Correct translation loaded: '" + correctTranslation + "'");
        availableWordsAdapter.updateWords(availableWords);
        selectedWordsAdapter.updateWords(selectedWords);
        availableWordsRecyclerView.post(() -> availableWordsRecyclerView.requestLayout());
    }

    private void addWord(WordBuild word) {
        if (!word.isSelected()) {
            word.setSelected(true);
            selectedWords.add(word);
            selectedWordsAdapter.updateWords(selectedWords);
            availableWordsAdapter.notifyDataSetChanged();
        }
    }

    private void removeWord(WordBuild word) {
        word.setSelected(false);
        selectedWords.remove(word);
        selectedWordsAdapter.updateWords(selectedWords);
        availableWordsAdapter.notifyDataSetChanged();
    }

    private void checkTranslation() {
        StringBuilder userTranslation = new StringBuilder();
        for (WordBuild word : selectedWords) {
            userTranslation.append(word.getText()).append(" ");
        }
        String userAnswer = userTranslation.toString().trim().toLowerCase();
        String normalizedCorrectTranslation = correctTranslation.trim().toLowerCase();
        Log.d("SentenceBuilder", "User answer: '" + userAnswer + "'");
        Log.d("SentenceBuilder", "Correct translation: '" + normalizedCorrectTranslation + "'");

        if (userAnswer.equals(normalizedCorrectTranslation)) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            currentSentenceIndex++;
            if (currentSentenceIndex < 5) {
                loadSentence(currentSentenceIndex);
            } else {
                Toast.makeText(this, "Level completed!", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Wrong! Correct answer: " + correctTranslation, Toast.LENGTH_LONG).show();
            selectedWords.clear();
            for (WordBuild word : availableWords) {
                word.setSelected(false);
            }
            selectedWordsAdapter.updateWords(selectedWords);
            availableWordsAdapter.notifyDataSetChanged();
        }
    }
}