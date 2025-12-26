package com.example.circlebloom_branch.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.viewmodels.SessionViewModel;
import com.example.circlebloom_branch.views.adapters.StudyPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class StudyFragment extends Fragment {

    // Deklarasikan ViewPager2 di sini agar bisa diakses di helper method
    private ViewPager2 viewPager;

    public StudyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_study, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inisialisasi Views
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager); // Gunakan variabel yang sudah dideklarasikan

        // Pasang Adapter
        StudyPagerAdapter adapter = new StudyPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Hubungkan Tab dengan ViewPager (Kasih Judul Tab)
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Find Match");
            } else {
                tab.setText("My Sessions");
            }
        }).attach();

        // 1. Inisialisasi ViewModel CUKUP SEKALI
        SessionViewModel sessionViewModel = new ViewModelProvider(requireActivity()).get(SessionViewModel.class);

        // 2. Observer untuk pemicu manual (jika sudah di tab Study)
        sessionViewModel.getTriggerTabSwitch().observe(getViewLifecycleOwner(), trigger -> {
            // Saat dipicu, jalankan logika untuk mengganti tab
            handleTabSwitch(sessionViewModel);
        });

        // 3. Jalankan juga saat pertama kali fragment dibuat (jika pindah dari tab lain)
        handleTabSwitch(sessionViewModel);
    }

    // Buat metode helper baru untuk menghindari duplikasi kode
    private void handleTabSwitch(SessionViewModel viewModel) {
        String target = viewModel.getTargetSessionId().getValue();
        if (target != null && !target.isEmpty()) {
            if ("show_sessions_tab".equals(target)) {
                // Gunakan 'viewPager' yang sudah kita simpan, bukan 'binding.viewPager'
                viewPager.setCurrentItem(1, false); // pindah ke tab sessions
            } else if ("show_matches_tab".equals(target)) {
                // Gunakan 'viewPager' yang sudah kita simpan, bukan 'binding.viewPager'
                viewPager.setCurrentItem(0, false); // pindah ke tab matches
            }
            // Reset LiveData setelah digunakan
            viewModel.clearTargetSessionId();
        }
    }
}
