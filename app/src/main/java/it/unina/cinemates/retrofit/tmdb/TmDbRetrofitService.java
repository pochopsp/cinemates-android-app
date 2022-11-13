package it.unina.cinemates.retrofit.tmdb;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import it.unina.cinemates.models.Movie;
import it.unina.cinemates.retrofit.tmdb.api.TmDbApi;
import it.unina.cinemates.retrofit.tmdb.api.TmDbApiEN;
import it.unina.cinemates.retrofit.tmdb.api.TmDbApiIT;
import it.unina.cinemates.retrofit.tmdb.jsonwrappers.actor.ActorResultWrapper;
import it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.MovieResultWrapper;
import it.unina.cinemates.views.tmdb.MovieBasicResult;
import it.unina.cinemates.views.tmdb.MoviePoster;
import it.unina.cinemates.views.tmdb.MovieResult;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TmDbRetrofitService {

    private static TmDbApi tmDbAPI;

    static {
        try {
             new TmDbRetrofitService();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private TmDbRetrofitService() throws NoSuchAlgorithmException {
        buildRetrofitService();
    }

    private void buildRetrofitService() {
        final Retrofit retrofitClient = new Retrofit.Builder()
                .client(new OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(TmDbConstants.BASE_URL)
                .build();

        if(Locale.getDefault().getLanguage().equals("it"))
            tmDbAPI = retrofitClient.create(TmDbApiIT.class);
        else
            tmDbAPI = retrofitClient.create(TmDbApiEN.class);
    }

    public static TmDbApi getTmDbDaoInstance(){
        return tmDbAPI;
    }


    public static void fetchMoviePosters(List<Movie> movies, MutableLiveData<List<MoviePoster>> moviePostersMutableLiveData){
        List<MoviePoster> posters = fetchMoviePosters(movies);
        moviePostersMutableLiveData.postValue(posters);
    }

    public static List<MoviePoster> fetchMoviePosters(List<Movie> movies){

        ExecutorService retrieveMoviePostersExecutorService = Executors.newFixedThreadPool(movies.size());

        BlockingQueue<MoviePoster> moviePostersQueue = new ArrayBlockingQueue<>(movies.size());

        for(Movie movie : movies){
            Runnable moviePosterRunnable = () -> {
                try {
                    Response<MovieBasicResult> response = tmDbAPI.getMoviePosterAndBackdrop(movie.getTmDbId()).execute();
                    assert response.body() != null;
                    moviePostersQueue.put(new MoviePoster(movie.getTmDbId(),response.body().getPosterPath()));

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            };
            retrieveMoviePostersExecutorService.submit(moviePosterRunnable);
        }

        retrieveMoviePostersExecutorService.shutdown();

        try {
            retrieveMoviePostersExecutorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<MoviePoster> moviePosters = new ArrayList<>(moviePostersQueue);

        moviePosters.sort((m1, m2) -> {
            if (m1.getPosterPath() != null && m2.getPosterPath() == null)
                return 1;
            else if (m1.getPosterPath() == null && m2.getPosterPath() == null)
                return 0;
            else if (m1.getPosterPath() == null && m2.getPosterPath() != null)
                return -1;
            else
                return m1.getPosterPath().compareTo(m2.getPosterPath());
        });

        return moviePosters;
    }


    public static void requestMoviesFromActor(String actor, int page, Callback<MovieResultWrapper> moviesFromActorCallback) {
        getTmDbDaoInstance().searchActorsByName(actor).enqueue(
                new Callback<ActorResultWrapper>() {
                    @Override
                    public void onResponse(@NonNull Call<ActorResultWrapper> call, @NonNull Response<ActorResultWrapper> response) {
                        if (response.body() != null && response.body().getActorResult().size() != 0) {
                            int actorId = response.body().getActorResult().get(0).getId();
                            TmDbRetrofitService.getTmDbDaoInstance().searchMoviesByActor(actorId, page).enqueue(moviesFromActorCallback);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ActorResultWrapper> call, @NonNull Throwable t) {}
                });
    }

}
