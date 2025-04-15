package com.example.kursa;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * DialogueEditActivity — активность для редактирования или удаления существующего диалога в Firestore.
 * Позволяет изменить ID диалога, имя персонажа, пять фраз и по три варианта ответа для каждой.
 * Загружает существующие данные диалога, выполняет валидацию и сохраняет изменения или удаляет диалог.
 */
public class DialogueEditActivity extends AppCompatActivity {

    private TextInputEditText dialogueIdEditText, characterEditText;
    private TextInputEditText[] phraseTextEditTexts = new TextInputEditText[5];
    private TextInputEditText[][] phraseOptionEditTexts = new TextInputEditText[5][3];
    private Button saveButton, deleteButton;
    private FirebaseFirestore db;
    private String dialogueId;

    /**
     * Инициализирует активность, устанавливает layout, связывает элементы интерфейса,
     * загружает данные диалога по ID и настраивает обработчики для кнопок.
     *
     * @param savedInstanceState Сохраненное состояние активности
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogue_edit);

        dialogueIdEditText = findViewById(R.id.dialogueIdEditText);
        characterEditText = findViewById(R.id.characterEditText);

        phraseTextEditTexts[0] = findViewById(R.id.phrase1TextEditText);
        phraseTextEditTexts[1] = findViewById(R.id.phrase2TextEditText);
        phraseTextEditTexts[2] = findViewById(R.id.phrase3TextEditText);
        phraseTextEditTexts[3] = findViewById(R.id.phrase4TextEditText);
        phraseTextEditTexts[4] = findViewById(R.id.phrase5TextEditText);

        phraseOptionEditTexts[0][0] = findViewById(R.id.phrase1Option1EditText);
        phraseOptionEditTexts[0][1] = findViewById(R.id.phrase1Option2EditText);
        phraseOptionEditTexts[0][2] = findViewById(R.id.phrase1Option3EditText);
        phraseOptionEditTexts[1][0] = findViewById(R.id.phrase2Option1EditText);
        phraseOptionEditTexts[1][1] = findViewById(R.id.phrase2Option2EditText);
        phraseOptionEditTexts[1][2] = findViewById(R.id.phrase2Option3EditText);
        phraseOptionEditTexts[2][0] = findViewById(R.id.phrase3Option1EditText);
        phraseOptionEditTexts[2][1] = findViewById(R.id.phrase3Option2EditText);
        phraseOptionEditTexts[2][2] = findViewById(R.id.phrase3Option3EditText);
        phraseOptionEditTexts[3][0] = findViewById(R.id.phrase4Option1EditText);
        phraseOptionEditTexts[3][1] = findViewById(R.id.phrase4Option2EditText);
        phraseOptionEditTexts[3][2] = findViewById(R.id.phrase4Option3EditText);
        phraseOptionEditTexts[4][0] = findViewById(R.id.phrase5Option1EditText);
        phraseOptionEditTexts[4][1] = findViewById(R.id.phrase5Option2EditText);
        phraseOptionEditTexts[4][2] = findViewById(R.id.phrase5Option3EditText);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        db = FirebaseFirestore.getInstance();

        dialogueId = getIntent().getStringExtra("DIALOGUE_ID");
        if (dialogueId != null) {
            dialogueIdEditText.setText(dialogueId);
            dialogueIdEditText.setEnabled(false);
            loadDialogueData();
        }

        saveButton.setOnClickListener(v -> saveDialogue());
        deleteButton.setOnClickListener(v -> deleteDialogue());
    }

    /**
     * Загружает данные диалога из Firestore по ID и заполняет поля ввода.
     */
    private void loadDialogueData() {
        db.collection("dialogues").document(dialogueId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String character = documentSnapshot.getString("character");
                        List<Map<String, Object>> options = (List<Map<String, Object>>) documentSnapshot.get("options");

                        characterEditText.setText(character);
                        if (options != null && options.size() == 5) {
                            for (int i = 0; i < 5; i++) {
                                Map<String, Object> phrase = options.get(i);
                                String text = (String) phrase.get("text");
                                List<Map<String, Object>> answers = (List<Map<String, Object>>) phrase.get("answers");

                                phraseTextEditTexts[i].setText(text);
                                if (answers != null && answers.size() == 3) {
                                    for (int j = 0; j < 3; j++) {
                                        String answerText = (String) answers.get(j).get("text");
                                        phraseOptionEditTexts[i][j].setText(answerText);
                                    }
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Диалог не найден", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки диалога: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Собирает данные из полей ввода, выполняет валидацию (пустота, английский язык, уникальность)
     * и сохраняет обновленный диалог в Firestore.
     */
    private void saveDialogue() {
        String dialogueId = dialogueIdEditText.getText().toString().trim();
        String character = characterEditText.getText().toString().trim();

        String[] phraseTexts = new String[5];
        String[][] phraseOptions = new String[5][3];

        for (int i = 0; i < 5; i++) {
            phraseTexts[i] = phraseTextEditTexts[i].getText().toString().trim();
            for (int j = 0; j < 3; j++) {
                phraseOptions[i][j] = phraseOptionEditTexts[i][j].getText().toString().trim();
            }
        }

        if (dialogueId.isEmpty() || character.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните ID диалога и имя персонажа", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0; i < 5; i++) {
            if (phraseTexts[i].isEmpty() || phraseOptions[i][0].isEmpty() ||
                    phraseOptions[i][1].isEmpty() || phraseOptions[i][2].isEmpty()) {
                Toast.makeText(this, "Пожалуйста, заполните все поля для фразы " + (i + 1), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        for (int i = 0; i < 5; i++) {
            if (!phraseTexts[i].matches("^[a-zA-Z\\s.,!?'-]+$")) {
                Toast.makeText(this, "Фраза " + (i + 1) + " должна быть на английском языке", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        for (int i = 0; i < 5; i++) {
            Set<String> optionsSet = new HashSet<>();
            for (int j = 0; j < 3; j++) {
                String option = phraseOptions[i][j];

                if (!option.matches("^[a-zA-Z\\s.,!?'-]+$")) {
                    Toast.makeText(this, "Фраза " + (i + 1) + ", вариант " + (j + 1) + " должен быть на английском языке", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!optionsSet.add(option)) {
                    Toast.makeText(this, "Фраза " + (i + 1) + " содержит повторяющиеся варианты", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        Map<String, Object> dialogueData = new HashMap<>();
        dialogueData.put("character", character);

        List<Map<String, Object>> options = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> phrase = new HashMap<>();
            phrase.put("text", phraseTexts[i]);
            List<Map<String, Object>> answers = new ArrayList<>();
            answers.add(createAnswer(phraseOptions[i][0], true));
            answers.add(createAnswer(phraseOptions[i][1], false));
            answers.add(createAnswer(phraseOptions[i][2], false));
            phrase.put("answers", answers);
            options.add(phrase);
        }

        dialogueData.put("options", options);

        db.collection("dialogues")
                .document(dialogueId)
                .set(dialogueData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Диалог успешно сохранен", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка при сохранении диалога: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Удаляет диалог из Firestore по его ID.
     */
    private void deleteDialogue() {
        if (dialogueId == null) {
            return;
        }

        db.collection("dialogues")
                .document(dialogueId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка при удалении диалога: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Создает объект ответа с текстом и флагом правильности.
     *
     * @param text      Текст ответа
     * @param isCorrect Флаг, указывающий, правильный ли ответ
     * @return Карта с данными ответа
     */
    private Map<String, Object> createAnswer(String text, boolean isCorrect) {
        Map<String, Object> answer = new HashMap<>();
        answer.put("text", text);
        answer.put("isCorrect", isCorrect);
        return answer;
    }
}