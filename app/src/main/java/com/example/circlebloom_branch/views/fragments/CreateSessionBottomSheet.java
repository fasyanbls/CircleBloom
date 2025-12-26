package com.example.circlebloom_branch.views.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.circlebloom_branch.databinding.DialogCreateSessionBinding;
import com.example.circlebloom_branch.models.Session;
import com.example.circlebloom_branch.viewmodels.SessionViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CreateSessionBottomSheet extends BottomSheetDialogFragment {

    private DialogCreateSessionBinding binding;
    private final Calendar selectedDateTime = Calendar.getInstance();
    private SessionViewModel viewModel;
    private Session sessionToEdit;

    public void setSessionToEdit(Session session) {
        this.sessionToEdit = session;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogCreateSessionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SessionViewModel.class);
        
        setupPickers();
        setupButton();
        
        if (sessionToEdit != null) {
            populateFields();
        }
    }

    private void populateFields() {
        binding.tvTitle.setText("Edit Study Session");
        binding.btnCreateSession.setText("Update Session");
        
        if (sessionToEdit.getSessionInfo() != null) {
            binding.etSessionTitle.setText(sessionToEdit.getSessionInfo().getTitle());
        }
        
        if (sessionToEdit.getLocation() != null) {
            binding.etLocation.setText(sessionToEdit.getLocation().getVenue());
        }
        
        binding.etQuota.setText(String.valueOf(sessionToEdit.getMaxParticipants()));
        
        // Parse date time if needed, for now assuming it's correctly set in calendar
        // In a real app, parse sessionToEdit.getSchedule().getDate() string back to Calendar
    }

    private void setupPickers() {
        binding.etDate.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        selectedDateTime.set(Calendar.YEAR, year);
                        selectedDateTime.set(Calendar.MONTH, month);
                        selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateLabel();
                    },
                    selectedDateTime.get(Calendar.YEAR),
                    selectedDateTime.get(Calendar.MONTH),
                    selectedDateTime.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        binding.etTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(requireContext(),
                    (view, hourOfDay, minute) -> {
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDateTime.set(Calendar.MINUTE, minute);
                        updateTimeLabel();
                    },
                    selectedDateTime.get(Calendar.HOUR_OF_DAY),
                    selectedDateTime.get(Calendar.MINUTE),
                    true);
            timePicker.show();
        });
        
        // Initial labels
        updateDateLabel();
        updateTimeLabel();
    }

    private void updateDateLabel() {
        String format = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        if (binding.etDate != null) binding.etDate.setText(sdf.format(selectedDateTime.getTime()));
    }

    private void updateTimeLabel() {
        String format = "HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        if (binding.etTime != null) binding.etTime.setText(sdf.format(selectedDateTime.getTime()));
    }

    private void setupButton() {
        binding.btnCreateSession.setOnClickListener(v -> {
            String title = binding.etSessionTitle.getText().toString();
            String locationText = binding.etLocation.getText().toString();
            String quotaStr = binding.etQuota.getText().toString();

            if (title.isEmpty()) {
                binding.etSessionTitle.setError("Title is required");
                return;
            }
            if (locationText.isEmpty()) {
                binding.etLocation.setError("Location is required");
                return;
            }
            if (quotaStr.isEmpty()) {
                binding.etQuota.setError("Quota is required");
                return;
            }

            int quota = Integer.parseInt(quotaStr);
            
            if (sessionToEdit != null) {
                // Update existing
                sessionToEdit.getSessionInfo().setTitle(title);
                sessionToEdit.getLocation().setVenue(locationText);
                sessionToEdit.setMaxParticipants(quota);
                sessionToEdit.getSchedule().setDate(binding.etDate.getText().toString());
                sessionToEdit.getSchedule().setStartTime(binding.etTime.getText().toString());
                
                viewModel.updateSession(sessionToEdit);
                Toast.makeText(getContext(), "Session updated", Toast.LENGTH_SHORT).show();
            } else {
                // Create new
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                        FirebaseAuth.getInstance().getCurrentUser().getUid() : "anonymous";

                Session session = new Session();
                session.getSessionInfo().setTitle(title);
                session.getSessionInfo().setCreatedBy(currentUserId);
                session.getSessionInfo().setStatus("Scheduled");
                session.getSessionInfo().setCreatedAt(com.google.firebase.Timestamp.now());
                
                session.getLocation().setVenue(locationText);
                
                session.getSchedule().setDate(binding.etDate.getText().toString());
                session.getSchedule().setStartTime(binding.etTime.getText().toString());
                
                session.setMaxParticipants(quota);
                
                Session.SessionParticipant host = new Session.SessionParticipant(currentUserId, "host");
                host.setRsvpStatus("accepted");
                session.getParticipants().add(host);

                viewModel.createSession(session);
                Toast.makeText(getContext(), "Session created", Toast.LENGTH_SHORT).show();
            }
            
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
