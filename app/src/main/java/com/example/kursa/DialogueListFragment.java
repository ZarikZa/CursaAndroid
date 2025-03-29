package com.example.kursa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDialogueFragment extends Fragment {

    private EditText dialogueIdEditText, characterEditText;
    private EditText phrase1TextEditText, phrase1Option1EditText, phrase1Option2EditText, phrase1Option3EditText;
    private EditText phrase2TextEditText, phrase2Option1EditText, phrase2Option2EditText, phrase2Option3EditText;
    // Добавьте поля для фраз 3, 4, 5
    private Button saveButton;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_dialogue_add, container, false);

        // Инициализация элементов
        dialogueIdEditText = view.findViewById(R.id.dialogueIdEditText);
        characterEditText = view.findViewById(R.id.characterEditText);
        phrase1TextEditText = view.findViewById(R.id.phrase1TextEditText);
        phrase1Option1EditText = view.findViewById(R.id.phrase1Option1EditText);
        phrase1Option2EditText = view.findViewById(R.id.phrase1Option2EditText);
        phrase1Option3EditText = view.findViewById(R.id.phrase1Option3EditText);
        phrase2TextEditText = view.findViewById(R.id.phrase2TextEditText);
        phrase2Option1EditText = view.findViewById(R.id.phrase2Option1EditText);
        phrase2Option2EditText = view.findViewById(R.id.phrase2Option2EditText);
        phrase2Option3EditText = view.findViewById(R.id.phrase2Option3EditText);
        // Инициализируйте поля для фраз 3, 4, 5
        saveButton = view.findViewById(R.id.saveButton);
        db = FirebaseFirestore.getInstance();

        saveButton.setOnClickListener(v -> saveDialogue());

        return view;
    }

    private void saveDialogue() {
        String dialogueId = dialogueIdEditText.getText().toString().trim();
        String character = characterEditText.getText().toString().trim();
        String phrase1Text = phrase1TextEditText.getText().toString().trim();
        String phrase1Option1 = phrase1Option1EditText.getText().toString().trim();
        String phrase1Option2 = phrase1Option2EditText.getText().toString().trim();
        String phrase1Option3 = phrase1Option3EditText.getText().toString().trim();
        String phrase2Text = phrase2TextEditText.getText().toString().trim();
        String phrase2Option1 = phrase2Option1EditText.getText().toString().trim();
        String phrase2Option2 = phrase2Option2EditText.getText().toString().trim();
        String phrase2Option3 = phrase2Option3EditText.getText().toString().trim();
        // Добавьте поля для фраз 3, 4, 5

        // Проверка обязательных полей
        if (dialogueId.isEmpty() || character.isEmpty() || phrase1Text.isEmpty() || phrase1Option1.isEmpty() ||
                phrase1Option2.isEmpty() || phrase1Option3.isEmpty() || phrase2Text.isEmpty() || phrase2Option1.isEmpty() ||
                phrase2Option2.isEmpty() || phrase2Option3.isEmpty() /* добавьте проверки для 3, 4, 5 */) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Формируем данные
        Map<String, Object> dialogueData = new HashMap<>();
        dialogueData.put("character", character);

        List<Map<String, Object>> options = new ArrayList<>();

        // Фраза 1
        Map<String, Object> phrase1 = new HashMap<>();
        phrase1.put("text", phrase1Text);
        List<Map<String, Object>> answers1 = new ArrayList<>();
        answers1.add(createAnswer(phrase1Option1, true));
        answers1.add(createAnswer(phrase1Option2, false));
        answers1.add(createAnswer(phrase1Option3, false));
        phrase1.put("answers", answers1);
        options.add(phrase1);

        // Фраза 2
        Map<String, Object> phrase2 = new HashMap<>();
        phrase2.put("text", phrase2Text);
        List<Map<String, Object>> answers2 = new ArrayList<>();
        answers2.add(createAnswer(phrase2Option1, true));
        answers2.add(createAnswer(phrase2Option2, false));
        answers2.add(createAnswer(phrase2Option3, false));
        phrase2.put("answers", answers2);
        options.add(phrase2);

        // Добавьте фразы 3, 4, 5 аналогично

        dialogueData.put("options", options);

        // Сохранение в Firestore
        db.collection("dialogues")
                .document(dialogueId)
                .set(dialogueData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Dialogue saved successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error saving dialogue: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private Map<String, Object> createAnswer(String text, boolean isCorrect) {
        Map<String, Object> answer = new HashMap<>();
        answer.put("text", text);
        answer.put("isCorrect", isCorrect);
        return answer;
    }

    private void clearFields() {
        dialogueIdEditText.setText("");
        characterEditText.setText("");
        phrase1TextEditText.setText("");
        phrase1Option1EditText.setText("");
        phrase1Option2EditText.setText("");
        phrase1Option3EditText.setText("");
        phrase2TextEditText.setText("");
        phrase2Option1EditText.setText("");
        phrase2Option2EditText.setText("");
        phrase2Option3EditText.setText("");
        // Очистите поля для фраз 3, 4, 5
    }
}