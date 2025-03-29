package com.example.kursa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SlovarFragment extends Fragment {
    private RecyclerView recyclerView;
    private WordAdapter adapter;
    private List<Word> wordList;
    private List<Word> allWordsList;
    private List<Word> hardWordsList;
    private String userNickname;
    private Button btnAllWords, btnHardWords;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            userNickname = getArguments().getString("USER_NICKNAME");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frame_slorar, container, false);

        recyclerView = view.findViewById(R.id.learnedWordsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnAllWords = view.findViewById(R.id.btnAllWords);
        btnHardWords = view.findViewById(R.id.btnHardWords);

        wordList = new ArrayList<>();
        allWordsList = new ArrayList<>();
        hardWordsList = new ArrayList<>();

        adapter = new WordAdapter(wordList);
        recyclerView.setAdapter(adapter);

        btnAllWords.setOnClickListener(v -> showAllWords());
        btnHardWords.setOnClickListener(v -> showHardWords());

        loadWordsFromFirebase();

        return view;
    }

    private void loadWordsFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("usersLearnedWords").document(userNickname)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> wordsMap = (Map<String, Object>) documentSnapshot.get("words");

                        if (wordsMap != null && !wordsMap.isEmpty()) {
                            allWordsList.clear();
                            hardWordsList.clear();

                            for (Map.Entry<String, Object> entry : wordsMap.entrySet()) {
                                String englishWord = entry.getKey();

                                if (entry.getValue() instanceof String) {
                                    String translation = (String) entry.getValue();
                                    allWordsList.add(new Word(englishWord, translation));
                                } else if (entry.getValue() instanceof Map) {
                                    Map<String, Object> wordData = (Map<String, Object>) entry.getValue();
                                    String translation = (String) wordData.get("translation");
                                    Boolean isHard = (Boolean) wordData.get("hard");

                                    Word word = new Word(englishWord, translation);
                                    allWordsList.add(word);

                                    if (isHard != null && isHard) {
                                        hardWordsList.add(word);
                                    }
                                }
                            }

                            showAllWords();
                        } else {
                            Toast.makeText(requireActivity(), "Словарь пуст", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireActivity(), "Документ пользователя не найден", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireActivity(), "Ошибка загрузки слов: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showAllWords() {
        wordList.clear();
        wordList.addAll(allWordsList);
        adapter.notifyDataSetChanged();

        btnAllWords.setBackgroundTintList(getResources().getColorStateList(R.color.selected_button_color));
        btnHardWords.setBackgroundTintList(getResources().getColorStateList(R.color.unselected_button_color));
    }

    private void showHardWords() {
        wordList.clear();
        wordList.addAll(hardWordsList);
        adapter.notifyDataSetChanged();

        btnAllWords.setBackgroundTintList(getResources().getColorStateList(R.color.unselected_button_color));
        btnHardWords.setBackgroundTintList(getResources().getColorStateList(R.color.selected_button_color));
    }
}