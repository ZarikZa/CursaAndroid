package com.example.kursa;

import android.os.Bundle;
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
/**
 * SentenceBuilderActivity — активность для сборки предложений на английском языке.
 * Позволяет пользователю составлять перевод русского предложения, выбирая слова из предложенного набора.
 * Использует Firestore для загрузки данных уровня и проверяет правильность собранного предложения.
 */
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

    /**
     * Инициализирует активность, настраивает элементы интерфейса и загружает данные уровня.
     *
     * @param savedInstanceState Сохраненное состояние активности
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sentence_builder);

        russianSentenceTextView = findViewById(R.id.russianSentenceTextView);
        selectedWordsRecyclerView = findViewById(R.id.selectedWordsRecyclerView);
        availableWordsRecyclerView = findViewById(R.id.availableWordsRecyclerView);
        checkButton = findViewById(R.id.checkButton);
        backBtm = findViewById(R.id.backButton);
        db = FirebaseFirestore.getInstance();

        selectedWordsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        availableWordsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        selectedWordsAdapter = new WordAdapterBuild(selectedWords, this::removeWord);
        availableWordsAdapter = new WordAdapterBuild(availableWords, this::addWord);

        selectedWordsRecyclerView.setAdapter(selectedWordsAdapter);
        availableWordsRecyclerView.setAdapter(availableWordsAdapter);

        levelId = getIntent().getStringExtra("levelId");
        nickname = getIntent().getStringExtra("nickname");
        if (levelId == null) {
            Toast.makeText(this, "Отсутствует ID уровня", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backBtm.setOnClickListener(v -> finish());

        loadLevel();

        checkButton.setOnClickListener(v -> checkTranslation());
    }

    /**
     * Загружает данные уровня из Firestore.
     */
    private void loadLevel() {
        db.collection("sentenceLevels")
                .document(levelId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        sentences = (List<Map<String, Object>>) documentSnapshot.get("sentences");
                        if (sentences != null && sentences.size() == 5) {
                            loadSentence(currentSentenceIndex);
                        } else {
                            Toast.makeText(this, "Неверные данные уровня", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Уровень не найден", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    /**
     * Загружает предложение по указанному индексу и подготавливает слова для сборки.
     *
     * @param index Индекс предложения
     */
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
        }
        availableWordsAdapter.updateWords(availableWords);
        selectedWordsAdapter.updateWords(selectedWords);
        availableWordsRecyclerView.post(() -> availableWordsRecyclerView.requestLayout());
    }

    /**
     * Добавляет слово в выбранные для составления предложения.
     *
     * @param word Слово для добавления
     */
    private void addWord(WordBuild word) {
        if (!word.isSelected()) {
            word.setSelected(true);
            selectedWords.add(word);
            selectedWordsAdapter.updateWords(selectedWords);
            availableWordsAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Удаляет слово из выбранных.
     *
     * @param word Слово для удаления
     */
    private void removeWord(WordBuild word) {
        word.setSelected(false);
        selectedWords.remove(word);
        selectedWordsAdapter.updateWords(selectedWords);
        availableWordsAdapter.notifyDataSetChanged();
    }

    /**
     * Проверяет правильность собранного предложения и переходит к следующему,
     * если ответ верный, или сбрасывает выбор, если ошибочный.
     */
    private void checkTranslation() {
        StringBuilder userTranslation = new StringBuilder();
        for (WordBuild word : selectedWords) {
            userTranslation.append(word.getText()).append(" ");
        }
        String userAnswer = userTranslation.toString().trim().toLowerCase();
        String normalizedCorrectTranslation = correctTranslation.trim().toLowerCase();

        if (userAnswer.equals(normalizedCorrectTranslation)) {
            currentSentenceIndex++;
            if (currentSentenceIndex < 5) {
                loadSentence(currentSentenceIndex);
            } else {
                Toast.makeText(this, "Уровень завершен!", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Неправильно! Правильный ответ: " + correctTranslation, Toast.LENGTH_LONG).show();
            selectedWords.clear();
            for (WordBuild word : availableWords) {
                word.setSelected(false);
            }
            selectedWordsAdapter.updateWords(selectedWords);
            availableWordsAdapter.notifyDataSetChanged();
        }
    }
}