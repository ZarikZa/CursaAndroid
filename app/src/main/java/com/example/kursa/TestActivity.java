package com.example.kursa;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
/**
 * TestActivity — активность для тестирования знаний слов уровня.
 * Показывает английские слова, принимает перевод от пользователя, проверяет правильность
 * и обновляет рейтинг на основе результатов. Использует анимацию для визуальной обратной связи.
 */
public class TestActivity extends AppCompatActivity {

    private TextView contentTextView;
    private TextView levelNameTextView;
    private List<Word> unlearnedWords;
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

    /**
     * Инициализирует активность, настраивает интерфейс, локализацию и обработчики событий.
     *
     * @param savedInstanceState Сохраненное состояние активности
     */
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

    /**
     * Обновляет содержимое экрана: показывает следующее слово или результаты.
     */
    private void updateContent() {
        if (unlearnedWords.isEmpty()) {
            contentTextView.setText("Вы изучили все слова!\n");
            String progressText = String.format(Locale.getDefault(), "Выучено %d из %d слов", correctAnswers, totalWords);
            contentTextView.append("\n\n" + progressText);
            completeButton.setVisibility(View.VISIBLE);
            inputTranslationEditText.setVisibility(View.GONE);
            checkTranslationButton.setVisibility(View.GONE);
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

    /**
     * Проверяет введенный перевод, обновляет счетчик правильных ответов
     * и переходит к следующему слову.
     */
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
        } else {
            animateBackgroundColor(inputTranslationEditText, Color.RED);
        }

        new android.os.Handler().postDelayed(() -> {
            currentIndex++;
            if (currentIndex < unlearnedWords.size()) {
                updateContent();
            } else {
                unlearnedWords.clear();
                updateContent();
            }
        }, 1000);
    }

    /**
     * Анимирует изменение цвета фона элемента ввода для обратной связи.
     *
     * @param view        Элемент для анимации
     * @param targetColor Целевой цвет (зеленый или красный)
     */
    private void animateBackgroundColor(View view, int targetColor) {
        int startColor = Color.parseColor("#888888");
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(16f);

        ValueAnimator colorAnimator = ValueAnimator.ofArgb(startColor, targetColor);
        colorAnimator.setDuration(700);
        colorAnimator.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        colorAnimator.addUpdateListener(animator -> {
            int animatedColor = (int) animator.getAnimatedValue();
            drawable.setColor(animatedColor);
            view.setBackground(drawable);
        });

        ValueAnimator reverseColorAnimator = ValueAnimator.ofArgb(targetColor, startColor);
        reverseColorAnimator.setDuration(700);
        reverseColorAnimator.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        reverseColorAnimator.addUpdateListener(animator -> {
            int animatedColor = (int) animator.getAnimatedValue();
            drawable.setColor(animatedColor);
            view.setBackground(drawable);
        });

        colorAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                reverseColorAnimator.start();
            }
        });

        colorAnimator.start();
    }

    /**
     * Показывает результат тестирования и обновляет рейтинговые баллы.
     */
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

    /**
     * Обновляет рейтинговые баллы пользователя в Firestore.
     *
     * @param change Значение изменения рейтинга (+1 или -1)
     */
    private void updateRatingPoints(int change) {
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
                                .addOnSuccessListener(aVoid -> {})
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

    /**
     * Применяет стиль к полю ввода (скругленные углы, серый фон, белый текст).
     *
     * @param editText Поле ввода
     */
    private void setEditTextStyle(EditText editText) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(Color.parseColor("#888888"));
        drawable.setCornerRadius(16f);

        editText.setBackground(drawable);
        editText.setTextColor(Color.WHITE);
        editText.setHintTextColor(Color.parseColor("#B0B0B0"));
    }
}