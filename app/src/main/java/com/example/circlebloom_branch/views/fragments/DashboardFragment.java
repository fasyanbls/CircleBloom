package com.example.circlebloom_branch.views.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.databinding.FragmentDashboardBinding;
import com.example.circlebloom_branch.viewmodels.AnalyticsViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private AnalyticsViewModel analyticsViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        analyticsViewModel = new ViewModelProvider(requireActivity()).get(AnalyticsViewModel.class);
        setupObservers();
    }

    private void setupObservers() {
        analyticsViewModel.getTotalSessions().observe(getViewLifecycleOwner(), sessions -> {
            if (binding != null && sessions != null) binding.tvTotalSessions.setText(String.valueOf(sessions));
        });
        analyticsViewModel.getStudyHours().observe(getViewLifecycleOwner(), hours -> {
            if (binding != null && hours != null) binding.tvTotalHours.setText(hours);
        });
        analyticsViewModel.getDayStreak().observe(getViewLifecycleOwner(), streak -> {
            if (binding != null && streak != null) binding.tvCurrentStreak.setText(String.valueOf(streak));
        });
        analyticsViewModel.getStudyPersona().observe(getViewLifecycleOwner(), persona -> {
            if (binding != null && persona != null) binding.tvStudyPersona.setText(persona);
        });
        analyticsViewModel.getBurnoutRisk().observe(getViewLifecycleOwner(), risk -> {
            if (binding != null && risk != null) binding.tvBurnoutRisk.setText(risk);
        });
        analyticsViewModel.getStudyHoursChartData().observe(getViewLifecycleOwner(), this::setupStudyHoursChart);
        analyticsViewModel.getSkillProgressData().observe(getViewLifecycleOwner(), this::setupSkillProgressChart);
        analyticsViewModel.getLevelProgressData().observe(getViewLifecycleOwner(), this::setupLevelProgressChart);

        analyticsViewModel.getAvgMatchScore().observe(getViewLifecycleOwner(), score -> {
            if (binding != null && score != null && binding.tvAverageScore != null) {
                binding.tvAverageScore.setText(String.format(Locale.getDefault(), "%d%%", score));
            }
        });
    }

    private void setupLevelProgressChart(Map<String, Number> levelData) {
        if (binding == null || levelData == null || levelData.isEmpty() || getContext() == null) return;
        PieChart chart = binding.chartCourseDistribution;

        int currentLevel = levelData.get("currentLevel").intValue();
        int sessionsInCurrentLevel = levelData.get("sessionsInCurrentLevel").intValue();
        int sessionsPerLevel = levelData.get("sessionsPerLevel").intValue();

        float progress = (float) sessionsInCurrentLevel;
        float remaining = (float) sessionsPerLevel - progress;

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(progress, ""));
        entries.add(new PieEntry(remaining, ""));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ContextCompat.getColor(getContext(), R.color.pastel_purple), Color.LTGRAY);
        dataSet.setDrawValues(false);

        PieData pieData = new PieData(dataSet);
        chart.setData(pieData);

        chart.setHoleRadius(85f);
        chart.setTransparentCircleRadius(85f);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setDrawEntryLabels(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        String levelText = "Level " + currentLevel;
        String progressText = sessionsInCurrentLevel + "/" + sessionsPerLevel;
        SpannableString centerText = new SpannableString(levelText + "\n" + progressText);
        centerText.setSpan(new RelativeSizeSpan(1.6f), 0, levelText.length(), 0);
        centerText.setSpan(new RelativeSizeSpan(1.1f), levelText.length(), centerText.length(), 0);
        chart.setCenterText(centerText);

        chart.animateY(1000);
        chart.invalidate();
    }

    private void setupStudyHoursChart(List<Entry> entries) {
        if (binding == null || entries == null || getContext() == null) return;

        LineChart chart = binding.chartStudyHours;

        if (entries.isEmpty()) {
            chart.clear();
            chart.setNoDataText("No study data for the last 7 days.");
            chart.invalidate();
            return;
        }

        // --- X-Axis Date Labels ---
        final ArrayList<String> dateLabels = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -6); // Start from 6 days ago
        SimpleDateFormat sdf = new SimpleDateFormat("d/M", Locale.getDefault());
        for (int i = 0; i < 7; i++) {
            dateLabels.add(sdf.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        // --- End Date Labels ---

        LineDataSet dataSet = new LineDataSet(entries, "Study Hours");
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.pastel_purple));
        dataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.pastel_pink));
        dataSet.setFillColor(ContextCompat.getColor(getContext(), R.color.pastel_purple_light));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawFilled(true);
        dataSet.setDrawValues(false);

        // Y-Axis setup (Vertical) - In Minutes
        YAxis axisLeft = chart.getAxisLeft();
        axisLeft.setAxisMinimum(0f);
        axisLeft.setLabelCount(5, true); // Force 5 labels, making it adaptive
        axisLeft.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int minutes = (int) (value * 60);
                return minutes + "m";
            }
        });
        chart.getAxisRight().setEnabled(false);

        // X-Axis setup (Horizontal) - In Dates
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int index = (int) value;
                if (index >= 0 && index < dateLabels.size()) {
                    return dateLabels.get(index);
                }
                return "";
            }
        });
        xAxis.setLabelCount(7, true);

        chart.setData(new LineData(dataSet));
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.animateY(1000);
        chart.invalidate();
    }

    private void setupSkillProgressChart(Map<String, Float> progressData) {
        if (binding == null || progressData == null || progressData.isEmpty() || getContext() == null) return;
        BarChart chart = binding.chartSkillProgress;
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Float> entry : progressData.entrySet()) {
            entries.add(new BarEntry(i++, entry.getValue()));
            labels.add(entry.getKey());
        }
        BarDataSet dataSet = new BarDataSet(entries, "Skill Progress");
        final int[] PASTEL_COLORS = {
                ContextCompat.getColor(getContext(), R.color.pastel_pink),
                ContextCompat.getColor(getContext(), R.color.pastel_purple_light),
                ContextCompat.getColor(getContext(), R.color.pastel_blue),
                ContextCompat.getColor(getContext(), R.color.pastel_lavender)
        };
        dataSet.setColors(PASTEL_COLORS);
        dataSet.setValueTextSize(12f);
        BarData barData = new BarData(dataSet);
        chart.setData(barData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labels.size());
        chart.getAxisRight().setEnabled(false);
        chart.animateY(1000);
        chart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
