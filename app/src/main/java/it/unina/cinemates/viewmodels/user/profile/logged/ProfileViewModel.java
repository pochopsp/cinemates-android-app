package it.unina.cinemates.viewmodels.user.profile.logged;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import it.unina.cinemates.retrofit.PostDataInMutableLiveDataCallback;
import it.unina.cinemates.retrofit.backend.BackendRetrofitService;
import it.unina.cinemates.retrofit.tmdb.TmDbRetrofitService;
import it.unina.cinemates.views.backend.UserProfile;
import it.unina.cinemates.views.backend.enums.FollowRequestStatus;
import it.unina.cinemates.views.tmdb.MoviePoster;
import retrofit2.Call;
import retrofit2.Response;

public class ProfileViewModel extends ViewModel {

    private static final String TAG = "LOGGEDUSERPROFILE_VIEWMODEL";

    private static final UserProfile fakeUser = new UserProfile("","",0, 0, 0, FollowRequestStatus.NoRequest, new ArrayList<>());

    private final MutableLiveData<UserProfile> userProfile = new MutableLiveData<>(fakeUser);

    private final MutableLiveData<List<MoviePoster>> toWatchPostersPaths = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<MoviePoster>> favoritesPostersPaths = new MutableLiveData<>(new ArrayList<>());


    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }



    public void fetchUserProfile(String username) {
        PostDataInMutableLiveDataCallback<UserProfile> callback = new PostDataInMutableLiveDataCallback<UserProfile>(this.userProfile, TAG) {
            @Override
            public void onResponse(@NonNull Call<UserProfile> call, Response<UserProfile> response) {
                super.onResponse(call, response);
                if (response.code() == 200) {
                    assert response.body() != null;
                    if (!response.body().getMovieLists().get(0).getMoviesInList().isEmpty())
                        TmDbRetrofitService.fetchMoviePosters(response.body().getMovieLists().get(0).getMoviesInList(), toWatchPostersPaths);
                    if (!response.body().getMovieLists().get(1).getMoviesInList().isEmpty())
                        TmDbRetrofitService.fetchMoviePosters(response.body().getMovieLists().get(1).getMoviesInList(), favoritesPostersPaths);
                }
            }
        };
        BackendRetrofitService.getInstance().getUserAPI().getUser(username, username).enqueue(callback);
    }

    public LiveData<List<MoviePoster>> getToWatchPostersPaths() {
        return toWatchPostersPaths;
    }

    public LiveData<List<MoviePoster>> getFavoritesPostersPaths() {
        return favoritesPostersPaths;
    }


    public void resetAllData() {
        userProfile.postValue(fakeUser);
        toWatchPostersPaths.postValue(new ArrayList<>());
        favoritesPostersPaths.postValue(new ArrayList<>());
    }

}
