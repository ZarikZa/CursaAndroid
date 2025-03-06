package com.example.kursa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileAdminFragment extends Fragment {

    private TextView loginTextView, nicknameTextView;
    private FirebaseFirestore db;
    private Button logoutButton, changePasswordButton;
    private String login;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        logoutButton.setOnClickListener(v ->{
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });

        changePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
            intent.putExtra("login", login);
            startActivity(intent);
        });

        return view;
    }

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
