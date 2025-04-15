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
/**
 * DialogueSelectionFragment — фрагмент для выбора диалога пользователем.
 * Загружает список диалогов из Firestore и отображает их в виде кнопок.
 * Каждая кнопка запускает DialogueActivity с соответствующим ID диалога и никнеймом пользователя.
 */
public class DialogueSelectionFragment extends Fragment {

    private LinearLayout dialogueContainer;
    private FirebaseFirestore db;
    private String nickname;

    /**
     * Инициализирует фрагмент, устанавливает layout, получает никнейм пользователя
     * из аргументов и подготавливает контейнер для диалогов.
     *
     * @param inflater           Объект для раздувания layout
     * @param container          Родительский контейнер
     * @param savedInstanceState Сохраненное состояние фрагмента
     * @return                   Надутый View фрагмента
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_dialogue_selection, container, false);
        dialogueContainer = view.findViewById(R.id.dialogueContainer);
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            nickname = getArguments().getString("USER_NICKNAME");
        }
        if (nickname == null) {
            Toast.makeText(requireContext(), "Ошибка: Отсутствует никнейм", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return view;
        }

        return view;
    }

    /**
     * Вызывается после создания представления, загружает список диалогов.
     *
     * @param view               Созданный View фрагмента
     * @param savedInstanceState Сохраненное состояние
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadDialogues();
    }

    /**
     * Загружает диалоги из Firestore и создает кнопки для каждого диалога.
     * Каждая кнопка отображает имя персонажа и запускает DialogueActivity.
     */
    private void loadDialogues() {
        if (getContext() == null) {
            return;
        }

        db.collection("dialogues")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String dialogueId = document.getId();
                        String character = document.getString("character");

                        Button dialogueButton = new Button(getContext());
                        dialogueButton.setText(character + " Диалог");
                        dialogueButton.setBackgroundResource(R.drawable.rounded_button_background);
                        dialogueButton.setTextColor(android.graphics.Color.parseColor("#1c1c1c"));
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 0, 0, 16);
                        dialogueButton.setLayoutParams(params);

                        dialogueButton.setOnClickListener(v -> {
                            Intent intent = new Intent(getActivity(), DialogueActivity.class);
                            intent.putExtra("nickname", nickname);
                            intent.putExtra("dialogueId", dialogueId);
                            startActivity(intent);
                        });

                        dialogueContainer.addView(dialogueButton);
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Ошибка загрузки диалогов: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}