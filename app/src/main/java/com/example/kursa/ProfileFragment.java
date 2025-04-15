package com.example.kursa;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
/**
 * ProfileFragment — фрагмент для отображения профиля пользователя.
 * Показывает логин, никнейм, количество изученных слов, рейтинговые баллы и место в рейтинге.
 * Содержит график изученных слов за последние 7 дней, а также кнопки для выхода,
 * смены пароля и перехода к словарю.
 */
public class ProfileFragment extends Fragment {

    private TextView loginTextView, nicknameTextView, rankingTextView,
            wordsLearnedTextView, reytingPoint, noChartDataText;
    private LineChart wordCountChart;
    private FirebaseFirestore db;
    private Button logoutButton, changePasswordButton, slovarBtm;
    private String login, nickname;

    /**
     * Создает представление фрагмента, инициализирует элементы интерфейса,
     * настраивает график и обработчики событий.
     *
     * @param inflater           Объект для раздувания layout
     * @param container          Родительский контейнер
     * @param savedInstanceState Сохраненное состояние фрагмента
     * @return                   Надутый View фрагмента
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frame_profile, container, false);

        loginTextView = view.findViewById(R.id.loginTextView);
        nicknameTextView = view.findViewById(R.id.nicknameTextView);
        rankingTextView = view.findViewById(R.id.rankingTextView);
        wordsLearnedTextView = view.findViewById(R.id.wordsLearnedTextView);
        reytingPoint = view.findViewById(R.id.reytingPointTV);
        wordCountChart = view.findViewById(R.id.wordCountChart);
        noChartDataText = view.findViewById(R.id.noChartDataText);
        logoutButton = view.findViewById(R.id.logoutButton);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        slovarBtm = view.findViewById(R.id.slovarBtm);
        db = FirebaseFirestore.getInstance();

        setupChart();

        Bundle bundle = getArguments();
        if (bundle != null) {
            nickname = bundle.getString("USER_NICKNAME", "Default Nickname");
            nicknameTextView.setText("Никнейм: " + nickname);
            fetchUserData(nickname);
            fetchDailyWordCount(nickname, 7);
        }

        slovarBtm.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SlovarActivity.class);
            intent.putExtra("USER_NICKNAME", nickname);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> showLogoutConfirmationDialog());

        changePasswordButton.setOnClickListener(v -> {
            if (login != null) {
                Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
                intent.putExtra("login", login);
                startActivity(intent);
            }
        });

        return view;
    }

    /**
     * Настраивает параметры графика изученных слов.
     */
    private void setupChart() {
        wordCountChart.getDescription().setEnabled(false);
        wordCountChart.setTouchEnabled(true);
        wordCountChart.setDragEnabled(true);
        wordCountChart.setScaleEnabled(true);
        wordCountChart.setPinchZoom(true);
        wordCountChart.getAxisRight().setEnabled(false);
        wordCountChart.getLegend().setTextColor(Color.WHITE);
        wordCountChart.getXAxis().setTextColor(Color.WHITE);
        wordCountChart.getAxisLeft().setTextColor(Color.WHITE);

        showNoDataMessage("Здесь будет график изученных слов");
    }

    /**
     * Загружает данные о количестве изученных слов за указанный период.
     *
     * @param nickname Никнейм пользователя
     * @param days     Количество дней для анализа
     */
    private void fetchDailyWordCount(String nickname, int days) {
        String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        long startTime = System.currentTimeMillis() - (days - 1) * 24 * 60 * 60 * 1000;
        String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(startTime));

