package com.example.kursa;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * ReytingFragment — фрагмент для отображения рейтинга пользователей.
 * Загружает данные пользователей из Firestore, сортирует их по рейтинговым баллам
 * и отображает в RecyclerView с использованием ReytingAdapter.
 */
public class ReytingFragment extends Fragment {

    private RecyclerView recyclerView;
    private ReytingAdapter reytingAdapter;
    private List<User> userList;

    /**
     * Создает представление фрагмента, инициализирует RecyclerView
     * и загружает список пользователей.
     *
     * @param inflater           Объект для раздувания layout
     * @param container          Родительский контейнер
     * @param savedInstanceState Сохраненное состояние фрагмента
     * @return                   Надутый View фрагмента
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frame_reyting, container, false);

        recyclerView = view.findViewById(R.id.reytinglist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        reytingAdapter = new ReytingAdapter(userList);
        recyclerView.setAdapter(reytingAdapter);
        loadUsers();
        return view;
    }

    /**
     * Загружает данные пользователей из Firestore, сортирует их по рейтинговым баллам
     * и обновляет адаптер.
     */
    private void loadUsers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String nickname = documentSnapshot.getString("nickname");
                        String login = documentSnapshot.getString("login");
                        String password = documentSnapshot.getString("password");

                        Long reytingPointsLong = documentSnapshot.getLong("reytingPoints");
                        int reytingPoints = reytingPointsLong != null ? reytingPointsLong.intValue() : 0;

                        User user = new User(nickname, login, password);
                        user.setReytingPoints(reytingPoints);

                        userList.add(user);
                    }

                    Collections.sort(userList, (user1, user2) -> Integer.compare(user2.getReytingPoints(), user1.getReytingPoints()));

                    reytingAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireActivity(), "Ошибка загрузки пользователей: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}