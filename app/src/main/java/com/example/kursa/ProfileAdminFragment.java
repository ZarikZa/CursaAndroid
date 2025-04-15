package com.example.kursa;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
/**
 * ProfileAdminFragment — фрагмент для отображения профиля администратора.
 * Показывает логин и никнейм, позволяет сменить пароль или выйти из аккаунта.
 * Использует Firestore для получения данных пользователя и SharedPreferences для управления сессией.
 */
public class ProfileAdminFragment extends Fragment {

    private TextView loginTextView, nicknameTextView;
    private FirebaseFirestore db;
    private Button logoutButton, changePasswordButton;
    private String login;

    /**
     * Создает представление фрагмента, инициализирует элементы интерфейса
     * и настраивает обработчики событий.
     *
     * @param inflater           Объект для раздувания layout
     * @param container          Родительский контейнер
     * @param savedInstanceState Сохраненное состояние фрагмента
     * @return                   Надутый View фрагмента
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frame_profile_admin, container, false);

        loginTextView = view.findViewById(R.id.loginTextView);
        nicknameTextView = view.findViewById(R.id.nicknameTextView);
        logoutButton = view.findViewById(R.id.logoutButton);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        db = FirebaseFirestore.getInstance();

        Bundle bundle = getArguments();
        if (bundle != null) {
            String nickname = bundle.getString("USER_NICKNAME", "Default Nickname");
            nicknameTextView.setText("Никнейм: " + nickname);

            fetchUserData(nickname);
        }

        logoutButton.setOnClickListener(v -> showLogoutConfirmationDialog());

        changePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
            intent.putExtra("login", login);
            startActivity(intent);
        });

        return view;
    }

    /**
     * Показывает диалог подтверждения выхода из аккаунта.
     */
    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Подтверждение выхода")
                .setMessage("Вы уверены, что хотите выйти из аккаунта?")
                .setPositiveButton("Да", (dialog, which) -> performLogout())
                .setNegativeButton("Отмена", null)
                .show();
    }

    /**
     * Выполняет выход из аккаунта, очищает SharedPreferences и перенаправляет
     * на экран авторизации.
     */
    private void performLogout() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    /**
     * Загружает данные пользователя из Firestore по никнейму.
     *
     * @param nickname Никнейм пользователя
     */
    private void fetchUserData(String nickname) {
        db.collection("users")
                .whereEqualTo("nickname", nickname)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        login = document.getString("login");

                        loginTextView.setText("Логин: " + login);
                    } else {
                        Log.e("Firestore", "No user found with nickname: " + nickname);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching user data", e);
                });
    }
}