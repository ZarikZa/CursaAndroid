package com.example.kursa;

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

import java.util.Random;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText emailEditText;
    private EditText verificationCodeEditText;
    private EditText newPasswordEditText;
    private EditText confirmNewPasswordEditText;
    private Button sendCodeButton, verifyCodeButton, changePasswordButton;
    private ImageButton backButton;
    private TextView loginTitle;

    private FirebaseFirestore db;
    private String userId;
    private String verificationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.email);
        verificationCodeEditText = findViewById(R.id.verificationCode);
        newPasswordEditText = findViewById(R.id.newPassword);
        confirmNewPasswordEditText = findViewById(R.id.confirmNewPassword);
        sendCodeButton = findViewById(R.id.sendCodeButton);
        verifyCodeButton = findViewById(R.id.verifyCodeButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        loginTitle = findViewById(R.id.loginTitle);

        verificationCodeEditText.setVisibility(View.GONE);
        verifyCodeButton.setVisibility(View.GONE);
        newPasswordEditText.setVisibility(View.GONE);
        confirmNewPasswordEditText.setVisibility(View.GONE);
        changePasswordButton.setVisibility(View.GONE);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredEmail = emailEditText.getText().toString();

                if (enteredEmail.isEmpty()) {
                    emailEditText.setError("Введите email");
                    return;
                }

                // Поиск пользователя по email в Firestore
                db.collection("users")
                        .whereEqualTo("email", enteredEmail)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                                    String firestoreEmail = document.getString("email");

                                    if (firestoreEmail != null && firestoreEmail.equals(enteredEmail)) {
                                        userId = document.getId();

                                        verificationCode = generateVerificationCode();

                                        String senderEmail = "ovetalingoveta@gmail.com";
                                        String senderPassword = "wzmc djkk phpx kswu";
                                        String subject = "Код подтверждения";
                                        String body = "Ваш код подтверждения: " + verificationCode;

                                        new SendMailTask(senderEmail, senderPassword, enteredEmail, subject, body).execute();

                                        emailEditText.setVisibility(View.GONE);
                                        sendCodeButton.setVisibility(View.GONE);
                                        verificationCodeEditText.setVisibility(View.VISIBLE);
                                        verifyCodeButton.setVisibility(View.VISIBLE);

                                        Toast.makeText(ChangePasswordActivity.this, "Код отправлен на ваш email", Toast.LENGTH_SHORT).show();
                                    } else {
                                        emailEditText.setError("Неверный email");
                                    }
                                } else {
                                    emailEditText.setError("Пользователь не найден");
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ChangePasswordActivity.this, "Ошибка при проверке email", Toast.LENGTH_SHORT).show();
                                Log.e("Firestore", "Ошибка при проверке email", e);
                            }
                        });
            }
        });

        verifyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredCode = verificationCodeEditText.getText().toString();

                if (enteredCode.isEmpty()) {
                    verificationCodeEditText.setError("Введите код");
                    return;
                }

                if (enteredCode.equals(verificationCode)) {
                    verificationCodeEditText.setVisibility(View.GONE);
                    verifyCodeButton.setVisibility(View.GONE);
                    newPasswordEditText.setVisibility(View.VISIBLE);
                    confirmNewPasswordEditText.setVisibility(View.VISIBLE);
                    changePasswordButton.setVisibility(View.VISIBLE);

                    Toast.makeText(ChangePasswordActivity.this, "Код подтвержден", Toast.LENGTH_SHORT).show();
                } else {
                    verificationCodeEditText.setError("Неверный код");
                }
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
                if (!isPasswordValid(newPassword)) {
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

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}