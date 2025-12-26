package com.example.circlebloom_branch.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.circlebloom_branch.models.User;
import com.example.circlebloom_branch.repositories.UserRepository;

import java.util.List;

public class UserViewModel extends ViewModel {

    private final UserRepository userRepository;

    public UserViewModel() {
        this.userRepository = UserRepository.getInstance();
    }

    public LiveData<User> getCurrentUser() {
        return userRepository.getCurrentUser();
    }

    public LiveData<List<User>> getOtherUsers() {
        return userRepository.getOtherUsers();
    }
}
