package com.example.kursa;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * DialogueActivity — активность для интерактивного диалога с пользователем.
 * Отображает фразы персонажа и варианты ответа, загружает данные диалога из Firestore,
 * позволяет пользователю выбирать ответы и отслеживает прогресс. Поддерживает отображение
 * правильных ответов и завершение диалога.
 */
public class DialogueActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private Button option1Button, option2Button, option3Button;
    private ChatAdapter chatAdapter;
    private FirebaseFirestore db;
    private String userId;
    private String dialogueId;
    private int currentStep = 0;
    private List<Map<String, Object>> dialogueOptions;
    private String character;
    private ImageButton backBtm;

    /**
     * Инициализирует активность, устанавливает layout, настраивает RecyclerView для чата
     * и загружает данные диалога. Проверяет наличие userId и dialogueId.
     *
     * @param savedInstanceState Сохраненное состояние активности
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_activity);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        option1Button = findViewById(R.id.option1Button);
        option2Button = findViewById(R.id.option2Button);
        option3Button = findViewById(R.id.option3Button);
        backBtm = findViewById(R.id.backButton);
        db = FirebaseFirestore.getInstance();

        chatAdapter = new ChatAdapter();
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        userId = getIntent().getStringExtra("nickname");
        dialogueId = getIntent().getStringExtra("dialogueId");
        if (userId == null || dialogueId == null) {
            finish();
            return;
        }

        backBtm.setOnClickListener(v -> {
            finish();
        });

        loadDialogue();
        //loadPlayerProgress();
    }

    /**
     * Загружает данные диалога из Firestore по dialogueId.
     * Извлекает персонажа и список опций, затем отображает текущий шаг диалога.
     */
    private void loadDialogue() {
        db.collection("dialogues")
                .document(dialogueId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        character = documentSnapshot.getString("character");
                        dialogueOptions = (List<Map<String, Object>>) documentSnapshot.get("options");

                        if (dialogueOptions != null && currentStep < dialogueOptions.size()) {
                            displayStep(currentStep);
                        } else {
                            finish();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки диалога: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    /**
     * Отображает текущий шаг диалога: фразу персонажа и варианты ответа.
     * Перемешивает ответы и добавляет фразу в чат с задержкой.
     *
     * @param step Номер текущего шага
     */
    private void displayStep(int step) {
        Map<String, Object> currentPhrase = dialogueOptions.get(step);
        String phraseText = (String) currentPhrase.get("text");
        List<Map<String, Object>> answers = new ArrayList<>((List<Map<String, Object>>) currentPhrase.get("answers"));

        Collections.shuffle(answers);

        new Handler().postDelayed(() -> {
            chatAdapter.addMessage(new ChatMessage(character, phraseText));
            chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            showOptions(answers);
        }, 500);
    }

    /**
     * Показывает кнопки с вариантами ответа, устанавливает их текст и обработчики событий.
     * Скрывает неиспользуемые кнопки.
     *
     * @param answers Список вариантов ответа
     */
    private void showOptions(List<Map<String, Object>> answers) {
        option1Button.setVisibility(View.GONE);
        option2Button.setVisibility(View.GONE);
        option3Button.setVisibility(View.GONE);

        Button[] buttons = {option1Button, option2Button, option3Button};
        for (int i = 0; i < answers.size() && i < buttons.length; i++) {
            Map<String, Object> answer = answers.get(i);
            String answerText = (String) answer.get("text");
            boolean isCorrect = (boolean) answer.get("isCorrect");
            int choiceIndex = i;

            Button button = buttons[i];
            button.setText(answerText);
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#363636")));
            button.setVisibility(View.VISIBLE);
            button.setEnabled(true);
            button.setOnClickListener(v -> handleChoice(choiceIndex, answerText, isCorrect, answers));
        }
    }

    /**
     * Обрабатывает выбор ответа пользователем. Добавляет ответ в чат, отключает кнопки,
     * показывает правильный ответ (если выбран неверный) и переходит к следующему шагу.
     *
     * @param choiceIndex Индекс выбранного ответа
     * @param answerText  Текст выбранного ответа
     * @param isCorrect   Флаг правильности ответа
     * @param answers     Список всех ответов
     */
    private void handleChoice(int choiceIndex, String answerText, boolean isCorrect, List<Map<String, Object>> answers) {
        Button[] buttons = {option1Button, option2Button, option3Button};
        for (Button button : buttons) {
            button.setEnabled(false);
        }

        chatAdapter.addMessage(new ChatMessage("You", answerText));
        chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);

        if (!isCorrect) {
            for (Map<String, Object> answer : answers) {
                if ((boolean) answer.get("isCorrect")) {
                    new Handler().postDelayed(() -> {
                        chatAdapter.addMessage(new ChatMessage(character, "Correct answer: " + answer.get("text")));
                        chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                    }, 1000);
                    break;
                }
            }
        }

        //savePlayerProgress(choiceIndex);
        new Handler().postDelayed(() -> {
            currentStep++;
            if (currentStep < dialogueOptions.size()) {
                displayStep(currentStep);
            } else {
                Toast.makeText(this, "Диалог завершен!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }, 2000);
    }

}