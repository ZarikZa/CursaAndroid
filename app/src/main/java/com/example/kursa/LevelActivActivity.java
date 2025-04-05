package com.example.kursa;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LevelActivActivity extends AppCompatActivity {
    private static final String TAG = "LevelActivActivity";

    private FrameLayout draggableView;
    private TextView contentTextView, levelNameTextView, translationTextView;
    private Button leftButton, rightButton, completeButton, checkTranslationButton;
    private LinearLayout linearBtm;
    private List<Word> unlearnedWords = new ArrayList<>();
    private List<WordLevel> learnedWords = new ArrayList<>();
    private int currentIndex = 0;
    private EditText inputTranslationEditText;
    private float initialX, initialY;
    private float previousX;
    private ImageButton proverkaBtm, back, perevoBtm;
    private FirebaseFirestore db;
    private String levelName, nickname;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.level_activity);
        setupLocale();
        initializeViews();
        setupListeners();
        loadLevelData();
    }

    private void setupLocale() {
        Locale locale = new Locale("ru");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void initializeViews() {
        db = FirebaseFirestore.getInstance();
        draggableView = findViewById(R.id.draggable_view);
        contentTextView = findViewById(R.id.contentTextView);
        levelNameTextView = findViewById(R.id.levelNameTextView);
        translationTextView = findViewById(R.id.translationTextView);
        leftButton = findViewById(R.id.left_button);
        rightButton = findViewById(R.id.right_button);
        linearBtm = findViewById(R.id.linearBtm);
        inputTranslationEditText = findViewById(R.id.inputTranslationEditText);
        checkTranslationButton = findViewById(R.id.checkTranslationButton);
        completeButton = findViewById(R.id.complete_button);
        proverkaBtm = findViewById(R.id.proverkaBtm);
        perevoBtm = findViewById(R.id.perevoBtm);
        back = findViewById(R.id.backButton);
        gestureDetector = new GestureDetector(this, new GestureListener());
    }

    private void setupListeners() {
        back.setOnClickListener(v -> finish());

        proverkaBtm.setOnClickListener(v -> showTranslationInput());
        perevoBtm.setOnClickListener(v -> showTranslation());
        checkTranslationButton.setOnClickListener(v -> checkTranslation());
        completeButton.setOnClickListener(v -> completeLevelAndFinish());

        draggableView.setOnTouchListener((v, event) -> handleDragEvent(event));
    }

    private void loadLevelData() {
        Intent intent = getIntent();
        nickname = intent.getStringExtra("nickname");
        Level level = (Level) intent.getSerializableExtra("level");

        if (level != null) {
            levelName = level.getLevelName();
            levelNameTextView.setText(levelName);
            unlearnedWords.addAll(level.getWords());
            updateContent();
        }
    }

    private void showTranslationInput() {
        linearBtm.setVisibility(View.GONE);
        inputTranslationEditText.setVisibility(View.VISIBLE);
        checkTranslationButton.setVisibility(View.VISIBLE);
    }

    private void showTranslation() {
        linearBtm.setVisibility(View.GONE);
        if (!unlearnedWords.isEmpty()) {
            translationTextView.setText(unlearnedWords.get(currentIndex).getTranslation());
            translationTextView.setVisibility(View.VISIBLE);
        }
    }

    private void checkTranslation() {
        String userInput = inputTranslationEditText.getText().toString().trim();
        Word currentWord = unlearnedWords.get(currentIndex);

        if (userInput.equalsIgnoreCase(currentWord.getTranslation())) {
            animateBackgroundColor(inputTranslationEditText, Color.GREEN);
            showToast("Правильно!");
        } else {
            animateBackgroundColor(inputTranslationEditText, Color.RED);
            showToast("Неправильно! Правильный перевод: " + currentWord.getTranslation());
        }
    }

    private void completeLevelAndFinish() {
        addLearnedWordsToFirestore(nickname);
        completeLevel();
    }

    private void updateContent() {
        if (unlearnedWords.isEmpty()) {
            new android.os.Handler().postDelayed(() -> {
                draggableView.setEnabled(false);
                draggableView.setAlpha(0.5f);
                contentTextView.setText("Вы изучили все слова!");
                showToast("Обучение завершено");
                linearBtm.setVisibility(View.GONE);
                translationTextView.setVisibility(View.GONE);
                completeButton.setVisibility(View.VISIBLE);
                leftButton.setVisibility(View.GONE);
                rightButton.setVisibility(View.GONE);
                inputTranslationEditText.setVisibility(View.GONE);
                checkTranslationButton.setVisibility(View.GONE);
            }, 300);
            return;
        }

        Word currentWord = unlearnedWords.get(currentIndex);
        contentTextView.setText(currentWord.getEnglish());
        resetUIElements();
    }

    private void resetUIElements() {
        draggableView.setEnabled(true);
        draggableView.setAlpha(1.0f);
        linearBtm.setVisibility(View.VISIBLE);
        checkTranslationButton.setVisibility(View.GONE);
        translationTextView.setVisibility(View.GONE);
        inputTranslationEditText.setVisibility(View.GONE);
        inputTranslationEditText.setBackgroundColor(Color.TRANSPARENT);
        inputTranslationEditText.setText("");
    }

    private boolean handleDragEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return handleActionDown(event);
            case MotionEvent.ACTION_MOVE:
                return handleActionMove(event);
            case MotionEvent.ACTION_UP:
                return handleActionUp();
        }
        return false;
    }

    private boolean handleActionDown(MotionEvent event) {
        if (draggableView.isEnabled()) {
            initialX = draggableView.getX();
            initialY = draggableView.getY();
            previousX = event.getRawX();
            return true;
        }
        return false;
    }

    private boolean handleActionMove(MotionEvent event) {
        if (draggableView.isEnabled()) {
            float newX = event.getRawX() - draggableView.getWidth() / 2;
            float newY = event.getRawY() - draggableView.getHeight() / 2;

            draggableView.setX(newX);
            draggableView.setY(newY);

            updateButtonColors(newX);
            return true;
        }
        return false;
    }

    private void updateButtonColors(float newX) {
        if (newX > previousX - 350) {
            rightButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green));
            leftButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray));
            contentTextView.setTextColor(Color.GREEN);
        } else if (newX < previousX + 350) {
            leftButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.red));
            rightButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray));
            contentTextView.setTextColor(Color.RED);
        }
    }

    private boolean handleActionUp() {
        if (draggableView.isEnabled()) {
            animateDraggableViewToInitialPosition();
            return true;
        }
        return false;
    }

    private void animateDraggableViewToInitialPosition() {
        draggableView.animate()
                .x(initialX)
                .y(initialY)
                .setDuration(200)
                .withEndAction(() -> {
                    leftButton.setBackgroundColor(ContextCompat.getColor(this, R.color.gray));
                    rightButton.setBackgroundColor(ContextCompat.getColor(this, R.color.gray));
                    contentTextView.setTextColor(Color.WHITE);
                })
                .start();
    }

    private void addLearnedWordsToFirestore(String nickname) {
        if (learnedWords.isEmpty()) {
            showToast("Нет изученных слов для добавления");
            return;
        }

        DocumentReference userRef = db.collection("usersLearnedWords").document(nickname);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                updateWordsInFirestore(userRef, document);
            } else {
                showToast("Ошибка при получении данных: " + task.getException().getMessage());
            }
        });
    }

    private void updateWordsInFirestore(DocumentReference userRef, DocumentSnapshot document) {
        Map<String, Object> existingWords = document.exists() ?
                (Map<String, Object>) document.get("words") : new HashMap<>();

        for (WordLevel word : learnedWords) {
            updateWordData(existingWords, word);
        }

        if (document.exists()) {
            userRef.update("words", existingWords)
                    .addOnSuccessListener(aVoid -> showToast("Изученные слова обновлены"))
                    .addOnFailureListener(e -> showToast("Ошибка при обновлении слов: " + e.getMessage()));
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("words", existingWords);
            userRef.set(data)
                    .addOnSuccessListener(aVoid -> showToast("Изученные слова добавлены"))
                    .addOnFailureListener(e -> showToast("Ошибка при создании словаря: " + e.getMessage()));
        }
    }

    private void updateWordData(Map<String, Object> existingWords, WordLevel word) {
        Map<String, Object> wordData = new HashMap<>();
        wordData.put("translation", word.getTranslation());
        wordData.put("hard", word.isHard());

        if (!existingWords.containsKey(word.getEnglish())) {
            existingWords.put(word.getEnglish(), wordData);
        } else {
            Map<String, Object> existingWordData = (Map<String, Object>) existingWords.get(word.getEnglish());
            if (existingWordData != null) {
                existingWordData.put("hard", word.isHard());
            }
        }
    }

    private void completeLevel() {
        Level currentLevel = (Level) getIntent().getSerializableExtra("level");
        if (currentLevel == null) {
            showToast("Ошибка: данные уровня не найдены");
            return;
        }

        String currentLevelName = currentLevel.getLevelName();
        int currentLevelNumber = extractLevelNumber(currentLevelName);
        if (currentLevelNumber == -1) {
            showToast("Ошибка: не удалось определить номер уровня");
            return;
        }

        String nextLevelName = "Уровень " + (currentLevelNumber + 1);

        db.collection("levels")
                .document(nickname)
                .get()
                .addOnSuccessListener(documentSnapshot -> handleLevelCompletion(documentSnapshot, nextLevelName))
                .addOnFailureListener(e -> showToast("Ошибка при получении данных: " + e.getMessage()));
    }

    private void handleLevelCompletion(DocumentSnapshot documentSnapshot, String nextLevelName) {
        if (!documentSnapshot.exists()) {
            showToast("Ошибка: данные пользователя не найдены");
            return;
        }

        List<Map<String, Object>> levels = (List<Map<String, Object>>) documentSnapshot.get("levels");
        boolean nextLevelFound = false;

        for (Map<String, Object> level : levels) {
            String levelName = (String) level.get("levelName");
            if (levelName != null && levelName.startsWith(nextLevelName)) {
                Map<String, Object> details = (Map<String, Object>) level.get("details");
                if (details != null) {
                    details.put("isUnlocked", true);
                    nextLevelFound = true;
                    break;
                }
            }
        }

        if (nextLevelFound) {
            unlockNextLevel(levels);
        } else {
            showToast("Это последний уровень. Поздравляем!");
            setResultAndFinish();
        }
    }

    private void unlockNextLevel(List<Map<String, Object>> levels) {
        db.collection("levels")
                .document(nickname)
                .update("levels", levels)
                .addOnSuccessListener(aVoid -> {
                    showToast("Следующий уровень разблокирован!");
                    setResultAndFinish();
                })
                .addOnFailureListener(e -> showToast("Ошибка при разблокировке: " + e.getMessage()));
    }

    private void setResultAndFinish() {
        setResult(RESULT_OK);
        finish();
    }

    private int extractLevelNumber(String levelName) {
        try {
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(levelName);
            return matcher.find() ? Integer.parseInt(matcher.group()) : -1;
        } catch (Exception e) {
            Log.e(TAG, "Error extracting level number", e);
            return -1;
        }
    }

    private void animateBackgroundColor(View view, int targetColor) {
        ValueAnimator colorAnimator = ValueAnimator.ofArgb(Color.TRANSPARENT, targetColor);
        colorAnimator.setDuration(500);
        colorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        colorAnimator.addUpdateListener(animator -> view.setBackgroundColor((int) animator.getAnimatedValue()));
        colorAnimator.start();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (!draggableView.isEnabled()) return false;

            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                handleSwipe(diffX > 0);
                return true;
            }
            return false;
        }

        private void handleSwipe(boolean isRightSwipe) {
            if (unlearnedWords.isEmpty()) return;

            Word currentWord = unlearnedWords.get(currentIndex);
            WordLevel wordToAdd = new WordLevel(currentWord.getEnglish(), currentWord.getTranslation(), false);
            wordToAdd.setHard(!isRightSwipe);

            if (!containsWord(learnedWords, wordToAdd)) {
                learnedWords.add(wordToAdd);
                unlearnedWords.remove(currentIndex);
                saveDailyWordCount(nickname, 1);

                if (!unlearnedWords.isEmpty()) {
                    currentIndex %= unlearnedWords.size();
                }

                updateContent();
                showToast(isRightSwipe ? "Слово добавлено в изученные" : "Слово помечено как сложное");
            } else {
                showToast("Слово уже изучено");
            }
        }

        private boolean containsWord(List<WordLevel> list, WordLevel word) {
            for (WordLevel w : list) {
                if (w.getEnglish().equals(word.getEnglish())) {
                    return true;
                }
            }
            return false;
        }
    }

    private void saveDailyWordCount(String nickname, int wordsLearnedToday) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DocumentReference dailyRef = db.collection("dailyWordCount")
                .document(nickname)
                .collection("wordCount")
                .document(today);

        dailyRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    updateExistingCount(dailyRef, document, wordsLearnedToday);
                } else {
                    createNewCount(dailyRef, wordsLearnedToday);
                }
            } else {
                showToast("Ошибка при получении данных: " + task.getException().getMessage());
            }
        });
    }

    private void updateExistingCount(DocumentReference ref, DocumentSnapshot document, int wordsLearnedToday) {
        Long currentCount = document.getLong("count");
        if (currentCount != null) {
            ref.update("count", currentCount + wordsLearnedToday)
                    .addOnFailureListener(e -> showToast("Ошибка при обновлении: " + e.getMessage()));
        }
    }

    private void createNewCount(DocumentReference ref, int wordsLearnedToday) {
        Map<String, Object> data = new HashMap<>();
        data.put("count", wordsLearnedToday);
        ref.set(data)
                .addOnFailureListener(e -> showToast("Ошибка при сохранении: " + e.getMessage()));
    }
}