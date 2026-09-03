package com.example.taranood;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.taranood.fragments.FavoritesFragment;
import com.example.taranood.fragments.HistoryFragment;
import com.example.taranood.fragments.HomeFragment;
import com.example.taranood.fragments.SettingsFragment;
import com.example.taranood.utils.StorageHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply dark mode before super.onCreate
        if (StorageHelper.isDarkMode(this)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_history) {
                selectedFragment = new HistoryFragment();
            } else if (id == R.id.nav_favorites) {
                selectedFragment = new FavoritesFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new SettingsFragment();
            }

            if (selectedFragment != null) {
                setBottomNavigationVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    public void setBottomNavigationVisibility(int visibility) {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setVisibility(visibility);
        }
    }
}
