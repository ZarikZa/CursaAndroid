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
/**
 * LevelActivActivity — активность для изучения слов на заданном уровне.
 * Позволяет пользователю взаимодействовать со словами через свайпы (вправо — изучено, влево — сложное),
 * проверять перевод, просматривать перевод слова и завершать уровень. Сохраняет прогресс в Firestore.
 */
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

    /**
     * Инициализирует активность, устанавливает layout, локализацию,
     * элементы интерфейса, обработчики событий и загружает данные уровня.
     *
     * @param savedInstanceState Сохраненное состояние активности
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.level_activity);
        setupLocale();
        initializeViews();
        setupListeners();
        loadLevelData();
    }

    /**
     * Устанавливает локализацию на русский язык.
     */
    private void setupLocale() {
        Locale locale = new Locale("ru");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    /**
     * Инициализирует элементы интерфейса.
     */
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

    /**
     * Настраивает обработчики событий для элементов интерфейса.
     */
    private void setupListeners() {
        back.setOnClickListener(v -> finish());

        proverkaBtm.setOnClickListener(v -> showTranslationInput());
        perevoBtm.setOnClickListener(v -> showTranslation());
        checkTranslationButton.setOnClickListener(v -> checkTranslation());
        completeButton.setOnClickListener(v -> completeLevelAndFinish());

        draggableView.setOnTouchListener((v, event) -> handleDragEvent(event));
    }

    /**
     * Загружает данные уровня из Intent.
     */
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

    /**
     * Показывает поле для ввода перевода.
     */
    private void showTranslationInput() {
        linearBtm.setVisibility(View.GONE);
        inputTranslationEditText.setVisibility(View.VISIBLE);
        checkTranslationButton.setVisibility(View.VISIBLE);
    }

    /**
     * Показывает перевод текущего слова.
     */
    private void showTranslation() {
        linearBtm.setVisibility(View.GONE);
        if (!unlearnedWords.isEmpty()) {
            translationTextView.setText(unlearnedWords.get(currentIndex).getTranslation());
            translationTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Проверяет введенный пользователем перевод.
     */
    private void checkTranslation() {
        String userInput = inputTranslationEditText.getText().toString().trim();
        Word currentWord = unlearnedWords.get(currentIndex);

        if (userInput.equalsIgnoreCase(currentWord.getTranslation())) {
            animateBackgroundColor(inputTranslationEditText, Color.GREEN);
        } else {
            animateBackgroundColor(inputTranslationEditText, Color.RED);
            showToast("Неправильно! Правильный перевод: " + currentWord.getTranslation());
        }
    }

    /**
     * Завершает уровень и сохраняет прогресс.
     */
    private void completeLevelAndFinish() {
        addLearnedWordsToFirestore(nickname);
        completeLevel();
    }

    /**
     * Обновляет содержимое интерфейса в зависимости от текущего слова.
     */
    private void updateContent() {
        if (unlearnedWords.isEmpty()) {
            new android.os.Handler().postDelayed(() -> {
                draggableView.setEnabled(false);
                draggableView.setAlpha(0.5f);
                contentTextView.setText("Вы изучили все слова!");
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

    /**
     * Сбрасывает состояние элементов интерфейса.
     */
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

    /**
     * Обрабатывает события касания для перетаскивания.
     *
     * @param event Событие касания
     * @return true, если событие обработано
     */
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

    /**
     * Обрабатывает начало перетаскивания.
     *
     * @param event Событие касания
     * @return true, если перетаскивание начато
     */
    private boolean handleActionDown(MotionEvent event) {
        if (draggableView.isEnabled()) {
            initialX = draggableView.getX();
            initialY = draggableView.getY();
            previousX = event.getRawX();
            return true;
        }
        return false;
    }

    /**
     * Обрабатывает перемещение при перетаскивании.
     *
     * @param event Событие касания
     * @return true, если перемещение обработано
     */
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

    /**
     * Обновляет цвета кнопок в зависимости от положения перетаскиваемого элемента.
     *
     * @param newX Новая X-координата
     */
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

    /**
     * Обрабатывает завершение перетаскивания.
     *
     * @return true, если завершение обработано
     */
    private boolean handleActionUp() {
        if (draggableView.isEnabled()) {
            animateDraggableViewToInitialPosition();
            return true;
        }
        return false;
    }

    /**
     * Анимирует возврат перетаскиваемого элемента в исходное положение.
     */
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

    /**
     * Сохраняет изученные слова в Firestore.
     *
     * @param nickname Никнейм пользователя
     */
    private void addLearnedWordsToFirestore(String nickname) {
        if (learnedWords.isEmpty()) {
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

    /**
     * Обновляет слова в Firestore.
     *
     * @param userRef  Ссылка на документ пользователя
     * @param document Документ с существующими данными
     */
    private void updateWordsInFirestore(DocumentReference userRef, DocumentSnapshot document) {
        Map<String, Object> existingWords = document.exists() ?
                (Map<String, Object>) document.get("words") : new HashMap<>();

        for (WordLevel word : learnedWords) {
            updateWordData(existingWords, word);
        }

        if (document.exists()) {
            userRef.update("words", existingWords)
                    .addOnFailureListener(e -> showToast("Ошибка при обновлении слов: " + e.getMessage()));
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("words", existingWords);
            userRef.set(data)
                    .addOnFailureListener(e -> showToast("Ошибка при создании словаря: " + e.getMessage()));
        }
    }

    /**
     * Обновляет данные слова в словаре.
     *
     * @param existingWords Существующие слова
     * @param word          Слово для обновления
     */
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

    /**
     * Завершает уровень и разблокирует следующий.
     */
    private void completeLevel() {
        Level currentLevel = (Level) getIntent().getSerializableExtra("level");
        if (currentLevel == null) {
            return;
        }

        String currentLevelName = currentLevel.getLevelName();
        int currentLevelNumber = extractLevelNumber(currentLevelName);
        if (currentLevelNumber == -1) {
            return;
        }

        String nextLevelName = "Уровень " + (currentLevelNumber + 1);

        db.collection("levels")
                .document(nickname)
                .get()
                .addOnSuccessListener(documentSnapshot -> handleLevelCompletion(documentSnapshot, nextLevelName));
    }

    /**
     * Обрабатывает завершение уровня и разблокировку следующего.
     *
     * @param documentSnapshot Документ с данными уровней
     * @param nextLevelName   Название следующего уровня
     */
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
            setResultAndFinish();
        }
    }

    /**
     * Разблокирует следующий уровень в Firestore.
     *
     * @param levels Список уровней
     */
    private void unlockNextLevel(List<Map<String, Object>> levels) {
        db.collection("levels")
                .document(nickname)
                .update("levels", levels)
                .addOnSuccessListener(aVoid -> {
                    setResultAndFinish();
                })
                .addOnFailureListener(e -> showToast("Ошибка при разблокировке: " + e.getMessage()));
    }

    /**
     * Устанавливает результат и завершает активность.
     */
    private void setResultAndFinish() {
        setResult(RESULT_OK);
        finish();
    }

    /**
     * Извлекает номер уровня из названия.
     *
     * @param levelName Название уровня
     * @return Номер уровня или -1 при ошибке
     */
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

    /**
     * Анимирует изменение цвета фона элемента.
     *
     * @param view       Элемент для анимации
     * @param targetColor Целевой цвет
     */
    private void animateBackgroundColor(View view, int targetColor) {
        ValueAnimator colorAnimator = ValueAnimator.ofArgb(Color.TRANSPARENT, targetColor);
        colorAnimator.setDuration(500);
        colorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        colorAnimator.addUpdateListener(animator -> view.setBackgroundColor((int) animator.getAnimatedValue()));
        colorAnimator.start();
    }

    /**
     * Показывает уведомление.
     *
     * @param message Сообщение для отображения
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Класс для обработки жестов свайпа.
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        /**
         * Обрабатывает жест свайпа.
         *
         * @param e1        Первое событие касания
         * @param e2        Второе событие касания
         * @param velocityX Скорость по оси X
         * @param velocityY Скорость по оси Y
         * @return true, если свайп обработан
         */
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

        /**
         * Обрабатывает свайп и обновляет списки слов.
         *
         * @param isRightSwipe Направление свайпа (true — вправо)
         */
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
            }
        }

        /**
         * Проверяет наличие слова в списке.
         *
         * @param list Список слов
         * @param word Слово для проверки
         * @return true, если слово уже есть
         */
        private boolean containsWord(List<WordLevel> list, WordLevel word) {
            for (WordLevel w : list) {
                if (w.getEnglish().equals(word.getEnglish())) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Сохраняет количество изученных слов за день в Firestore.
     *
     * @param nickname         Никнейм пользователя
     * @param wordsLearnedToday Количество изученных слов
     */
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

    /**
     * Обновляет существующий счетчик слов.
     *
     * @param ref              Ссылка на документ
     * @param document         Документ с данными
     * @param wordsLearnedToday Количество новых слов
     */
    private void updateExistingCount(DocumentReference ref, DocumentSnapshot document, int wordsLearnedToday) {
        Long currentCount = document.getLong("count");
        if (currentCount != null) {
            ref.update("count", currentCount + wordsLearnedToday)
                    .addOnFailureListener(e -> showToast("Ошибка при обновлении: " + e.getMessage()));
        }
    }

    /**
     * Создает новый счетчик слов.
     *
     * @param ref              Ссылка на документ
     * @param wordsLearnedToday Количество слов
     */
    private void createNewCount(DocumentReference ref, int wordsLearnedToday) {
        Map<String, Object> data = new HashMap<>();
        data.put("count", wordsLearnedToday);
        ref.set(data)
                .addOnFailureListener(e -> showToast("Ошибка при сохранении: " + e.getMessage()));
    }
}