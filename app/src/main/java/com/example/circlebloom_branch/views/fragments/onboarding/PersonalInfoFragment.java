package com.example.circlebloom_branch.views.fragments.onboarding;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.circlebloom_branch.R;
import com.example.circlebloom_branch.databinding.FragmentPersonalInfoBinding;
import com.example.circlebloom_branch.models.User;
import com.example.circlebloom_branch.views.activities.OnboardingActivity;
import com.google.firebase.Timestamp;

public class PersonalInfoFragment extends Fragment {

    private FragmentPersonalInfoBinding binding;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPersonalInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cek null untuk mencegah crash jika activity tidak ditemukan
        if (getActivity() instanceof OnboardingActivity) {
            currentUser = ((OnboardingActivity) getActivity()).getCurrentUser();
        }

        // Metode setupSpinners() tidak diperlukan lagi, jadi kita hapus panggilannya
        // setupSpinners();

        loadExistingData();
    }

    // Metode ini sudah tidak relevan karena spinner sudah diganti menjadi EditText
    // private void setupSpinners() { ... }

    private void loadExistingData() {
        // Pastikan currentUser dan personalInfo tidak null sebelum diakses
        if (currentUser != null && currentUser.getPersonalInfo() != null) {
            User.PersonalInfo info = currentUser.getPersonalInfo();

            // Mengisi data yang sudah ada ke field yang benar
            if (info.getFullName() != null) binding.etFullName.setText(info.getFullName());
            if (info.getEmail() != null) binding.etEmail.setText(info.getEmail());
            if (info.getUniversity() != null) binding.etUniversity.setText(info.getUniversity());
            if (info.getMajor() != null) binding.etMajor.setText(info.getMajor());
            if (info.getSemester() > 0) binding.etSemester.setText(String.valueOf(info.getSemester()));
            if (info.getGpa() > 0.0) binding.etGpa.setText(String.valueOf(info.getGpa()));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Simpan data hanya jika currentUser tidak null
        if (currentUser != null) {
            saveData();
        }
    }

    private void saveData() {
        User.PersonalInfo info = currentUser.getPersonalInfo();
        if (info == null) {
            // Inisialisasi jika belum ada
            info = new User.PersonalInfo();
            currentUser.setPersonalInfo(info);
        }

        // Mengambil data dari EditText yang sesuai
        info.setFullName(binding.etFullName.getText().toString().trim());
        info.setEmail(binding.etEmail.getText().toString().trim());
        info.setUniversity(binding.etUniversity.getText().toString().trim());
        info.setMajor(binding.etMajor.getText().toString().trim());

        // Menghapus `setCampus` karena field sudah dihapus
        // info.setCampus(...);

        // Try-catch untuk Semester
        String semesterStr = binding.etSemester.getText().toString();
        try {
            info.setSemester(TextUtils.isEmpty(semesterStr) ? 0 : Integer.parseInt(semesterStr));
        } catch (NumberFormatException e) {
            info.setSemester(0); // Default value jika input tidak valid
        }

        // Try-catch untuk GPA
        String gpaStr = binding.etGpa.getText().toString();
        try {
            info.setGpa(TextUtils.isEmpty(gpaStr) ? 0.0 : Double.parseDouble(gpaStr));
        } catch (NumberFormatException e) {
            info.setGpa(0.0); // Default value jika input tidak valid
        }

        // Hanya set joinDate jika belum pernah di-set sebelumnya
        if (info.getJoinDate() == null) {
            info.setJoinDate(Timestamp.now());
        }
        info.setLastActive(Timestamp.now());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
