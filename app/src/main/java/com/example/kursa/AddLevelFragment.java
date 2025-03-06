package com.example.kursa;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class AddLevelFragment extends Fragment {

    private EditText levelNameEditText;
    private EditText[] englishWords = new EditText[10];
    private EditText[] translations = new EditText[10];
    private Button addLevelButton;

    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frame_add_level, container, false);

        db = FirebaseFirestore.getInstance();

        levelNameEditText = view.findViewById(R.id.levelNameEditText);

        englishWords[0] = view.findViewById(R.id.englishWord1);
        englishWords[1] = view.findViewById(R.id.englishWord2);
        englishWords[2] = view.findViewById(R.id.englishWord3);
        englishWords[3] = view.findViewById(R.id.englishWord4);
        englishWords[4] = view.findViewById(R.id.englishWord5);
        englishWords[5] = view.findViewById(R.id.englishWord6);
        englishWords[6] = view.findViewById(R.id.englishWord7);
        englishWords[7] = view.findViewById(R.id.englishWord8);
        englishWords[8] = view.findViewById(R.id.englishWord9);
        englishWords[9] = view.findViewById(R.id.englishWord10);

        translations[0] = view.findViewById(R.id.translation1);
        translations[1] = view.findViewById(R.id.translation2);
        translations[2] = view.findViewById(R.id.translation3);
        translations[3] = view.findViewById(R.id.translation4);
        translations[4] = view.findViewById(R.id.translation5);
        translations[5] = view.findViewById(R.id.translation6);
        translations[6] = view.findViewById(R.id.translation7);
        translations[7] = view.findViewById(R.id.translation8);
        translations[8] = view.findViewById(R.id.translation9);
        translations[9] = view.findViewById(R.id.translation10);

        addLevelButton = view.findViewById(R.id.addLevelButton);

        addLevelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLevelToFirestore();
            }
        });

        return view;
    }

    private void addLevelToFirestore() {
        String levelName = levelNameEditText.getText().toString().trim();

        if (levelName.isEmpty()) {
            levelNameEditText.setError("Введите название уровня");
            return;
        }

        Map<String, String> wordsMap = new HashMap<>();

        for (int i = 0; i < 10; i++) {
            String englishWord = englishWords[i].getText().toString().trim();
            String translation = translations[i].getText().toString().trim();

            if (englishWord.isEmpty() || translation.isEmpty()) {
                Toast.makeText(getContext(), "Заполните все поля слов и переводов", Toast.LENGTH_SHORT).show();
                return;
            }

            wordsMap.put(englishWord, translation);
            Log.d("Firestore", "Добавлено слово: " + englishWord + " -> " + translation);
        }

        Log.d("Firestore", "Всего добавлено слов: " + wordsMap.size());

        generateLevelId(new OnLevelIdGeneratedListener() {
            @Override
            public void onLevelIdGenerated(String levelId) {
                Map<String, Object> levelDetails = new HashMap<>();
                levelDetails.put("isUnlocked", true);
                levelDetails.put("words", wordsMap);

                Map<String, Object> level = new HashMap<>();
                level.put("details", levelDetails);
                level.put("levelName", levelName);

                db.collection("levelsAll")
                        .document(levelId)
                        .set(levelDetails)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(), "Уровень успешно добавлен", Toast.LENGTH_SHORT).show();
                                resetFields();
                                bindLevelToUsers(level);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Ошибка при добавлении уровня", Toast.LENGTH_SHORT).show();
                                Log.e("Firestore", "Ошибка: ", e);
                            }
                        });
            }
        });
    }

    private void resetFields() {
        levelNameEditText.setText("");

        for (int i = 0; i < 10; i++) {
            englishWords[i].setText("");
            translations[i].setText("");
        }
    }

    private void generateLevelId(OnLevelIdGeneratedListener listener) {
        db.collection("levelsAll")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        int levelCount = queryDocumentSnapshots.size();

                        String levelId = "level" + (levelCount + 1);

                        listener.onLevelIdGenerated(levelId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Ошибка при получении количества уровней", e);
                        listener.onLevelIdGenerated("level1");
                    }
                });
    }

    private void bindLevelToUsers(Map<String, Object> level) {
        db.collection("users")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String nickname = document.getString("nickname");

                            if (nickname != null && !nickname.isEmpty()) {
                                db.collection("levels")
                                        .document(nickname)
                                        .update("levels", FieldValue.arrayUnion(level))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("Firestore", "Уровень добавлен пользователю: " + nickname);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                if (e.getMessage() != null && e.getMessage().contains("No document to update")) {
                                                    Map<String, Object> userLevels = new HashMap<>();
                                                    userLevels.put("levels", FieldValue.arrayUnion(level));

                                                    db.collection("levels")
                                                            .document(nickname)
                                                            .set(userLevels)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.d("Firestore", "Документ создан и уровень добавлен пользователю: " + nickname);
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.e("Firestore", "Ошибка при создании документа: " + nickname, e);
                                                                }
                                                            });
                                                } else {
                                                    Log.e("Firestore", "Ошибка при добавлении уровня пользователю: " + nickname, e);
                                                }
                                            }
                                        });
                            } else {
                                Log.e("Firestore", "Никнейм пользователя пустой или null");
                            }
                        }

                        Toast.makeText(getContext(), "Уровень привязан ко всем пользователям", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Ошибка при привязке уровня", Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Ошибка при получении пользователей", e);
                    }
                });
    }

    interface OnLevelIdGeneratedListener {
        void onLevelIdGenerated(String levelId);
    }
}