        db.collection("dailyWordCount")
                .document(nickname)
                .collection("wordCount")
                .whereGreaterThanOrEqualTo(FieldPath.documentId(), startDate)
                .whereLessThanOrEqualTo(FieldPath.documentId(), endDate)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        showNoDataMessage("Нет данных для построения графика");
                        return;
                    }

                    Map<String, Integer> wordCountMap = new HashMap<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Long count = doc.getLong("count");
                        if (count != null) {
                            wordCountMap.put(doc.getId(), count.intValue());
                        }
                    }

                    if (wordCountMap.isEmpty()) {
                        showNoDataMessage("Нет данных для построения графика");
                    } else {
                        displayChart(wordCountMap);
                    }
                })
                .addOnFailureListener(e -> {
                    showNoDataMessage("Ошибка загрузки данных");
                });
    }

    /**
     * Отображает график на основе данных о словах.
     *
     * @param wordCountMap Карта с датами и количеством слов
     */
    private void displayChart(Map<String, Integer> wordCountMap) {
        List<Entry> entries = new ArrayList<>();
        List<String> dates = new ArrayList<>(wordCountMap.keySet());
        Collections.sort(dates);

        for (int i = 0; i < dates.size(); i++) {
            entries.add(new Entry(i, wordCountMap.get(dates.get(i))));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Изученные слова");
        dataSet.setColor(Color.parseColor("#E0E0E0"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setLineWidth(2f);
        dataSet.setValueTextSize(12f);
        dataSet.setCircleColor(Color.WHITE);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);

        LineData lineData = new LineData(dataSet);
        wordCountChart.setData(lineData);
        wordCountChart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(dates));
        wordCountChart.invalidate();

        wordCountChart.setVisibility(View.VISIBLE);
        noChartDataText.setVisibility(View.GONE);
    }

    /**
     * Показывает сообщение, если данных для графика нет.
     *
     * @param message Текст сообщения
     */
    private void showNoDataMessage(String message) {
        noChartDataText.setText(message);
        noChartDataText.setVisibility(View.VISIBLE);
        wordCountChart.setVisibility(View.GONE);
    }

    /**
     * Показывает диалог подтверждения выхода из аккаунта.
     */
    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Подтверждение выхода")
                .setMessage("Вы уверены, что хотите выйти из аккаунта?")
                .setPositiveButton("Да", (dialog, which) -> performLogout())
                .setNegativeButton("Отмена", null)
                .show();
    }

    /**
     * Выполняет выход из аккаунта, очищает SharedPreferences и перенаправляет
     * на экран авторизации.
     */
    private void performLogout() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    /**
     * Загружает данные пользователя из Firestore по никнейму.
     *
     * @param nickname Никнейм пользователя
     */
    private void fetchUserData(String nickname) {
        db.collection("users")
                .whereEqualTo("nickname", nickname)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        login = document.getString("login");
                        String userId = document.getId();
                        long reytingPoints = document.getLong("reytingPoints");

                        if (login != null) {
                            loginTextView.setText("Логин: " + login);
                        }

                        fetchWordsLearnedData(nickname);
                        fetchRanking(userId, reytingPoints);
                    }
                })
                .addOnFailureListener(e -> {});
    }

    /**
     * Загружает данные об изученных словах пользователя.
     *
     * @param nickname Никнейм пользователя
     */
    private void fetchWordsLearnedData(String nickname) {
        db.collection("usersLearnedWords")
                .document(nickname)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> words = (Map<String, Object>) documentSnapshot.get("words");

                        if (words != null) {
                            int wordsLearned = words.size();
                            wordsLearnedTextView.setText("Изучено слов: " + wordsLearned);
                        }
                    }
                })
                .addOnFailureListener(e -> {});
    }

    /**
     * Определяет место пользователя в рейтинге на основе рейтинговых баллов.
     *
     * @param userId           ID пользователя
     * @param userReytingPoints Рейтинговые баллы пользователя
     */
    private void fetchRanking(String userId, long userReytingPoints) {
        db.collection("users")
                .orderBy("reytingPoints", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        int rank = 1;
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            if (document.getId().equals(userId)) {
                                rankingTextView.setText("Место в рейтинге: " + rank);
                                reytingPoint.setText("Количество рейтинговых баллов: " + userReytingPoints);
                                break;
                            }
                            rank++;
                        }
                    }
                })
                .addOnFailureListener(e -> {});
    }
}