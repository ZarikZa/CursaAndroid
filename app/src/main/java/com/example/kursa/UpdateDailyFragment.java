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
/**
 * UpdateDailyFragment — фрагмент для отображения и обновления ежедневных слов.
 * Загружает текущие слова из Firestore, отображает их в RecyclerView и позволяет
 * обновлять список слов через парсер и селектор слов.
 */
public class UpdateDailyFragment extends Fragment {
    private static final String TAG = "UpdateDailyFragment";
    private FirebaseFirestore db;
    private Parser parser;
    private WordSelector wordSelector;
    private FirestoreHelper firestoreHelper;
    private RecyclerView learnedWordsList;
    private WordAdapter2 wordAdapter;
    private List<Word> wordList;

    /**
     * Инициализирует объекты Firestore, парсера, селектора и списка слов.
     *
     * @param savedInstanceState Сохраненное состояние фрагмента
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        parser = new Parser();
        wordSelector = new WordSelector();
        firestoreHelper = new FirestoreHelper();
        wordList = new ArrayList<>();
    }

    /**
     * Создает представление фрагмента, настраивает RecyclerView и кнопку обновления.
     *
     * @param inflater           Объект для раздувания layout
     * @param container          Родительский контейнер
     * @param savedInstanceState Сохраненное состояние фрагмента
     * @return                   Надутый View фрагмента
     */
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

    /**
     * Запускает процесс обновления слов и перезагружает список.
     */
    private void updateWordsAndRefresh() {
        firestoreHelper.setUpdateListener(success -> {
            requireActivity().runOnUiThread(() -> {
            });
        });
        firestoreHelper.checkAndUpdateData(parser, wordSelector);
    }

    /**
     * Загружает текущие ежедневные слова из Firestore и обновляет RecyclerView.
     */
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
                });
    }
}