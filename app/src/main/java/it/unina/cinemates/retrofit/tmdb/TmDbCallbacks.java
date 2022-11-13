package it.unina.cinemates.retrofit.tmdb;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.MovieBasicResultWrapper;
import it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.details.MovieDetails;
import it.unina.cinemates.viewmodels.MovieFullViewModel;
import it.unina.cinemates.views.tmdb.MovieBasicResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TmDbCallbacks {

    public static Callback<MovieDetails> movieDetailsCallBack(MovieFullViewModel movieFullViewModel){
        return new Callback<MovieDetails>() {
            @Override
            public void onResponse(@NonNull Call<MovieDetails> call, @NonNull Response<MovieDetails> response) {
                if(response.code() == 200) {
                    MovieDetails movieDetails = response.body();
                    assert movieDetails != null;
                    movieFullViewModel.setMovieFullLiveData(movieDetails);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieDetails> call, @NonNull Throwable t) {
                movieFullViewModel.setMovieFullLiveData(null);
            }
        };
    }

    public static Callback<MovieBasicResultWrapper> movieBasicCallbackIntoLiveData(MutableLiveData<List<MovieBasicResult>> basicMovies) {
        return new Callback<MovieBasicResultWrapper>() {
            @Override
            public void onResponse(@NonNull Call<MovieBasicResultWrapper> call, @NonNull Response<MovieBasicResultWrapper> response) {
                assert response.body() != null;
                basicMovies.postValue(response.body().getMovies());
            }

            @Override
            public void onFailure(@NonNull Call<MovieBasicResultWrapper> call, @NonNull Throwable t) {
                //TODO gestire questa cosa
            }
        };
    }
}
