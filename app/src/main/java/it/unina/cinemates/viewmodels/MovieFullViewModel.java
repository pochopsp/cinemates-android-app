package it.unina.cinemates.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.details.MovieDetails;

public class MovieFullViewModel extends ViewModel {

    private final MutableLiveData<MovieDetails> movieDetailsMutableLiveData = new MutableLiveData<>(null);

    public LiveData<MovieDetails> getMovieFullLiveData() {
        return movieDetailsMutableLiveData;
    }

    public void setMovieFullLiveData(MovieDetails movie){
        movieDetailsMutableLiveData.setValue(movie);
    }

}
