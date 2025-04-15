
package com.example.kursa;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
/**
 * NavigationAdminActivity — основная активность для администратора.
 * Использует BottomNavigationView для переключения между фрагментами:
 * списком уровней, профилем, ежедневными обновлениями, диалогами и предложениями.
 * Передает никнейм администратора каждому фрагменту через Bundle.
 */
public class NavigationAdminActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private String nickname;

    /**
     * Инициализирует активность, получает никнейм администратора из Intent,
     * настраивает BottomNavigationView и устанавливает начальный фрагмент.
     *
     * @param savedInstanceState Сохраненное состояние активности
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_navigation);

        Intent intent = getIntent();
        nickname = intent.getStringExtra("USER_NICKNAME");

        bottomNavigationView = findViewById(R.id.bottomNavigationViewAdmin);
        setFragment(new LevelMainListFragment());
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_levels) {
                setFragment(new LevelMainListFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                setFragment(new ProfileAdminFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_update) {
                setFragment(new UpdateDailyFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_dialoge) {
                setFragment(new DialogueListFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_predloj) {
                setFragment(new LevelListFragment());
                return true;
            }
            return false;
        });
    }

    /**
     * Устанавливает указанный фрагмент в контейнер, передавая никнейм администратора.
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