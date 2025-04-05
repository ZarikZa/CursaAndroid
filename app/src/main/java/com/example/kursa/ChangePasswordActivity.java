package com.example.kursa;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import java.util.Properties;
import java.util.Random;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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

        // Инициализация UI элементов
        initializeViews();

        backButton.setOnClickListener(v -> finish());

        sendCodeButton.setOnClickListener(v -> handleSendCode());

        verifyCodeButton.setOnClickListener(v -> handleVerifyCode());

        changePasswordButton.setOnClickListener(v -> handleChangePassword());
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.email);
        verificationCodeEditText = findViewById(R.id.verificationCode);
        newPasswordEditText = findViewById(R.id.newPassword);
        confirmNewPasswordEditText = findViewById(R.id.confirmNewPassword);
        sendCodeButton = findViewById(R.id.sendCodeButton);
        verifyCodeButton = findViewById(R.id.verifyCodeButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        loginTitle = findViewById(R.id.loginTitle);
        backButton = findViewById(R.id.backButton);

        // Скрываем ненужные элементы на начальном этапе
        verificationCodeEditText.setVisibility(View.GONE);
        verifyCodeButton.setVisibility(View.GONE);
        newPasswordEditText.setVisibility(View.GONE);
        confirmNewPasswordEditText.setVisibility(View.GONE);
        changePasswordButton.setVisibility(View.GONE);
    }

    private void handleSendCode() {
        String enteredEmail = emailEditText.getText().toString().trim();

        if (enteredEmail.isEmpty()) {
            emailEditText.setError("Введите email");
            return;
        }

        if (!isNetworkConnected()) {
            showToast("Нет подключения к интернету");
            return;
        }

        checkEmailInFirestore(enteredEmail);
    }

    private void checkEmailInFirestore(String email) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String firestoreEmail = document.getString("email");

                        if (firestoreEmail != null && firestoreEmail.equals(email)) {
                            userId = document.getId();
                            verificationCode = generateVerificationCode();
                            sendVerificationEmail(email);
                        } else {
                            emailEditText.setError("Неверный email");
                        }
                    } else {
                        emailEditText.setError("Пользователь не найден");
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Ошибка при проверке email");
                    Log.e("Firestore", "Ошибка при проверке email", e);
                });
    }

    private void sendVerificationEmail(String recipientEmail) {
        new Thread(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.mail.ru");
                props.put("mail.smtp.port", "465");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

                String senderEmail = "ovetalingoveta@mail.ru";
                String senderPassword = "9rsCgqait3g2ACjjj6RS";

                Session session = Session.getInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(senderEmail, senderPassword);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(senderEmail));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject("Код подтверждения для Ovetaling");

                String htmlContent = "<html>" +
                        "<body style='background-color: #1c1c1c; padding: 20px; font-family: Arial, sans-serif; color: #E0E0E0;'>" +
                        "<div style='background-color: #363636; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); text-align: center;'>" +
                        "<h2 style='color: #e8def8;'>Код подтверждения для Ovetaling</h2>" +
                        "<p style='color: #E0E0E0;'>Ваш код подтверждения:</p>" +
                        "<p style='font-size: 24px; font-weight: bold; color: #e8def8;'>" + verificationCode + "</p>" +
                        "<p style='color: #E0E0E0; font-size: 12px;'>Если вы не запрашивали этот код, проигнорируйте это сообщение.</p>" +
                        "</div>" +
                        "</body>" +
                        "</html>";

                message.setContent(htmlContent, "text/html; charset=utf-8");

                Transport.send(message);

                runOnUiThread(() -> {
                    emailEditText.setVisibility(View.GONE);
                    sendCodeButton.setVisibility(View.GONE);
                    verificationCodeEditText.setVisibility(View.VISIBLE);
                    verifyCodeButton.setVisibility(View.VISIBLE);
                    showToast("Код отправлен на ваш email");
                });

            } catch (MessagingException e) {
                Log.e("EmailError", "Ошибка отправки email", e);
                runOnUiThread(() -> showToast("Ошибка отправки email: " + e.getMessage()));
            }
        }).start();
    }

    private void handleVerifyCode() {
        String enteredCode = verificationCodeEditText.getText().toString().trim();

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
            showToast("Код подтвержден");
        } else {
            verificationCodeEditText.setError("Неверный код");
        }
    }

    private void handleChangePassword() {
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmNewPasswordEditText.getText().toString().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Заполните все поля");
            return;
        }

        if (!isPasswordValid(newPassword)) {
            return;
        }

        if (newPassword.equals(confirmPassword)) {
            updatePasswordInFirestore(newPassword);
        } else {
            confirmNewPasswordEditText.setError("Пароли не совпадают");
        }
    }

    private void updatePasswordInFirestore(String newPassword) {
        db.collection("users")
                .document(userId)
                .update("password", newPassword)
                .addOnSuccessListener(aVoid -> {
                    showToast("Пароль успешно изменен");
                    finish();
                })
                .addOnFailureListener(e -> {
                    showToast("Ошибка при изменении пароля");
                    Log.e("Firestore", "Ошибка при изменении пароля", e);
                });
    }

    private boolean isPasswordValid(String password) {
        if (password.length() <= 8) {
            showToast("Пароль должен быть больше 8 символов");
            return false;
        }
        if (!password.matches(".*[A-Z].*")) {
            showToast("Пароль должен содержать хотя бы одну заглавную букву");
            return false;
        }
        if (!password.matches(".*[a-z].*")) {
            showToast("Пароль должен содержать хотя бы одну строчную букву");
            return false;
        }
        if (!password.matches(".*\\d.*")) {
            showToast("Пароль должен содержать хотя бы одну цифру");
            return false;
        }
        return true;
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}