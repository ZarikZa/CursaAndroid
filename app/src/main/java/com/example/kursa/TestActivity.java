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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestActivity extends AppCompatActivity {

    private FrameLayout draggableView;
    private TextView contentTextView;
    private Button leftButton;
    private Button rightButton;
    private Button completeButton;
    private GestureDetector gestureDetector;
    private LinearLayout linearBtm;
    private List<Word> unlearnedWords;
    private List<Word> learnedWords;
    private TextView translationTextView;
    private ImageButton perevoBtm;
    private int currentIndex = 0;
    private TextView levelNameTextView;
    private float initialX, initialY;
    private float previousX;
    private EditText inputTranslationEditText;
    private Button checkTranslationButton;
    private ImageButton proverkaBtm;
    private FirebaseFirestore db;
    private String levelName;
    private String nickname;
    private ImageButton back;
    private int correctAnswers = 0;
    private int totalWords = 0;

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

        back.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        nickname = intent.getStringExtra("nickname");
        Level level = (Level) intent.getSerializableExtra("level");
        if (level != null) {
            levelName = level.getLevelName();
            levelNameTextView.setText(levelName);
            unlearnedWords.addAll(level.getWords());
            totalWords = unlearnedWords.size();
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

        completeButton.setOnClickListener(v -> finish());

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

                    if (newX > previousX - 350) {
                        rightButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green));
                        leftButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray));
                    } else if (newX < previousX + 350) {
                        leftButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.red));
                        rightButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray));
                    }

                    return true;

                case MotionEvent.ACTION_UP:
                    draggableView.animate()
                            .x(initialX)
                            .y(initialY)
                            .setDuration(200)
                            .withEndAction(() -> {
                                leftButton.setBackgroundColor(ContextCompat.getColor(this, R.color.gray));
                                rightButton.setBackgroundColor(ContextCompat.getColor(this, R.color.gray));
                            })
                            .start();
                    return true;
            }
            return false;
        });
    }

    private void updateContent() {
        if (unlearnedWords.isEmpty()) {
            contentTextView.setText("Вы изучили все слова!\n");
            String progressText = String.format(Locale.getDefault(), "Выучено %d из %d слов", correctAnswers, totalWords);
            contentTextView.append("\n\n" + progressText);
            Toast.makeText(this, "Обучение завершено", Toast.LENGTH_SHORT).show();
            linearBtm.setVisibility(View.GONE);
            translationTextView.setVisibility(View.GONE);
            completeButton.setVisibility(View.VISIBLE);
            leftButton.setVisibility(View.GONE);
            rightButton.setVisibility(View.GONE);
            inputTranslationEditText.setVisibility(View.GONE);
            checkTranslationButton.setVisibility(View.GONE);
            showResult();
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

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();

            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    correctAnswers++;
                    Word currentWord = unlearnedWords.get(currentIndex);
                    if (!learnedWords.contains(currentWord)) {
                        learnedWords.add(currentWord);
                    }
                    updateContent();
                } else {
                    correctAnswers--;
                    updateContent();
                }
                unlearnedWords.remove(currentIndex);
                if (!unlearnedWords.isEmpty()) {
                    currentIndex = (currentIndex % unlearnedWords.size());
                }

                updateContent();

                if (unlearnedWords.isEmpty()) {
                    showResult();
                }

                return true;
            }
            return false;
        }
    }

    private void showResult() {
        double percentage = (double) correctAnswers / totalWords * 100;
        String message;

        if (percentage > 80) {
            message = "Отлично! Вы заработали 1 балл.";
            updateRatingPoints(1);
        } else {
            message = "Попробуйте ещё раз. Вы потеряли 1 балл.";
            updateRatingPoints(-1);
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void updateRatingPoints(int change) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (nickname == null) {
            Toast.makeText(this, "Ошибка: никнейм пользователя не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .whereEqualTo("nickname", nickname)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String userId = document.getId();

                        db.collection("users").document(userId)
                                .update("reytingPoints", FieldValue.increment(change))
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Рейтинг обновлён", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Ошибка обновления рейтинга: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Пользователь с никнеймом " + nickname + " не найден", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка поиска пользователя: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
