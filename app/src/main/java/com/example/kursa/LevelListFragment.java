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
 * LevelListFragment — фрагмент для отображения списка уровней предложений.
 * Загружает уровни из Firestore, отображает их в виде кнопок, позволяет перейти
 * к редактированию существующего уровня или созданию нового.
 */
public class LevelListFragment extends Fragment {

    private LinearLayout levelContainer;
    private Button addLevelButton;
    private FirebaseFirestore db;

    /**
     * Инициализирует фрагмент, устанавливает layout, связывает элементы интерфейса
     * и настраивает обработчик для кнопки добавления уровня. Загружает список уровней.
     *
     * @param inflater           Объект для раздувания layout
     * @param container          Родительский контейнер
     * @param savedInstanceState Сохраненное состояние фрагмента
     * @return                   Надутый View фрагмента
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_level_list, container, false);

        levelContainer = view.findViewById(R.id.levelContainer);
        addLevelButton = view.findViewById(R.id.addLevelButton);
        db = FirebaseFirestore.getInstance();

        addLevelButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), LevelAddActivity.class);
            startActivity(intent);
        });

        loadLevels();

        return view;
    }

    /**
     * Загружает список уровней из Firestore и отображает их в виде кнопок.
     * Каждая кнопка ведет к редактированию соответствующего уровня.
     */
    private void loadLevels() {
        db.collection("sentenceLevels")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;
                    levelContainer.removeAllViews();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String levelId = document.getId();

                        Button levelButton = new Button(requireContext());
                        levelButton.setText("Level: " + levelId);
                        levelButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#363636")));
                        levelButton.setTextColor(android.graphics.Color.parseColor("#E0E0E0"));
                        levelButton.setTextSize(16);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 0, 0, 16);
                        levelButton.setLayoutParams(params);

                        levelButton.setOnClickListener(v -> {
                            Intent intent = new Intent(requireActivity(), LevelEditActivity.class);
                            intent.putExtra("LEVEL_ID", levelId);
                            startActivity(intent);
                        });

                        levelContainer.addView(levelButton);
                    }
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(requireContext(), "No levels found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Error loading levels: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Обновляет список уровней при возвращении фрагмента в активное состояние.
     */
    @Override
    public void onResume() {
        super.onResume();
        loadLevels();
    }
}