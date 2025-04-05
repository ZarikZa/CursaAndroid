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
            Toast.makeText(this, "Error: Missing user or dialogue ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backBtm.setOnClickListener(v -> {
            finish();
        });

        loadDialogue();
        //loadPlayerProgress();
    }

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
                            Toast.makeText(this, "Dialogue not found or completed", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading dialogue: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

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
                Toast.makeText(this, "Dialogue completed!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }, 2000);
    }

//    private void savePlayerProgress(int choiceIndex) {
//        Map<String, Object> playerData = new HashMap<>();
//        playerData.put("currentStep", currentStep + 1);
//        playerData.put("dialogueId", dialogueId);
//
//        db.collection("players")
//                .document(userId)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    List<Integer> choices = new ArrayList<>();
//                    if (documentSnapshot.exists()) {
//                        choices = (List<Integer>) documentSnapshot.get("choices");
//                        if (choices == null) {
//                            choices = new ArrayList<>();
//                        }
//                    }
//                    choices.add(choiceIndex);
//                    playerData.put("choices", choices);
//
//                    db.collection("players")
//                            .document(userId)
//                            .set(playerData);
//                });
//    }
//
//    private void loadPlayerProgress() {
//        db.collection("players")
//                .document(userId)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        String savedDialogueId = documentSnapshot.getString("dialogueId");
//                        if (savedDialogueId != null && savedDialogueId.equals(dialogueId)) {
//                            Long step = documentSnapshot.getLong("currentStep");
//                            if (step != null) {
//                                currentStep = step.intValue();
//                                if (currentStep >= dialogueOptions.size()) {
//                                    Toast.makeText(this, "Dialogue already completed", Toast.LENGTH_SHORT).show();
//                                    finish();
//                                } else {
//                                    displayStep(currentStep);
//                                }
//                            }
//                        }
//                    }
//                });
//    }
}