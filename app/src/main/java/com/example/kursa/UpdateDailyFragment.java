package com.example.kursa;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpdateDailyFragment extends Fragment {
    private static final String TAG = "UpdateDailyFragment";
    private FirebaseFirestore db;
    private Parser parser;
    private WordSelector wordSelector;
    private FirestoreHelper firestoreHelper;
    private RecyclerView learnedWordsList;
    private WordAdapter2 wordAdapter;
    private List<Word> wordList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        parser = new Parser();
        wordSelector = new WordSelector();
        firestoreHelper = new FirestoreHelper();
        wordList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frame_updatedaily, container, false);

        learnedWordsList = view.findViewById(R.id.learnedWordsList);
        learnedWordsList.setLayoutManager(new LinearLayoutManager(getContext()));
        wordAdapter = new WordAdapter2(wordList);
        learnedWordsList.setAdapter(wordAdapter);

        loadDailyWordsFromFirebase();

        Button updateButton = view.findViewById(R.id.updateButton);
        updateButton.setOnClickListener(v -> updateWordsAndRefresh());

        return view;
    }

    private void updateWordsAndRefresh() {
        firestoreHelper.setUpdateListener(success -> {
            requireActivity().runOnUiThread(() -> {
                if (success) {
                    loadDailyWordsFromFirebase();
                    Toast.makeText(getContext(), "Слова успешно обновлены", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Данные обновлены и список перезагружен");
                } else {
                    Toast.makeText(getContext(), "Ошибка при обновлении слов", Toast.LENGTH_SHORT).show();
                }
            });
        });
        firestoreHelper.checkAndUpdateData(parser, wordSelector);
    }

    private void loadDailyWordsFromFirebase() {
        db.collection("dailyWords")
                .document("current")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> wordsArray = (List<Map<String, Object>>) documentSnapshot.get("words");

                        if (wordsArray != null && !wordsArray.isEmpty()) {
                            wordList.clear();
                            for (Map<String, Object> wordMap : wordsArray) {
                                String englishWord = (String) wordMap.get("english");
                                String translation = (String) wordMap.get("translation");
                                wordList.add(new Word(englishWord, translation));
                            }
                            wordAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "Словарь пуст", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Документ current не найден", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Ошибка загрузки слов: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Ошибка загрузки слов", e);
                });
    }
}