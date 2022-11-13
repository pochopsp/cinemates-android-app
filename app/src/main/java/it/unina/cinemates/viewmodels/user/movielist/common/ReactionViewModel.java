package it.unina.cinemates.viewmodels.user.movielist.common;

import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;

import java.util.ArrayList;
import java.util.List;

import it.unina.cinemates.R;
import it.unina.cinemates.models.Notification;
import it.unina.cinemates.retrofit.PaginatedResponse;
import it.unina.cinemates.retrofit.backend.BackendRetrofitService;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.views.backend.MovieListBasic;
import it.unina.cinemates.models.Reaction;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReactionViewModel extends ViewModel {

    private List<Reaction> reactions = new ArrayList<>();
    private final MutableLiveData<List<Reaction>> movieListReactionsLiveData = new MutableLiveData<>();

    public LiveData<List<Reaction>> getMovieListReactionsLiveData() {
        return movieListReactionsLiveData;
    }

    public void setMovieListReactionsLiveData(List<Reaction> reactions) {
        this.movieListReactionsLiveData.setValue(reactions);
    }

    private final MutableLiveData<Integer> movieListTotalReactionsLiveData = new MutableLiveData<>();

    public LiveData<Integer> getMovieListTotalReactionsLiveData() {
        return movieListTotalReactionsLiveData;
    }

    public void setMovieListTotalReactionsLiveData(Integer totalReactions) {
        this.movieListTotalReactionsLiveData.setValue(totalReactions);
    }

    public void requestMovieListReactions(Integer movieListId, Integer page) {
        BackendRetrofitService.getInstance().getMovieListAPI().getMovieListReactions(movieListId, page).enqueue(
                new Callback<PaginatedResponse<List<Reaction>>>() {
                    @Override
                    public void onResponse(Call<PaginatedResponse<List<Reaction>>> call, Response<PaginatedResponse<List<Reaction>>> response) {
                        if (response.isSuccessful()) {
                            reactions.addAll(response.body().getData());
                            setMovieListTotalReactionsLiveData(response.body().getTotalRecords());
                            setMovieListReactionsLiveData(reactions);
                        } else
                            setMovieListReactionsLiveData(null);
                    }

                    @Override
                    public void onFailure(Call<PaginatedResponse<List<Reaction>>> call, Throwable t) {
                        setMovieListReactionsLiveData(null);
                    }
                });
    }

    public void requestReactionByNotification(Notification notification, NavController navController, FragmentActivity activity){
        BackendRetrofitService.getInstance().getReactionApi().getReaction(notification.getContentId()).enqueue(new Callback<Reaction>() {
            @Override
            public void onResponse(Call<Reaction> call, Response<Reaction> response) {
                if(response.body().getReactedListId() == null){
                    SnackbarUtils.failureSnackbar(activity, activity.getString(R.string.content_removed)).show();
                    return;
                }
                BackendRetrofitService.getInstance().getMovieListAPI().getMovieListBasic(response.body().getReactedListId()).enqueue(new Callback<MovieListBasic>() {
                    @Override
                    public void onResponse(Call<MovieListBasic> call, Response<MovieListBasic> response) {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("movieList",response.body());
                        navController.navigate(R.id.navigation_reaction,bundle);
                    }

                    @Override
                    public void onFailure(Call<MovieListBasic> call, Throwable t) {
                        SnackbarUtils.failureSnackbar(activity, activity.getString(R.string.snackbar_server_unreachable)).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<Reaction> call, Throwable t) {
                SnackbarUtils.failureSnackbar(activity, activity.getString(R.string.snackbar_server_unreachable)).show();
            }
        });
    }

    public void resetReactions(){
        this.reactions.clear();
    }
}


