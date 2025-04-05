package com.example.kursa;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class LearnFragment extends Fragment {

    private TextView tvTimeRemaining;
    private CountDownTimer countDownTimer;
    private Button TenSlovBtm;
    private String userNickname;
    private List<Word> wordList;
    private Button VseSlovaBtm;
    private Button EzednevIspitBtm;
    private LinearLayout levelsContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userNickname = getArguments().getString("USER_NICKNAME");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frame_learn, container, false);
        tvTimeRemaining = view.findViewById(R.id.tvTimeRemaining);
        startDailyChallengeTimer();
        wordList = new ArrayList<>();

        levelsContainer = view.findViewById(R.id.levelsContainer);

        TenSlovBtm = view.findViewById(R.id.tenslovtest);
        TenSlovBtm.setOnClickListener(v -> {
            loadWordsFromFirebase(new OnWordsLoadedListener() {
                @Override
                public void onWordsLoaded(boolean isSuccess) {
                    if (isSuccess) {
                        List<Word> lastTenWords = getLastTenWords(wordList);
                        if (lastTenWords != null && !lastTenWords.isEmpty()) {
                            Level level = new Level("Тест на последние 10 слов", lastTenWords, true);
                            Intent intent = new Intent(getContext(), TestActivity.class);
                            intent.putExtra("level", level);
                            intent.putExtra("nickname", userNickname);
                            startActivity(intent);
                        } else {
                            Toast.makeText(requireActivity(), "Недостаточно слов для теста", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireActivity(), "Не удалось загрузить слова", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        VseSlovaBtm = view.findViewById(R.id.vseSlovaBtm);
        VseSlovaBtm.setOnClickListener(v -> {
            loadWordsFromFirebase(new OnWordsLoadedListener() {
                @Override
                public void onWordsLoaded(boolean isSuccess) {
                    if (isSuccess) {
                        Level level = new Level("Тест на все слова", wordList, true);
                        Intent intent = new Intent(getContext(), TestActivity.class);
                        intent.putExtra("level", level);
                        intent.putExtra("nickname", userNickname);
                        startActivity(intent);
                    } else {
                        Toast.makeText(requireActivity(), "Не удалось загрузить слова", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        EzednevIspitBtm = view.findViewById(R.id.ezednevIspitBtm);
        EzednevIspitBtm.setOnClickListener(v -> {
            loadDailyWordsFromFirebase(new OnWordsLoadedListener() {
                @Override
                public void onWordsLoaded(boolean isSuccess) {
                    if (isSuccess) {
                        Level level = new Level("Ежедневный тест", wordList, true);
                        Intent intent = new Intent(getContext(), TestActivity.class);
                        intent.putExtra("level", level);
                        intent.putExtra("nickname", userNickname);
                        startActivity(intent);
                    } else {
                        Toast.makeText(requireActivity(), "Не удалось загрузить слова", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        // Загрузка уровней
        loadLevels();

        return view;
    }

    private void loadLevels() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("sentenceLevels")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String levelId = document.getId();

                        // Создаём кнопку для уровня
                        Button levelButton = new Button(getContext());
                        levelButton.setText("Уровень: " + levelId);
                        levelButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#292828")));
                        levelButton.setTextColor(android.graphics.Color.parseColor("#A9A9A9"));
                        levelButton.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 0, 0, 8);
                        levelButton.setLayoutParams(params);

                        // Обработчик нажатия на кнопку уровня
                        levelButton.setOnClickListener(v -> {
                            Intent intent = new Intent(getContext(), SentenceBuilderActivity.class);
                            intent.putExtra("levelId", levelId);
                            intent.putExtra("nickname", userNickname);
                            startActivity(intent);
                        });

                        // Добавляем разделитель перед кнопкой (кроме первой)
                        if (levelsContainer.getChildCount() > 1) {
                            View divider = new View(getContext());
                            divider.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    1));
                            divider.setBackgroundColor(android.graphics.Color.parseColor("#A9A9A9"));
                            levelsContainer.addView(divider);
                        }

                        levelsContainer.addView(levelButton);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Ошибка загрузки уровней: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private List<Word> getLastTenWords(List<Word> wordList) {
        if (wordList == null || wordList.size() < 10) {
            Toast.makeText(requireActivity(), "Недостаточно слов для теста (нужно минимум 10)", Toast.LENGTH_SHORT).show();
            return wordList;
        }
        int startIndex = wordList.size() - 10;
        return new ArrayList<>(wordList.subList(startIndex, wordList.size()));
    }

    private void loadWordsFromFirebase(OnWordsLoadedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usersLearnedWords").document(userNickname)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> wordsMap = (Map<String, Object>) documentSnapshot.get("words");
                        if (wordsMap != null && !wordsMap.isEmpty()) {
                            wordList.clear();
                            for (Map.Entry<String, Object> entry : wordsMap.entrySet()) {
                                String englishWord = entry.getKey();
                                if (entry.getValue() instanceof Map) {
                                    Map<String, Object> wordData = (Map<String, Object>) entry.getValue();
                                    String translation = (String) wordData.get("translation");
                                    wordList.add(new Word(englishWord, translation));
                                } else if (entry.getValue() instanceof String) {
                                    String translation = (String) entry.getValue();
                                    wordList.add(new Word(englishWord, translation));
                                }
                            }
                            listener.onWordsLoaded(true);
                        } else {
                            Toast.makeText(requireActivity(), "Словарь пуст", Toast.LENGTH_SHORT).show();
                            listener.onWordsLoaded(false);
                        }
                    } else {
                        Toast.makeText(requireActivity(), "Документ пользователя не найден", Toast.LENGTH_SHORT).show();
                        listener.onWordsLoaded(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireActivity(), "Ошибка загрузки слов: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    listener.onWordsLoaded(false);
                });
    }

    private void loadDailyWordsFromFirebase(OnWordsLoadedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
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
                            listener.onWordsLoaded(true);
                        } else {
                            Toast.makeText(requireActivity(), "Словарь пуст", Toast.LENGTH_SHORT).show();
                            listener.onWordsLoaded(false);
                        }
                    } else {
                        Toast.makeText(requireActivity(), "Документ current не найден", Toast.LENGTH_SHORT).show();
                        listener.onWordsLoaded(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireActivity(), "Ошибка загрузки слов: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    listener.onWordsLoaded(false);
                });
    }

    private void startDailyChallengeTimer() {
        Calendar now = Calendar.getInstance();
        Calendar nextUpdateTime = Calendar.getInstance();
        nextUpdateTime.set(Calendar.HOUR_OF_DAY, 0);
        nextUpdateTime.set(Calendar.MINUTE, 0);
        nextUpdateTime.set(Calendar.SECOND, 0);
        nextUpdateTime.set(Calendar.MILLISECOND, 0);
        if (now.after(nextUpdateTime)) {
            nextUpdateTime.add(Calendar.DAY_OF_MONTH, 1);
        }
        long timeUntilNextUpdate = nextUpdateTime.getTimeInMillis() - now.getTimeInMillis();
        countDownTimer = new CountDownTimer(timeUntilNextUpdate, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long hours = millisUntilFinished / (60 * 60 * 1000);
                long minutes = (millisUntilFinished % (60 * 60 * 1000)) / (60 * 1000);
                long seconds = (millisUntilFinished % (60 * 1000)) / 1000;
                String timeRemaining = String.format("Обновиться через %02d:%02d:%02d", hours, minutes, seconds);
                tvTimeRemaining.setText(timeRemaining);
            }

            @Override
            public void onFinish() {
                tvTimeRemaining.setText("Обновление доступно!");
            }
        };
        countDownTimer.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    public interface OnWordsLoadedListener {
        void onWordsLoaded(boolean isSuccess);
    }
}