package com.example.circlebloom_branch.views.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

// Import Fragment Match dan Session kamu yang LAMA
import com.example.circlebloom_branch.views.fragments.MatchFragment;
import com.example.circlebloom_branch.views.fragments.SessionFragment;

public class StudyPagerAdapter extends FragmentStateAdapter {

    public StudyPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Posisi 0 = Tab Kiri (Match)
        // Posisi 1 = Tab Kanan (Session)
        if (position == 0) {
            return new MatchFragment();
        } else {
            return new SessionFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Karena ada 2 tab
    }
}
