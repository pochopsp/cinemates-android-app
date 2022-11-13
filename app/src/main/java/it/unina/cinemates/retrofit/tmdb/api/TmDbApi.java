package it.unina.cinemates.retrofit.tmdb.api;

import it.unina.cinemates.retrofit.tmdb.jsonwrappers.actor.ActorResultWrapper;
import it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.MovieBasicResultWrapper;
import it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.MovieResultWrapper;
import it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.details.MovieDetails;
import it.unina.cinemates.views.tmdb.MovieBasicResult;
import retrofit2.Call;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TmDbApi {

    Call<MovieBasicResult> getMoviePosterAndBackdrop(@Path("movieid") int movieid);

    Call<ActorResultWrapper> searchActorsByName(@Query("query") String query);

    Call<MovieResultWrapper> searchMoviesByTitle(@Query("query") String query, @Query("page") int page);

    Call<MovieResultWrapper> searchMoviesByTitleAndYear(@Query("query") String query, @Query("page") int page, @Query("year") int year);

    Call<MovieResultWrapper> searchMoviesByActor(@Query("with_cast") int actorId, @Query("page") int page);

    Call<MovieResultWrapper> searchMoviesByTitleAndYearAndActor(@Query("page") int page, @Query("year") int year,@Query("with_cast") int actorId);

    Call<MovieDetails> getMoviesDetailsById(@Path("movieid") int movieid);


    Call<MovieBasicResultWrapper> getUpcomingBasic(@Query("page") int page);

    Call<MovieBasicResultWrapper> getPopularBasic(@Query("page") int page);

    Call<MovieBasicResultWrapper> getNowPlayingBasic(@Query("page") int page);

    Call<MovieBasicResultWrapper> getTopRatedBasic(@Query("page") int page);



    Call<MovieResultWrapper> getUpcomingResult(@Query("page") int page);

    Call<MovieResultWrapper> getNowPlayingResult(@Query("page") int page);

    Call<MovieResultWrapper> getPopularResult(@Query("page") int page);

    Call<MovieResultWrapper> getTopRatedResult(@Query("page") int page);
}
