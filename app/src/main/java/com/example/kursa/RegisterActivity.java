package com.example.kursa;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private ImageButton back;
    private EditText nicknameEditText, usernameEditText, passwordEditText;
    private FirebaseFirestore db;
    private Button registr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        nicknameEditText = findViewById(R.id.nickname);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        db = FirebaseFirestore.getInstance();

        back = findViewById(R.id.backButton);
        back.setOnClickListener(v -> finish());

        registr = findViewById(R.id.loginButton);
        registr.setOnClickListener(v -> onRegisterButtonClick());
    }

    public void onRegisterButtonClick() {
        String nickname = nicknameEditText.getText().toString();
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (nickname.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nickname.length() <= 5) {
            Toast.makeText(this, "Никнейм должен быть длиннее 5 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() <= 6) {
            Toast.makeText(this, "Логин должен быть больше 6 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        checkNicknameAvailability(nickname, username, password);
    }

    private void checkNicknameAvailability(String nickname, String username, String password) {
        db.collection("users")
                .whereEqualTo("nickname", nickname)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(RegisterActivity.this, "Никнейм уже занят", Toast.LENGTH_SHORT).show();
                    } else {
                        checkUsernameAvailability(username, password, nickname);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Ошибка при проверке никнейма: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUsernameAvailability(String username, String password, String nickname) {
        db.collection("users")
                .whereEqualTo("login", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(RegisterActivity.this, "Логин уже занят", Toast.LENGTH_SHORT).show();
                    } else {
                        if (!isPasswordValid(password)) {
                            Toast.makeText(this, "Пароль должен содержать минимум 1 заглавную букву, 1 строчную букву, цифру и быть больше 8 символов", Toast.LENGTH_SHORT).show();
                        } else {
                            addUserToDatabase(nickname, username, password);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Ошибка при проверке логина: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addUserToDatabase(String nickname, String username, String password) {
        Map<String, Object> user = new HashMap<>();
        user.put("nickname", nickname);
        user.put("login", username);
        user.put("password", password);
        user.put("role", "user");
        user.put("reytingPoints", 0);

        db.collection("users")
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    addLevelsForUser(nickname);
                    Toast.makeText(RegisterActivity.this, "Регистрация успешна", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Ошибка регистрации: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addLevelsForUser(String nickname) {
        db.collection("levelsAll")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, Object> userLevels = new HashMap<>();
                    userLevels.put("levels", new ArrayList<>());

                    for (DocumentSnapshot levelDoc : querySnapshot.getDocuments()) {
                        Map<String, Object> levelData = levelDoc.getData();

                        Map<String, String> wordsMap = (Map<String, String>) levelData.get("words");

                        Map<String, Object> details = new HashMap<>();
                        details.put("isUnlocked", levelDoc.getId().equals("level1"));
                        details.put("words", wordsMap);

                        Map<String, Object> level = new HashMap<>();
                        level.put("levelName", levelData.get("levelName"));
                        level.put("details", details);

                        ((ArrayList<Map<String, Object>>) userLevels.get("levels")).add(level);
                    }

                    db.collection("levels")
                            .document(nickname)
                            .set(userLevels)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(RegisterActivity.this, "Уровни успешно добавлены", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(RegisterActivity.this, "Ошибка при добавлении уровней: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Ошибка при загрузке уровней: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isPasswordValid(String password) {
        if (password.length() <= 8) {
            Toast.makeText(this, "Пароль должен быть больше 8 символов", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.matches(".*[A-Z].*")) {
            Toast.makeText(this, "Пароль должен содержать хотя бы одну заглавную букву", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.matches(".*[a-z].*")) {
            Toast.makeText(this, "Пароль должен содержать хотя бы одну строчную букву", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.matches(".*\\d.*")) {
            Toast.makeText(this, "Пароль должен содержать хотя бы одну цифру", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}