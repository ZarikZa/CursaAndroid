package com.example.kursa;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.AnimatorListenerAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TestActivity extends AppCompatActivity {

    private TextView contentTextView;
    private TextView levelNameTextView;
    private List<Word> unlearnedWords;
    private List<Word> learnedWords;
    private int currentIndex = 0;
    private EditText inputTranslationEditText;
    private Button checkTranslationButton;
    private Button completeButton;
    private FirebaseFirestore db;
    private String levelName;
    private String nickname;
    private ImageButton back;
    private int correctAnswers = 0;
    private int totalWords = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        db = FirebaseFirestore.getInstance();

        Locale locale = new Locale("ru");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        contentTextView = findViewById(R.id.contentTextView);
        levelNameTextView = findViewById(R.id.levelNameTextView);
        inputTranslationEditText = findViewById(R.id.inputTranslationEditText);
        checkTranslationButton = findViewById(R.id.checkTranslationButton);
        completeButton = findViewById(R.id.complete_button);
        back = findViewById(R.id.backButton);

        back.setOnClickListener(v -> finish());
        setEditTextStyle(inputTranslationEditText);

        Intent intent = getIntent();
        nickname = intent.getStringExtra("nickname");
        Level level = (Level) intent.getSerializableExtra("level");
        if (level != null) {
            levelName = level.getLevelName();
            levelNameTextView.setText(levelName);
            unlearnedWords = new ArrayList<>(level.getWords());
            totalWords = unlearnedWords.size();
            updateContent();
        }

        checkTranslationButton.setOnClickListener(v -> checkTranslation());

        completeButton.setOnClickListener(v -> finish());
    }

    private void updateContent() {
        if (unlearnedWords.isEmpty()) {
            contentTextView.setText("Вы изучили все слова!\n");
            String progressText = String.format(Locale.getDefault(), "Выучено %d из %d слов", correctAnswers, totalWords);
            contentTextView.append("\n\n" + progressText);
            Toast.makeText(this, "Обучение завершено", Toast.LENGTH_SHORT).show();
            completeButton.setVisibility(View.VISIBLE);
            setEditTextStyle(inputTranslationEditText);
            showResult();
            return;
        }

        Word currentWord = unlearnedWords.get(currentIndex);
        contentTextView.setText(currentWord.getEnglish());

        inputTranslationEditText.setVisibility(View.VISIBLE);
        checkTranslationButton.setVisibility(View.VISIBLE);
        setEditTextStyle(inputTranslationEditText);
        inputTranslationEditText.setText("");
    }

    private void checkTranslation() {
        String userInput = inputTranslationEditText.getText().toString().trim();

        if (userInput.isEmpty()) {
            Toast.makeText(this, "Введите перевод!", Toast.LENGTH_SHORT).show();
            return;
        }

        Word currentWord = unlearnedWords.get(currentIndex);
        String correctTranslation = currentWord.getTranslation();

        if (userInput.equalsIgnoreCase(correctTranslation)) {
            animateBackgroundColor(inputTranslationEditText, Color.GREEN);
            correctAnswers++;
            Toast.makeText(this, "Правильно!", Toast.LENGTH_SHORT).show();
        } else {
            animateBackgroundColor(inputTranslationEditText, Color.RED);
            Toast.makeText(this, "Неправильно! Правильный перевод: " + correctTranslation, Toast.LENGTH_SHORT).show();
        }

        new android.os.Handler().postDelayed(() -> {
            // Переход к следующему слову
            currentIndex++;
            if (currentIndex < unlearnedWords.size()) {
                updateContent();
            } else {
                unlearnedWords.clear();
                updateContent();
            }
        }, 1000); // Задержка 1 секунда (1000 мс)
    }


    private void animateBackgroundColor(View view, int targetColor) {
        // Исходный цвет (серый или другой, который вы используете)
        int startColor = Color.parseColor("#888888"); // Серый цвет

        // Создаем GradientDrawable для скруглённых углов
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE); // Прямоугольник
        drawable.setCornerRadius(16f); // Скруглённые углы с радиусом 16dp

        // Анимация изменения цвета на целевой (красный или зелёный)
        ValueAnimator colorAnimator = ValueAnimator.ofArgb(startColor, targetColor);
        colorAnimator.setDuration(700); // 0.7 секунды до целевого цвета
        colorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        colorAnimator.addUpdateListener(animator -> {
            int animatedColor = (int) animator.getAnimatedValue();
            drawable.setColor(animatedColor); // Устанавливаем анимированный цвет
            view.setBackground(drawable); // Применяем новый фон
        });

        // Анимация возврата к исходному цвету
        ValueAnimator reverseColorAnimator = ValueAnimator.ofArgb(targetColor, startColor);
        reverseColorAnimator.setDuration(700); // 0.7 секунды до возврата
        reverseColorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        reverseColorAnimator.addUpdateListener(animator -> {
            int animatedColor = (int) animator.getAnimatedValue();
            drawable.setColor(animatedColor); // Устанавливаем анимированный цвет
            view.setBackground(drawable); // Применяем новый фон
        });

        // Запуск анимаций последовательно
        colorAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                reverseColorAnimator.start(); // Запускаем обратную анимацию после завершения первой
            }
        });

        colorAnimator.start(); // Запускаем первую анимацию
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
    private void setEditTextStyle(EditText editText) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE); // Прямоугольник
        drawable.setColor(Color.parseColor("#888888")); // Серый цвет
        drawable.setCornerRadius(16f); // Скругленные углы с радиусом 16dp

        editText.setBackground(drawable); // Применение фона к EditText
        editText.setTextColor(Color.WHITE); // Белый цвет текста
        editText.setHintTextColor(Color.parseColor("#B0B0B0")); // Цвет подсказки
    }
}