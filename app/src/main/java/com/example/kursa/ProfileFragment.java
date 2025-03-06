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
import com.google.firebase.firestore.Query;

import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView loginTextView, nicknameTextView, rankingTextView, wordsLearnedTextView, reytingPoint;
    private FirebaseFirestore db;
    private Button logoutButton, changePasswordButton;
    private String login;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frame_profile, container, false);

        loginTextView = view.findViewById(R.id.loginTextView);
        nicknameTextView = view.findViewById(R.id.nicknameTextView);
        rankingTextView = view.findViewById(R.id.rankingTextView);
        wordsLearnedTextView = view.findViewById(R.id.wordsLearnedTextView);
        reytingPoint = view.findViewById(R.id.reytingPointTV);
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
                        String userId = document.getId();
                        long reytingPoints = document.getLong("reytingPoints");

                        loginTextView.setText("Логин: " + login);

                        fetchWordsLearnedData(nickname);

                        fetchRanking(userId, reytingPoints);
                    } else {
                        Log.e("Firestore", "No user found with nickname: " + nickname);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching user data", e);
                });
    }

    private void fetchWordsLearnedData(String nickname) {
        db.collection("usersLearnedWords")
                .document(nickname)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> words = (Map<String, Object>) documentSnapshot.get("words");

                        if (words != null) {
                            int wordsLearned = words.size();
                            wordsLearnedTextView.setText("Изучено слов: " + wordsLearned);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching words learned data", e);
                });
    }

    private void fetchRanking(String userId, long userReytingPoints) {
        db.collection("users")
                .orderBy("reytingPoints", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        int rank = 1;
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            if (document.getId().equals(userId)) {
                                rankingTextView.setText("Место в рейтинге: " + rank);
                                reytingPoint.setText("Количество рейтинговых баллов: " + userReytingPoints);
                                break;
                            }
                            rank++;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching ranking data", e);
                });
    }
}
