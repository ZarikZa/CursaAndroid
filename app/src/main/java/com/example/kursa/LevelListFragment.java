package com.example.kursa;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LevelListActivity extends AppCompatActivity {

    private LinearLayout levelContainer;
    private Button addLevelButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_list);

        levelContainer = findViewById(R.id.levelContainer);
        addLevelButton = findViewById(R.id.addLevelButton);
        db = FirebaseFirestore.getInstance();

        addLevelButton.setOnClickListener(v -> {
            Intent intent = new Intent(LevelListActivity.this, LevelAddActivity.class);
            startActivity(intent);
        });

        loadLevels();
    }

    private void loadLevels() {
        db.collection("sentenceLevels")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    levelContainer.removeAllViews(); // Очищаем контейнер перед загрузкой
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String levelId = document.getId();

                        Button levelButton = new Button(this);
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
                            Intent intent = new Intent(LevelListActivity.this, LevelEditActivity.class);
                            intent.putExtra("LEVEL_ID", levelId);
                            startActivity(intent);
                        });

                        levelContainer.addView(levelButton);
                    }
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "No levels found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading levels: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLevels(); // Обновляем список уровней при возвращении
    }
}