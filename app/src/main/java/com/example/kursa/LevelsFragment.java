package com.example.kursa;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class LevelsFragment extends Fragment {

    private RecyclerView recyclerView;
    private LevelsAdapter levelsAdapter;
    private List<Level> levels;
    private FirebaseFirestore db;
    private String userNickname;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frame_levels, container, false);

        // Ensure that userNickname is passed as an argument to this fragment
        if (getArguments() != null) {
            userNickname = getArguments().getString("USER_NICKNAME");
        }

        if (userNickname == null) {
            Toast.makeText(getContext(), "User nickname is missing", Toast.LENGTH_SHORT).show();
            return view;  // Exit early if userNickname is not provided
        }

        recyclerView = view.findViewById(R.id.levels_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        levels = new ArrayList<>();
        levelsAdapter = new LevelsAdapter(levels, level -> {
            Intent intent = new Intent(getContext(), LevelActivActivity.class);
            intent.putExtra("level", level);
            intent.putExtra("nickname", userNickname);
            startActivityForResult(intent, 1);
            loadLevels();
        });
        recyclerView.setAdapter(levelsAdapter);

        db = FirebaseFirestore.getInstance();
        loadLevels();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            loadLevels();
        }
    }

    private void loadLevels() {
        if (userNickname == null) {
            Toast.makeText(getContext(), "User nickname is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("levels")
                .document(userNickname)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> userData = (Map<String, Object>) documentSnapshot.getData();
                        List<Map<String, Object>> levelsData = (List<Map<String, Object>>) userData.get("levels");

                        if (levelsData != null) {
                            levels.clear();

                            for (Map<String, Object> levelData : levelsData) {
                                String levelName = (String) levelData.get("levelName");
                                Map<String, Object> levelDetails = (Map<String, Object>) levelData.get("details");

                                boolean isUnlocked = (boolean) levelDetails.get("isUnlocked");
                                Map<String, String> wordsMap = (Map<String, String>) levelDetails.get("words");
                                List<Word> words = new ArrayList<>();

                                if (wordsMap != null) {
                                    for (Map.Entry<String, String> entry : wordsMap.entrySet()) {
                                        words.add(new Word(entry.getKey(), entry.getValue()));
                                    }
                                }

                                Level level = new Level(levelName, words, isUnlocked);
                                levels.add(level);
                            }

                            levelsAdapter.notifyDataSetChanged();  // Notify adapter to update the view
                        }
                    } else {
                        Toast.makeText(getContext(), "No data found for the user", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading levels: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}