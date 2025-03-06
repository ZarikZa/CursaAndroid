package com.example.kursa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private String userNickname;

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

        wordList = new ArrayList<>();
        adapter = new WordAdapter(wordList);
        recyclerView.setAdapter(adapter);

        loadWordsFromFirebase();

        return view;
    }

    private void loadWordsFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("usersLearnedWords").document(userNickname)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, String> wordsMap = (Map<String, String>) documentSnapshot.get("words");

                        if (wordsMap != null && !wordsMap.isEmpty()) {
                            wordList.clear();

                            for (Map.Entry<String, String> entry : wordsMap.entrySet()) {
                                String englishWord = entry.getKey();
                                String translation = entry.getValue();

                                wordList.add(new Word(englishWord, translation));
                            }

                            adapter.notifyDataSetChanged();
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
}