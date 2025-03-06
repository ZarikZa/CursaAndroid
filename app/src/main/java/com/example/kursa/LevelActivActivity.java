package com.example.kursa;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LevelActivActivity extends AppCompatActivity {
    private FrameLayout draggableView;
    private TextView contentTextView, levelNameTextView, translationTextView;
    private Button leftButton, rightButton, completeButton, checkTranslationButton;
    private GestureDetector gestureDetector;
    private LinearLayout linearBtm;
    private List<Word> unlearnedWords, learnedWords;
    private int currentIndex = 0;
    private float initialX, initialY;
    private float previousX;
    private EditText inputTranslationEditText;
    private ImageButton proverkaBtm, back, perevoBtm;
    private FirebaseFirestore db;
    private String levelName, nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.level_activity);

        db = FirebaseFirestore.getInstance();

        Locale locale = new Locale("ru");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        perevoBtm = findViewById(R.id.perevoBtm);
        proverkaBtm = findViewById(R.id.proverkaBtm);
        draggableView = findViewById(R.id.draggable_view);
        contentTextView = findViewById(R.id.contentTextView);
        leftButton = findViewById(R.id.left_button);
        rightButton = findViewById(R.id.right_button);
        linearBtm = findViewById(R.id.linearBtm);
        unlearnedWords = new ArrayList<>();
        learnedWords = new ArrayList<>();
        inputTranslationEditText = findViewById(R.id.inputTranslationEditText);
        checkTranslationButton = findViewById(R.id.checkTranslationButton);
        completeButton = findViewById(R.id.complete_button);
        translationTextView = findViewById(R.id.translationTextView);
        levelNameTextView = findViewById(R.id.levelNameTextView);
        back = findViewById(R.id.backButton);

        back.setOnClickListener(v ->{
            finish();
        });

        Intent intent = getIntent();
        nickname = intent.getStringExtra("nickname");
        Level level = (Level) intent.getSerializableExtra("level");
        if (level != null) {
            levelName = level.getLevelName();
            levelNameTextView.setText(levelName);
            unlearnedWords.addAll(level.getWords());
            updateContent();
        }

        proverkaBtm.setOnClickListener(v -> {
            linearBtm.setVisibility(View.GONE);
            inputTranslationEditText.setVisibility(View.VISIBLE);
            checkTranslationButton.setVisibility(View.VISIBLE);
        });

        checkTranslationButton.setOnClickListener(v -> {
            String userInput = inputTranslationEditText.getText().toString().trim();
            Word currentWord = unlearnedWords.get(currentIndex);
            String correctTranslation = currentWord.getTranslation();

            if (userInput.equalsIgnoreCase(correctTranslation)) {
                animateBackgroundColor(inputTranslationEditText, Color.GREEN);
                Toast.makeText(this, "Правильно!", Toast.LENGTH_SHORT).show();
            } else {
                animateBackgroundColor(inputTranslationEditText, Color.RED);
                Toast.makeText(this, "Неправильно! Правильный перевод: " + correctTranslation, Toast.LENGTH_SHORT).show();
            }
        });

        perevoBtm.setOnClickListener(v -> {
            linearBtm.setVisibility(View.GONE);
            if (!unlearnedWords.isEmpty()) {
                Word currentWord = unlearnedWords.get(currentIndex);
                translationTextView.setText(currentWord.getTranslation());
                translationTextView.setVisibility(View.VISIBLE);
            }
        });

        completeButton.setOnClickListener(v -> {
            addLearnedWordsToFirestore(nickname);
            completeLevel();
            finish();
        });

        gestureDetector = new GestureDetector(this, new GestureListener());

        draggableView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = draggableView.getX();
                    initialY = draggableView.getY();
                    previousX = event.getRawX();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float newX = event.getRawX() - draggableView.getWidth() / 2;
                    float newY = event.getRawY() - draggableView.getHeight() / 2;

                    draggableView.setX(newX);
                    draggableView.setY(newY);

                    if (newX > previousX) {
                        rightButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green));
                        leftButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray));
                    } else if (newX < previousX) {
                        leftButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.red));
                        rightButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray));
                    }

                    return true;

                case MotionEvent.ACTION_UP:
                    draggableView.animate()
                            .x(initialX)
                            .y(initialY)
                            .setDuration(200)
                            .start();
                    return true;
            }
            return false;
        });
    }

    private void addLearnedWordsToFirestore(String nickname) {
        if (learnedWords.isEmpty()) {
            Toast.makeText(this, "Нет изученных слов для добавления", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference userRef = db.collection("usersLearnedWords").document(nickname);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, String> existingWords = (Map<String, String>) document.get("words");
                    if (existingWords == null) {
                        existingWords = new HashMap<>();
                    }

                    for (Word word : learnedWords) {
                        String englishWord = word.getEnglish();
                        String translation = word.getTranslation();

                        if (!existingWords.containsKey(englishWord)) {
                            existingWords.put(englishWord, translation);
                        } else {
                            Toast.makeText(this, "Слово '" + englishWord + "' уже есть в вашем словаре", Toast.LENGTH_SHORT).show();
                        }
                    }

                    userRef.update("words", existingWords)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Изученные слова добавлены в ваш словарь", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Ошибка при добавлении слов: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                } else {
                    Map<String, String> newWords = new HashMap<>();
                    for (Word word : learnedWords) {
                        newWords.put(word.getEnglish(), word.getTranslation());
                    }

                    userRef.set(Collections.singletonMap("words", newWords))
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Изученные слова добавлены в ваш словарь", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Ошибка при создании словаря: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                Toast.makeText(this, "Ошибка при получении данных: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateContent() {
        if (unlearnedWords.isEmpty()) {
            contentTextView.setText("Вы изучили все слова!");
            Toast.makeText(this, "Обучение завершено", Toast.LENGTH_SHORT).show();
            linearBtm.setVisibility(View.GONE);
            translationTextView.setVisibility(View.GONE);
            completeButton.setVisibility(View.VISIBLE);
            leftButton.setVisibility(View.GONE);
            rightButton.setVisibility(View.GONE);
            inputTranslationEditText.setVisibility(View.GONE);
            checkTranslationButton.setVisibility(View.GONE);
            return;
        }

        linearBtm.setVisibility(View.VISIBLE);
        Word currentWord = unlearnedWords.get(currentIndex);
        contentTextView.setText(currentWord.getEnglish());

        checkTranslationButton.setVisibility(View.GONE);
        translationTextView.setVisibility(View.GONE);
        inputTranslationEditText.setVisibility(View.GONE);
        inputTranslationEditText.setBackgroundColor(Color.TRANSPARENT);
        inputTranslationEditText.setText("");
    }

    private void animateBackgroundColor(View view, int targetColor) {
        ValueAnimator colorAnimator = ValueAnimator.ofArgb(Color.TRANSPARENT, targetColor);
        colorAnimator.setDuration(500);
        colorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        colorAnimator.addUpdateListener(animator -> view.setBackgroundColor((int) animator.getAnimatedValue()));
        colorAnimator.start();
    }

    private void completeLevel() {
        Level currentLevel = (Level) getIntent().getSerializableExtra("level");
        if (currentLevel == null) {
            Toast.makeText(this, "Ошибка: данные уровня не найдены", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentLevelName = currentLevel.getLevelName();

        int currentLevelNumber = extractLevelNumber(currentLevelName);
        if (currentLevelNumber == -1) {
            Toast.makeText(this, "Ошибка: не удалось определить номер уровня", Toast.LENGTH_SHORT).show();
            return;
        }

        String nextLevelName = "Уровень " + (currentLevelNumber + 1);

        db.collection("levels")
                .document(nickname)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> levels = (List<Map<String, Object>>) documentSnapshot.get("levels");

                        boolean nextLevelFound = false;
                        for (Map<String, Object> level : levels) {
                            String levelName = (String) level.get("levelName");
                            if (levelName != null && levelName.startsWith(nextLevelName)) {
                                Map<String, Object> details = (Map<String, Object>) level.get("details");
                                if (details != null) {
                                    details.put("isUnlocked", true); // Разблокируем следующий уровень
                                    nextLevelFound = true;
                                    break;
                                }
                            }
                        }

                        if (nextLevelFound) {
                            db.collection("levels")
                                    .document(nickname)
                                    .update("levels", levels)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Следующий уровень разблокирован!", Toast.LENGTH_SHORT).show();

                                        // Возвращаем результат в LevelsFragment
                                        Intent resultIntent = new Intent();
                                        resultIntent.putExtra("LEVEL_UNLOCKED", true);
                                        setResult(RESULT_OK, resultIntent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Ошибка при разблокировке уровня: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(this, "Это последний уровень. Поздравляем с завершением!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Ошибка: данные пользователя не найдены", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка при получении данных пользователя: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private int extractLevelNumber(String levelName) {
        try {
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(levelName);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();

            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    Word currentWord = unlearnedWords.get(currentIndex);
                    if (!learnedWords.contains(currentWord)) {
                        learnedWords.add(currentWord);
                        unlearnedWords.remove(currentIndex);

                        if (!unlearnedWords.isEmpty()) {
                            currentIndex = (currentIndex % unlearnedWords.size());
                        }

                        updateContent();
                        Toast.makeText(LevelActivActivity.this, "Слово добавлено в изученные", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LevelActivActivity.this, "Слово уже изучено", Toast.LENGTH_SHORT).show();
                    }

                    rightButton.setBackgroundTintList(ContextCompat.getColorStateList(LevelActivActivity.this, R.color.green));
                    leftButton.setBackgroundTintList(ContextCompat.getColorStateList(LevelActivActivity.this, R.color.gray));

                } else {
                    if (!unlearnedWords.isEmpty()) {
                        Word currentWord = unlearnedWords.get(currentIndex);
                        unlearnedWords.remove(currentIndex);

                        if (!unlearnedWords.contains(currentWord)) {
                            unlearnedWords.add(currentWord);
                        } else {
                            Toast.makeText(LevelActivActivity.this, "Слово уже существует в списке", Toast.LENGTH_SHORT).show();
                        }

                        if (currentIndex >= unlearnedWords.size()) {
                            currentIndex = 0;
                        }

                        updateContent();
                        Toast.makeText(LevelActivActivity.this, "Слово перемещено в конец списка", Toast.LENGTH_SHORT).show();

                        leftButton.setBackgroundTintList(ContextCompat.getColorStateList(LevelActivActivity.this, R.color.red));
                        rightButton.setBackgroundTintList(ContextCompat.getColorStateList(LevelActivActivity.this, R.color.gray));
                    }
                }
                return true;
            }
            return false;
        }
    }
}