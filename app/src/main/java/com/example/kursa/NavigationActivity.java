package com.example.kursa;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationActivity extends AppCompatActivity {
    BottomNavigationView botton;
    private String nickname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Intent intent = getIntent();
        nickname = intent.getStringExtra("USER_NICKNAME");

        botton = findViewById(R.id.bottomNavigationView);
        setFragment(new LearnFragment());
        botton.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_learn) {
                    setFragment(new LearnFragment());
                    return true;
                } else if (item.getItemId() == R.id.nav_levels) {
                    setFragment(new LevelsFragment());
                    return true;
                } else if (item.getItemId() == R.id.nav_dictionary) {
                    setFragment(new SlovarFragment());
                    return true;
                } else if (item.getItemId() == R.id.nav_ranking) {
                    setFragment(new ReytingFragment());
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    setFragment(new ProfileFragment());
                    return true;
                }
                return false;
            }
        });
    }

    public void setFragment(Fragment fragment) {
        Bundle bundle = new Bundle();
        bundle.putString("USER_NICKNAME", nickname);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment, null).commit();
    }
}
