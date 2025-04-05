package com.example.kursa;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LevelsFragment extends Fragment {
    private static final String TAG = "LevelsFragment";
    private static final int LEVEL_ACTIVITY_REQUEST_CODE = 1;

    private RecyclerView recyclerView;
    private LevelsAdapter levelsAdapter;
    private List<Level> levels = new ArrayList<>();
    private FirebaseFirestore db;
    private String userNickname;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frame_levels, container, false);
        initializeViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            userNickname = getArguments().getString("USER_NICKNAME");
        }
        setupRecyclerView();
        loadLevels();
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.levels_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        db = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView() {
        levelsAdapter = new LevelsAdapter(levels, level -> {
            Intent intent = new Intent(getContext(), LevelActivActivity.class);
            intent.putExtra("level", level);
            intent.putExtra("nickname", userNickname);
            startActivityForResult(intent, LEVEL_ACTIVITY_REQUEST_CODE);
        });
        recyclerView.setAdapter(levelsAdapter);
    }

    private void loadLevels() {
        if (userNickname == null || !isAdded()) return;

        db.collection("levels")
                .document(userNickname)
                .get()
                .addOnSuccessListener(this::processLevelsData)
                .addOnFailureListener(this::handleLoadError);
    }

    private void processLevelsData(DocumentSnapshot documentSnapshot) {
        if (!documentSnapshot.exists()) {
            showToast("No data found for the user");
            return;
        }

        levels.clear();
        Map<String, Object> userData = documentSnapshot.getData();
        if (userData == null) return;

        List<Map<String, Object>> levelsData = (List<Map<String, Object>>) userData.get("levels");
        if (levelsData == null) return;

        for (Map<String, Object> levelData : levelsData) {
            try {
                Level level = createLevelFromData(levelData);
                if (level != null) {
                    levels.add(level);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing level data", e);
            }
        }

        levelsAdapter.notifyDataSetChanged();
    }

    private Level createLevelFromData(Map<String, Object> levelData) {
        String levelName = (String) levelData.get("levelName");
        Map<String, Object> levelDetails = (Map<String, Object>) levelData.get("details");

        if (levelName == null || levelDetails == null) return null;

        boolean isUnlocked = Boolean.TRUE.equals(levelDetails.get("isUnlocked"));
        Map<String, String> wordsMap = (Map<String, String>) levelDetails.get("words");
        List<Word> words = new ArrayList<>();

        if (wordsMap != null) {
            for (Map.Entry<String, String> entry : wordsMap.entrySet()) {
                words.add(new Word(entry.getKey(), entry.getValue()));
            }
        }

        return new Level(levelName, words, isUnlocked);
    }

    private void handleLoadError(Exception e) {
        showToast("Error loading levels");
        Log.e(TAG, "Error loading levels", e);
    }

    private void showToast(String message) {
        if (isAdded()) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LEVEL_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Log.d(TAG, "Level completed, refreshing levels list");
            loadLevels();
        }
    }
}