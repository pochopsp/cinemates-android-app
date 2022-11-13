package it.unina.cinemates.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.unina.cinemates.retrofit.tmdb.TmDbCallbacks;
import it.unina.cinemates.retrofit.tmdb.TmDbRetrofitService;
import it.unina.cinemates.retrofit.tmdb.api.TmDbApi;
import it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.MovieResultWrapper;
import it.unina.cinemates.viewmodels.enums.ViewAllType;
import it.unina.cinemates.views.tmdb.MovieBasicResult;
import it.unina.cinemates.views.tmdb.MovieResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {

    TmDbApi tmDbAPI = TmDbRetrofitService.getTmDbDaoInstance();

    private final MutableLiveData<ViewAllType> viewAllTypeMutableLiveData = new MutableLiveData<>();

    private MutableLiveData<List<MovieBasicResult>> nowPlayingMovies;
    private MutableLiveData<List<MovieBasicResult>> popularMovies;
    private MutableLiveData<List<MovieBasicResult>> upcomingMovies;
    private MutableLiveData<List<MovieBasicResult>> topRatedMovies;

    private boolean backFromMovieFullOrInsertList = false;
    private final List<MovieResult> movieResultsList = new ArrayList<>();
    private final Set<MovieResult> movieResultsSet = new HashSet<>();

    public boolean backFromMovieFull(){
        return backFromMovieFullOrInsertList;
    }

    public void setBackFromMovieFullOrInsertList(boolean back){
        backFromMovieFullOrInsertList = back;
    }

    public LiveData<ViewAllType> getViewAllTypeMutableLiveData() {
        return viewAllTypeMutableLiveData;
    }

    public void setViewTypeLiveData(ViewAllType type){
        viewAllTypeMutableLiveData.setValue(type);
    }

    public LiveData<List<MovieBasicResult>> getNowPlayingMovies(){
        if(nowPlayingMovies == null) {
            nowPlayingMovies = new MutableLiveData<>();
            loadNowPlayingMovies();
        }
        return nowPlayingMovies;
    }

    private void loadNowPlayingMovies() {
            tmDbAPI.getNowPlayingBasic(1).enqueue(TmDbCallbacks.movieBasicCallbackIntoLiveData(this.nowPlayingMovies));
    }

    public LiveData<List<MovieBasicResult>> getPopularMovies(){
        if(popularMovies == null) {
            popularMovies = new MutableLiveData<>();
            loadPopularMovies();
        }
        return popularMovies;
    }

    private void loadPopularMovies() {
            tmDbAPI.getPopularBasic(1).enqueue(TmDbCallbacks.movieBasicCallbackIntoLiveData(this.popularMovies));
    }

    public LiveData<List<MovieBasicResult>> getUpcomingMovies(){
        if(upcomingMovies == null) {
            upcomingMovies = new MutableLiveData<>();
            loadUpcomingMovies();
        }
        return upcomingMovies;
    }

    private void loadUpcomingMovies() {
            tmDbAPI.getUpcomingBasic(1).enqueue(TmDbCallbacks.movieBasicCallbackIntoLiveData(this.upcomingMovies));
    }

    public LiveData<List<MovieBasicResult>> getTopRatedMovies(){
        if(topRatedMovies == null) {
            topRatedMovies = new MutableLiveData<>();
            loadTopRatedMovies();
        }
        return topRatedMovies;
    }

    private void loadTopRatedMovies() {
            tmDbAPI.getTopRatedBasic(2).enqueue(TmDbCallbacks.movieBasicCallbackIntoLiveData(this.topRatedMovies));
    }

    private final MutableLiveData<List<MovieResult>> viewAllMovieResultLiveData = new MutableLiveData<>();

    public LiveData<List<MovieResult>> getViewAllMovieResultLiveData(){ return  viewAllMovieResultLiveData; }

    public void setViewAllMovieResultLiveData(List<MovieResult> movieResults){
        viewAllMovieResultLiveData.setValue(movieResults);
    }

    public void requestNowPlayingMovieResult(Integer page){
        TmDbRetrofitService.getTmDbDaoInstance().getNowPlayingResult(page).enqueue(new Callback<MovieResultWrapper>() {
            @Override
            public void onResponse(@NonNull Call<MovieResultWrapper> call, @NonNull Response<MovieResultWrapper> response) {
                handleMovieResultResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<MovieResultWrapper> call, @NonNull Throwable t) {
                setViewAllMovieResultLiveData(null);
            }
        });
    }

    public void requestPopularMovieResult(Integer page){
        TmDbRetrofitService.getTmDbDaoInstance().getPopularResult(page).enqueue(new Callback<MovieResultWrapper>() {
            @Override
            public void onResponse(@NonNull Call<MovieResultWrapper> call, @NonNull Response<MovieResultWrapper> response) {
                handleMovieResultResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<MovieResultWrapper> call, @NonNull Throwable t) {
                setViewAllMovieResultLiveData(null);
            }
        });
    }

    public void requestUpcomingMovieResult(Integer page){
        TmDbRetrofitService.getTmDbDaoInstance().getUpcomingResult(page).enqueue(new Callback<MovieResultWrapper>() {
            @Override
            public void onResponse(@NonNull Call<MovieResultWrapper> call, @NonNull Response<MovieResultWrapper> response) {
                handleMovieResultResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<MovieResultWrapper> call, @NonNull Throwable t) {
                setViewAllMovieResultLiveData(null);
            }
        });
    }

    public void requestTopRatedMovieResult(Integer page){
        TmDbRetrofitService.getTmDbDaoInstance().getTopRatedResult(page).enqueue(new Callback<MovieResultWrapper>() {
            @Override
            public void onResponse(@NonNull Call<MovieResultWrapper> call, @NonNull Response<MovieResultWrapper> response) {
                handleMovieResultResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<MovieResultWrapper> call, @NonNull Throwable t) {
                setViewAllMovieResultLiveData(null);
            }
        });
    }

    public void resetMovieListResult(){
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

            setViewAllMovieResultLiveData(movieResultsList);
        }
    }


}
