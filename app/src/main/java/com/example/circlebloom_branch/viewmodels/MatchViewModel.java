package com.example.circlebloom_branch.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.circlebloom_branch.models.Match;
import com.example.circlebloom_branch.repositories.MatchRepository;
import java.util.List;

public class MatchViewModel extends AndroidViewModel {

    private final MatchRepository matchRepository;
    private LiveData<List<Match>> matchesLiveData;
    private final LiveData<String> errorLiveData;

    public MatchViewModel(@NonNull Application application) {
        super(application);
        matchRepository = new MatchRepository();
        errorLiveData = matchRepository.getErrors();
    }

    public LiveData<List<Match>> getMatchesForUser(String userId) {
        if (matchesLiveData == null) {
            matchesLiveData = matchRepository.getMatchesForUser(userId);
        }
        return matchesLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }
}
