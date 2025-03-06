package com.example.kursa;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationAdminActivity extends AppCompatActivity {
    BottomNavigationView botton;
    private String nickname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_navigation);

        Intent intent = getIntent();
        nickname = intent.getStringExtra("USER_NICKNAME");

        botton = findViewById(R.id.bottomNavigationViewAdmin);
        setFragment(new AddLevelFragment());
        botton.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_levels) {
                    setFragment(new AddLevelFragment());
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    setFragment(new ProfileAdminFragment());
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
