package com.example.kursa;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class DialogueListFragment extends Fragment {

    private LinearLayout dialogueContainer;
    private Button addDialogueButton;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialogue_list, container, false);

        dialogueContainer = view.findViewById(R.id.dialogueContainer);
        addDialogueButton = view.findViewById(R.id.addDialogueButton);
        db = FirebaseFirestore.getInstance();

        addDialogueButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), DialogueAddActivity.class);
            startActivity(intent);
        });

        loadDialogues();

        return view;
    }

    private void loadDialogues() {
        db.collection("dialogues")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return; // Проверяем, что фрагмент прикреплён
                    dialogueContainer.removeAllViews(); // Очищаем контейнер перед загрузкой
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String dialogueId = document.getId();

                        Button dialogueButton = new Button(requireContext());
                        dialogueButton.setText("Dialogue: " + dialogueId);
                        dialogueButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#363636")));
                        dialogueButton.setTextColor(android.graphics.Color.parseColor("#E0E0E0"));
                        dialogueButton.setTextSize(16);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 0, 0, 16);
                        dialogueButton.setLayoutParams(params);

                        dialogueButton.setOnClickListener(v -> {
                            Intent intent = new Intent(requireActivity(), DialogueEditActivity.class);
                            intent.putExtra("DIALOGUE_ID", dialogueId);
                            startActivity(intent);
                        });

                        dialogueContainer.addView(dialogueButton);
                    }
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(requireContext(), "No dialogues found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Error loading dialogues: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDialogues(); // Обновляем список диалогов при возвращении
    }
}