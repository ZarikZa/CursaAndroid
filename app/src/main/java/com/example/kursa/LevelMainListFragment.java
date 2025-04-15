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
 * LevelMainListFragment — фрагмент для отображения списка уровней слов.
 * Загружает уровни из коллекции Firestore "levelsAll", отображает их в виде кнопок,
 * позволяет перейти к редактированию существующего уровня или созданию нового.
 */
public class LevelMainListFragment extends Fragment {

    private LinearLayout levelContainer;
    private Button addLevelButton;
    private FirebaseFirestore db;

    /**
     * Инициализирует фрагмент, устанавливает layout, связывает элементы интерфейса,
     * настраивает обработчик для кнопки добавления уровня и загружает список уровней.
     *
     * @param inflater           Объект для раздувания layout
     * @param container          Родительский контейнер
     * @param savedInstanceState Сохраненное состояние фрагмента
     * @return                   Надутый View фрагмента
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_level_main_list, container, false);

        levelContainer = view.findViewById(R.id.levelContainer);
        addLevelButton = view.findViewById(R.id.addLevelButton);
        db = FirebaseFirestore.getInstance();

        addLevelButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), LevelMainAddActivity.class);
            startActivity(intent);
        });

        loadLevels();

        return view;
    }

    /**
     * Загружает уровни из Firestore и отображает их в виде кнопок.
     * Каждая кнопка ведет к редактированию соответствующего уровня.
     */
    private void loadLevels() {
        db.collection("levelsAll")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;
                    levelContainer.removeAllViews();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String levelId = document.getId();
                        String levelName = document.getString("levelName");

                        Button levelButton = new Button(requireContext());
                        levelButton.setText(levelName != null ? levelName : "Уровень: " + levelId);
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
                            Intent intent = new Intent(requireActivity(), LevelMainEditActivity.class);
                            intent.putExtra("LEVEL_ID", levelId);
                            startActivity(intent);
                        });

                        levelContainer.addView(levelButton);
                    }
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(requireContext(), "Уровни не найдены", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Ошибка загрузки уровней: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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