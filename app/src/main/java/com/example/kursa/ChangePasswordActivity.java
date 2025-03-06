package com.example.kursa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText newPasswordEditText;
    private EditText confirmNewPasswordEditText;
    private Button loginButton, changePasswordButton;
    private ImageButton backButton;
    private TextView loginTitle;

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        db = FirebaseFirestore.getInstance();

        usernameEditText = findViewById(R.id.username);
        newPasswordEditText = findViewById(R.id.newPassword);
        confirmNewPasswordEditText = findViewById(R.id.confirmNewPassword);
        loginButton = findViewById(R.id.loginButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        loginTitle = findViewById(R.id.loginTitle);

        newPasswordEditText.setVisibility(View.GONE);
        confirmNewPasswordEditText.setVisibility(View.GONE);
        changePasswordButton.setVisibility(View.GONE);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v ->{
            finish();
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredUsername = usernameEditText.getText().toString();

                if (enteredUsername.isEmpty()) {
                    usernameEditText.setError("Введите логин");
                    return;
                }

                db.collection("users")
                        .whereEqualTo("login", enteredUsername)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                                    String firestoreLogin = document.getString("login");

                                    if (firestoreLogin != null && firestoreLogin.equals(enteredUsername)) {
                                        userId = document.getId();

                                        usernameEditText.setVisibility(View.GONE);
                                        loginButton.setVisibility(View.GONE);

                                        newPasswordEditText.setVisibility(View.VISIBLE);
                                        confirmNewPasswordEditText.setVisibility(View.VISIBLE);
                                        changePasswordButton.setVisibility(View.VISIBLE);

                                        loginTitle.setText("Смена пароля");
                                    } else {
                                        usernameEditText.setError("Неверный логин");
                                    }
                                } else {
                                    usernameEditText.setError("Пользователь не найден");
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ChangePasswordActivity.this, "Ошибка при проверке логина", Toast.LENGTH_SHORT).show();
                                Log.e("Firestore", "Ошибка при проверке логина", e);
                            }
                        });
            }
        });

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = newPasswordEditText.getText().toString();
                String confirmPassword = confirmNewPasswordEditText.getText().toString();

                if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(ChangePasswordActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isPasswordValid(newPassword)){
                    return;
                }

                if (newPassword.equals(confirmPassword)) {
                    db.collection("users")
                            .document(userId)
                            .update("password", newPassword)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(ChangePasswordActivity.this, "Пароль успешно изменен", Toast.LENGTH_SHORT).show();

                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ChangePasswordActivity.this, "Ошибка при изменении пароля", Toast.LENGTH_SHORT).show();
                                    Log.e("Firestore", "Ошибка при изменении пароля", e);
                                }
                            });
                } else {
                    confirmNewPasswordEditText.setError("Пароли не совпадают");
                }
            }
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