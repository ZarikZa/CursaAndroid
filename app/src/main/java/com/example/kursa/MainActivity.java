package com.example.kursa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private Button enter;
    private Button regist;
    private TextView forgotPass;
    private EditText usernameEditText, passwordEditText;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember";
    private static final String KEY_ROLE = "role"; // Новый ключ для роли

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        WorkManagerHelper.scheduleDailyTask(this);

        boolean remember = sharedPreferences.getBoolean(KEY_REMEMBER, false);
        if (remember) {
            String savedUsername = sharedPreferences.getString(KEY_USERNAME, "");
            String savedRole = sharedPreferences.getString(KEY_ROLE, "user");
            Intent intent;
            if ("admin".equals(savedRole)) {
                intent = new Intent(MainActivity.this, NavigationAdminActivity.class);
            } else {
                intent = new Intent(MainActivity.this, NavigationActivity.class);
            }
            intent.putExtra("USER_NICKNAME", savedUsername);
            startActivity(intent);
            finish();
        }

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        db = FirebaseFirestore.getInstance();
        enter = findViewById(R.id.loginButton);
        regist = findViewById(R.id.registerButton);
        forgotPass = findViewById(R.id.forgotPassword);

        enter.setOnClickListener(v -> onLoginButtonClick());

        regist.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        forgotPass.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }

    public void onLoginButtonClick() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .whereEqualTo("login", username)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            // Успешный вход, получаем данные пользователя
                            DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
                            String nickname = userDoc.getString("nickname");
                            String role = userDoc.getString("role");

                            // Сохраняем данные в SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(KEY_USERNAME, username);
                            editor.putString(KEY_PASSWORD, password);
                            editor.putBoolean(KEY_REMEMBER, true);
                            editor.putString(KEY_ROLE, role); // Сохраняем роль как строку
                            editor.apply();

                            // Переход на нужную активность в зависимости от роли
                            Intent intent;
                            if ("user".equals(role)) {
                                intent = new Intent(MainActivity.this, NavigationActivity.class);
                                intent.putExtra("USER_NICKNAME", nickname);
                                startActivity(intent);
                                finish();
                            } else { // Предполагаем, что другая роль — "admin"
                                intent = new Intent(MainActivity.this, NavigationAdminActivity.class);
                                intent.putExtra("USER_NICKNAME", nickname);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Ошибка авторизации: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearSavedCredentials() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_PASSWORD);
        editor.remove(KEY_ROLE); // Удаляем роль при очистке
        editor.putBoolean(KEY_REMEMBER, false);
        editor.apply();
    }
}