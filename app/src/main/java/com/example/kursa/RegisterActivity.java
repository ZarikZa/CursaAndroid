package com.example.kursa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
/**
 * RegisterActivity — активность для регистрации нового пользователя.
 * Проверяет корректность введенных данных (никнейм, логин, пароль, email),
 * обеспечивает уникальность никнейма и логина, сохраняет данные в Firestore
 * и добавляет пользователю доступные уровни.
 */
public class RegisterActivity extends AppCompatActivity {
    private ImageButton back;
    private EditText nicknameEditText, usernameEditText, passwordEditText, emailEditText;
    private FirebaseFirestore db;
    private Button registr;

    /**
     * Инициализирует активность, настраивает интерфейс и обработчики событий.
     *
     * @param savedInstanceState Сохраненное состояние активности
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        nicknameEditText = findViewById(R.id.nickname);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        emailEditText = findViewById(R.id.email);
        db = FirebaseFirestore.getInstance();

        back = findViewById(R.id.backButton);
        back.setOnClickListener(v -> finish());

        registr = findViewById(R.id.loginButton);
        registr.setOnClickListener(v -> onRegisterButtonClick());
    }

    /**
     * Обрабатывает нажатие кнопки регистрации, проверяет поля и запускает процесс регистрации.
     */
    public void onRegisterButtonClick() {
        String nickname = nicknameEditText.getText().toString();
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String email = emailEditText.getText().toString();

        if (nickname.isEmpty() || username.isEmpty() || password.isEmpty() || email.isEmpty()) {
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

        if (!isEmailValid(email)) {
            Toast.makeText(this, "Некорректный формат email", Toast.LENGTH_SHORT).show();
            return;
        }

        checkNicknameAvailability(nickname, username, password, email);
    }

    /**
     * Проверяет валидность email по регулярному выражению.
     *
     * @param email Введенный email
     * @return true, если email валиден, иначе false
     */
    private boolean isEmailValid(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    /**
     * Проверяет доступность никнейма в Firestore.
     *
     * @param nickname Никнейм для проверки
     * @param username Логин пользователя
     * @param password Пароль пользователя
     * @param email    Email пользователя
     */
    private void checkNicknameAvailability(String nickname, String username, String password, String email) {
        db.collection("users")
                .whereEqualTo("nickname", nickname)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(RegisterActivity.this, "Никнейм уже занят", Toast.LENGTH_SHORT).show();
                    } else {
                        checkUsernameAvailability(username, password, nickname, email);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Ошибка при проверке никнейма: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Проверяет доступность логина в Firestore и валидность пароля.
     *
     * @param username Логин для проверки
     * @param password Пароль пользователя
     * @param nickname Никнейм пользователя
     * @param email    Email пользователя
     */
    private void checkUsernameAvailability(String username, String password, String nickname, String email) {
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
                            addUserToDatabase(nickname, username, password, email);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Ошибка при проверке логина: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Добавляет нового пользователя в Firestore и инициализирует его уровни.
     *
     * @param nickname Никнейм пользователя
     * @param username Логин пользователя
     * @param password Пароль пользователя
     * @param email    Email пользователя
     */
    private void addUserToDatabase(String nickname, String username, String password, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("nickname", nickname);
        user.put("login", username);
        user.put("password", password);
        user.put("role", "user");
        user.put("reytingPoints", 0);
        user.put("email", email);

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

    /**
     * Добавляет доступные уровни для нового пользователя в Firestore.
     *
     * @param nickname Никнейм пользователя
     */
    private void addLevelsForUser(String nickname) {
        db.collection("levelsAll")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, Object> userLevels = new HashMap<>();
                    ArrayList<Map<String, Object>> levelsList = new ArrayList<>();
                    userLevels.put("levels", levelsList);

                    for (DocumentSnapshot levelDoc : querySnapshot.getDocuments()) {
                        Map<String, Object> levelData = levelDoc.getData();
                        if (levelData == null) continue;

                        Map<String, Object> detailsMap = (Map<String, Object>) levelData.get("details");
                        if (detailsMap == null) {
                            detailsMap = new HashMap<>();
                        }

                        Map<String, Object> level = new HashMap<>();
                        level.put("levelName", levelData.get("levelName"));
                        level.put("levelId", levelData.get("levelId"));

                        Map<String, Object> newDetails = new HashMap<>();
                        newDetails.put("isUnlocked", levelDoc.getId().equals("level1"));
                        newDetails.put("words", detailsMap.get("words"));

                        level.put("details", newDetails);
                        levelsList.add(level);
                    }

                    db.collection("levels")
                            .document(nickname)
                            .set(userLevels)
                            .addOnSuccessListener(aVoid -> {})
                            .addOnFailureListener(e -> {
                                Toast.makeText(RegisterActivity.this, "Ошибка при добавлении уровней", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Ошибка при загрузке уровней", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Проверяет валидность пароля по заданным критериям.
     *
     * @param password Пароль для проверки
     * @return true, если пароль валиден, иначе false
     */
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