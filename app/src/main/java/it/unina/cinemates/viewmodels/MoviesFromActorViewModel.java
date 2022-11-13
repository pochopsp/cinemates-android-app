package it.unina.cinemates.viewmodels;


import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.unina.cinemates.retrofit.tmdb.TmDbRetrofitService;
import it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.MovieResultWrapper;
import it.unina.cinemates.views.tmdb.MovieResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoviesFromActorViewModel extends ViewModel {

    private final MutableLiveData<List<MovieResult>> moviesFromActor = new MutableLiveData<>(null);

    private final List<MovieResult> movieResultsList = new ArrayList<>();
    private final Set<MovieResult> movieResultsSet = new HashSet<>();


    public MutableLiveData<List<MovieResult>> getMoviesFromActor() {
        return moviesFromActor;
    }

    public void requestMoviesFromActor(String actor, int page) {

        TmDbRetrofitService.requestMoviesFromActor(
                actor,
                page,
                new Callback<MovieResultWrapper>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onResponse(@NonNull Call<MovieResultWrapper> call, @NonNull Response<MovieResultWrapper> response) {
                        assert response.body() != null;
                        if (response.body().getMovieResults() != null) {
                            handleMovieResultResponse(response);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MovieResultWrapper> call, @NonNull Throwable t) {}
                }
        );
    }

    public void resetMovieListResult() {
        movieResultsList.clear();
        movieResultsSet.clear();
    }


    private void handleMovieResultResponse(@NonNull Response<MovieResultWrapper> response) {
        assert response.body() != null;
        if (response.body().getMovieResults() != null) {

            for (MovieResult m : response.body().getMovieResults()) {
                if (movieResultsSet.add(m))
                    movieResultsList.add(m);
            }

            moviesFromActor.postValue(movieResultsList);
        }
    }


}
