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

public class DialogueEditActivity extends AppCompatActivity {

    private TextInputEditText dialogueIdEditText, characterEditText;
    private TextInputEditText[] phraseTextEditTexts = new TextInputEditText[5];
    private TextInputEditText[][] phraseOptionEditTexts = new TextInputEditText[5][3];
    private Button saveButton, deleteButton;
    private FirebaseFirestore db;
    private String dialogueId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogue_edit);

        // Инициализация элементов
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
                        Toast.makeText(this, "Dialogue not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading dialogue: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveDialogue() {
        String dialogueId = dialogueIdEditText.getText().toString().trim();
        String character = characterEditText.getText().toString().trim();

        String[] phraseTexts = new String[5];
        String[][] phraseOptions = new String[5][3];

        // Сбор данных из полей
        for (int i = 0; i < 5; i++) {
            phraseTexts[i] = phraseTextEditTexts[i].getText().toString().trim();
            for (int j = 0; j < 3; j++) {
                phraseOptions[i][j] = phraseOptionEditTexts[i][j].getText().toString().trim();
            }
        }

        // Проверка на пустые поля
        if (dialogueId.isEmpty() || character.isEmpty()) {
            Toast.makeText(this, "Please fill in Dialogue ID and Character", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0; i < 5; i++) {
            if (phraseTexts[i].isEmpty() || phraseOptions[i][0].isEmpty() ||
                    phraseOptions[i][1].isEmpty() || phraseOptions[i][2].isEmpty()) {
                Toast.makeText(this, "Please fill in all fields for Phrase " + (i + 1), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Проверка, что фразы на английском
        for (int i = 0; i < 5; i++) {
            if (!phraseTexts[i].matches("^[a-zA-Z\\s.,!?'-]+$")) {
                Toast.makeText(this, "Phrase " + (i + 1) + " must be in English", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Проверка уникальности и английского языка для вариантов ответа
        for (int i = 0; i < 5; i++) {
            Set<String> optionsSet = new HashSet<>();
            for (int j = 0; j < 3; j++) {
                String option = phraseOptions[i][j];

                // Проверка на английский язык
                if (!option.matches("^[a-zA-Z\\s.,!?'-]+$")) {
                    Toast.makeText(this, "Phrase " + (i + 1) + " Option " + (j + 1) + " must be in English", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Проверка на уникальность
                if (!optionsSet.add(option)) {
                    Toast.makeText(this, "Phrase " + (i + 1) + " has duplicate options", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        // Формируем данные
        Map<String, Object> dialogueData = new HashMap<>();
        dialogueData.put("character", character);

        List<Map<String, Object>> options = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> phrase = new HashMap<>();
            phrase.put("text", phraseTexts[i]);
            List<Map<String, Object>> answers = new ArrayList<>();
            answers.add(createAnswer(phraseOptions[i][0], true)); // Option 1 - правильный
            answers.add(createAnswer(phraseOptions[i][1], false));
            answers.add(createAnswer(phraseOptions[i][2], false));
            phrase.put("answers", answers);
            options.add(phrase);
        }

        dialogueData.put("options", options);

        // Сохранение в Firestore
        db.collection("dialogues")
                .document(dialogueId)
                .set(dialogueData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Dialogue saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving dialogue: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteDialogue() {
        if (dialogueId == null) {
            Toast.makeText(this, "No dialogue to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("dialogues")
                .document(dialogueId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Dialogue deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting dialogue: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private Map<String, Object> createAnswer(String text, boolean isCorrect) {
        Map<String, Object> answer = new HashMap<>();
        answer.put("text", text);
        answer.put("isCorrect", isCorrect);
        return answer;
    }
}