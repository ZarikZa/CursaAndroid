package com.example.kursa;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SlovarActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private WordAdapter adapter;
    private List<WordLevel> wordList;
    private List<WordLevel> allWordsList;
    private List<WordLevel> hardWordsList;
    private String userNickname;
    private Button btnAllWords, btnHardWords;
    private ImageButton backBtm;
    private FirebaseFirestore db;
    private boolean showingHardWords = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frame_slorar);

        db = FirebaseFirestore.getInstance();
        userNickname = getIntent().getStringExtra("USER_NICKNAME");
        if (userNickname == null) {
            Toast.makeText(this, "Error: Missing nickname", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupRecyclerView();
        setupButtons();
        loadWordsFromFirebase();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.learnedWordsList);
        btnAllWords = findViewById(R.id.btnAllWords);
        btnHardWords = findViewById(R.id.btnHardWords);
        backBtm = findViewById(R.id.backButton);
        wordList = new ArrayList<>();
        allWordsList = new ArrayList<>();
        hardWordsList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new WordAdapter(wordList, this::deleteHardWord);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupButtons() {
        btnAllWords.setOnClickListener(v -> {
            showingHardWords = false;
            showAllWords();
        });
        btnHardWords.setOnClickListener(v -> {
            showingHardWords = true;
            showHardWords();
        });
        backBtm.setOnClickListener(v -> finish());
    }

    private void loadWordsFromFirebase() {
        db.collection("usersLearnedWords").document(userNickname)
                .get()
                .addOnSuccessListener(this::processWords)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка загрузки слов: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void processWords(DocumentSnapshot documentSnapshot) {
        if (!documentSnapshot.exists()) {
            Toast.makeText(this, "Документ пользователя не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> wordsMap = (Map<String, Object>) documentSnapshot.get("words");
        if (wordsMap == null || wordsMap.isEmpty()) {
            Toast.makeText(this, "Словарь пуст", Toast.LENGTH_SHORT).show();
            return;
        }

        allWordsList.clear();
        hardWordsList.clear();

        for (Map.Entry<String, Object> entry : wordsMap.entrySet()) {
            String englishWord = entry.getKey();
            WordLevel word = createWordFromEntry(englishWord, entry.getValue());

            if (word != null) {
                allWordsList.add(word);
                if (word.isHard()) {
                    hardWordsList.add(word);
                }
            }
        }

        if (showingHardWords) {
            showHardWords();
        } else {
            showAllWords();
        }
    }

    private WordLevel createWordFromEntry(String englishWord, Object value) {
        if (value instanceof String) {
            return new WordLevel(englishWord, (String) value, false);
        } else if (value instanceof Map) {
            Map<String, Object> wordData = (Map<String, Object>) value;
            String translation = (String) wordData.get("translation");
            Boolean isHard = (Boolean) wordData.get("hard");
            return new WordLevel(englishWord, translation, isHard != null && isHard);
        }
        return null;
    }

    private void showAllWords() {
        updateWordList(allWordsList);
        updateButtonColors(true);
    }

    private void showHardWords() {
        updateWordList(hardWordsList);
        updateButtonColors(false);
    }

    private void updateWordList(List<WordLevel> words) {
        wordList.clear();
        wordList.addAll(words);
        adapter.notifyDataSetChanged();
    }

    private void updateButtonColors(boolean isAllWordsSelected) {
        btnAllWords.setBackgroundTintList(getResources().getColorStateList(
                isAllWordsSelected ? R.color.selected_button_color : R.color.unselected_button_color));
        btnHardWords.setBackgroundTintList(getResources().getColorStateList(
                isAllWordsSelected ? R.color.unselected_button_color : R.color.selected_button_color));
    }

    private void deleteHardWord(WordLevel word) {
        db.collection("usersLearnedWords").document(userNickname)
                .update("words." + word.getEnglish() + ".hard", false)
                .addOnSuccessListener(aVoid -> {
                    word.setHard(false);
                    hardWordsList.remove(word);

                    // Обновляем текущий список в зависимости от того, что показывается
                    if (showingHardWords) {
                        wordList.remove(word);
                    }
                    adapter.notifyDataSetChanged();

                    Toast.makeText(this, "Слово удалено из сложных", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка удаления: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}