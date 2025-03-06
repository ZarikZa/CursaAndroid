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

public class ReytingFragment extends Fragment {

    private RecyclerView recyclerView;
    private ReytingAdapter ReytingAdapter;
    private List<User> userList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frame_reyting, container, false);

        recyclerView = view.findViewById(R.id.reytinglist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        ReytingAdapter = new ReytingAdapter(userList);
        recyclerView.setAdapter(ReytingAdapter);
        loadUsers();
        return view;
    }

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

                    ReytingAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireActivity(), "Ошибка загрузки пользователей: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}