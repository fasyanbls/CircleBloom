package com.example.circlebloom_branch;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.circlebloom_branch.databinding.ActivityMainBinding;
//import com.example.circlebloom_branch.views.fragments.ChatListFragment;
//import com.example.circlebloom_branch.views.fragments.CommunityFragment;
import com.example.circlebloom_branch.views.fragments.AnalyticsFragment;
import com.example.circlebloom_branch.views.fragments.HomeFragment;
import com.example.circlebloom_branch.views.fragments.ProfileFragment;
import com.example.circlebloom_branch.views.fragments.StudyFragment; // Ini Fragment gabungan Match & Session

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup View Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            }
            else if (id == R.id.nav_study) {
                selectedFragment = new StudyFragment();
            }

            else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }
            else if (id == R.id.nav_analytics) {
                selectedFragment = new AnalyticsFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
