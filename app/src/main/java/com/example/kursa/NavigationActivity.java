package com.example.kursa;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
/**
 * NavigationActivity — основная активность для навигации пользователя.
 * Использует BottomNavigationView для переключения между фрагментами:
 * обучением, уровнями, диалогами, рейтингом и профилем.
 * Передает никнейм пользователя каждому фрагменту через Bundle.
 */
public class NavigationActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private String nickname;

    /**
     * Инициализирует активность, получает никнейм пользователя из Intent,
     * настраивает BottomNavigationView и устанавливает начальный фрагмент.
     *
     * @param savedInstanceState Сохраненное состояние активности
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Intent intent = getIntent();
        nickname = intent.getStringExtra("USER_NICKNAME");

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setFragment(new LearnFragment());
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_learn) {
                setFragment(new LearnFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_levels) {
                setFragment(new LevelsFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_dictionary) {
                setFragment(new DialogueSelectionFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_ranking) {
                setFragment(new ReytingFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                setFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    /**
     * Устанавливает указанный фрагмент в контейнер, передавая никнейм пользователя.
     *
     * @param fragment Фрагмент для отображения
     */
    public void setFragment(Fragment fragment) {
        Bundle bundle = new Bundle();
        bundle.putString("USER_NICKNAME", nickname);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment, null)
                .commit();
    }
}