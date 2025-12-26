package com.example.circlebloom_branch.views.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.circlebloom_branch.databinding.FragmentAnalyticsBinding;
import com.example.circlebloom_branch.models.Analytics;
import com.example.circlebloom_branch.utils.Constants;
import com.example.circlebloom_branch.viewmodels.AnalyticsViewModel;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class AnalyticsFragment extends Fragment {

    private FragmentAnalyticsBinding binding;
    private AnalyticsViewModel analyticsViewModel;

    private DatabaseReference analyticsChartRef;
    private ValueEventListener analyticsChartListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAnalyticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        analyticsViewModel = new ViewModelProvider(requireActivity()).get(AnalyticsViewModel.class);
        setupObservers();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            setupChartListener(currentUser.getUid());
        }
    }

    private void setupObservers() {
        analyticsViewModel.getTotalSessions().observe(getViewLifecycleOwner(), sessions -> {
            if (binding != null && sessions != null) {
                binding.tvTotalSessions.setText(String.valueOf(sessions));
            }
        });

        analyticsViewModel.getStudyHours().observe(getViewLifecycleOwner(), hours -> {
            if (binding != null && hours != null) {
                binding.tvStudyHours.setText(hours);
            }
        });

        analyticsViewModel.getDayStreak().observe(getViewLifecycleOwner(), streak -> {
            if (binding != null && streak != null) {
                binding.tvDayStreak.setText(String.valueOf(streak));
            }
        });

        analyticsViewModel.getAvgMatchScore().observe(getViewLifecycleOwner(), score -> {
            if (binding != null && score != null) {
                binding.tvAvgMatchScore.setText(String.format(Locale.getDefault(), "%d%%", score));
            }
        });

        analyticsViewModel.getStudyPersona().observe(getViewLifecycleOwner(), persona -> {
            if (binding != null && persona != null) {
                binding.tvStudyPersona.setText(persona);
            }
        });

        analyticsViewModel.getBurnoutRisk().observe(getViewLifecycleOwner(), risk -> {
            if (binding != null && risk != null) {
                binding.tvBurnoutRisk.setText(risk);
            }
        });
    }

    private void setupChartListener(String userId) {
        analyticsChartRef = FirebaseDatabase.getInstance().getReference()
                .child(Constants.COLLECTION_ANALYTICS)
                .child(userId);

        analyticsChartListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Analytics analyticsData = snapshot.getValue(Analytics.class);
                    if (analyticsData != null && analyticsData.getPersonalAnalytics() != null) {
                        updateSkillProgressChart(analyticsData.getPersonalAnalytics().getSkillProgress());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load analytics chart: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void updateSkillProgressChart(Map<String, Analytics.SkillProgress> skillProgressMap) {
        if (binding == null || skillProgressMap == null || skillProgressMap.isEmpty()) {
            if (binding != null && binding.chartSkillProgress != null) {
                 binding.chartSkillProgress.clear();
                 binding.chartSkillProgress.invalidate();
            }
            return;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Analytics.SkillProgress> entry : skillProgressMap.entrySet()) {
            Analytics.SkillProgress skill = entry.getValue();
            entries.add(new BarEntry(i, skill.getCurrentLevel()));
            labels.add(skill.getSkillName());
            i++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Skill Level");
        dataSet.setColor(Color.rgb(123, 104, 238));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        binding.chartSkillProgress.setData(barData);

        XAxis xAxis = binding.chartSkillProgress.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labels.size());

        binding.chartSkillProgress.getDescription().setEnabled(false);
        binding.chartSkillProgress.animateY(1200);
        binding.chartSkillProgress.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (analyticsChartRef != null && analyticsChartListener != null) {
            analyticsChartRef.addValueEventListener(analyticsChartListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (analyticsChartRef != null && analyticsChartListener != null) {
            analyticsChartRef.removeEventListener(analyticsChartListener);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
