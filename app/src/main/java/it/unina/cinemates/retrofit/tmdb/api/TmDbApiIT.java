package it.unina.cinemates.retrofit.tmdb.api;

import it.unina.cinemates.retrofit.tmdb.TmDbConstants;
import it.unina.cinemates.retrofit.tmdb.jsonwrappers.actor.ActorResultWrapper;
import it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.MovieBasicResultWrapper;
import it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.MovieResultWrapper;
import it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.details.MovieDetails;
import it.unina.cinemates.views.tmdb.MovieBasicResult;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TmDbApiIT extends TmDbApi {

    @Override
    @GET(TmDbConstants.Italian.MOVIE_DETAILS_URL)
    Call<MovieBasicResult> getMoviePosterAndBackdrop(@Path("movieid") int movieid);

    @Override
    @GET(TmDbConstants.Italian.SEARCH_ACTORS_URL)
    Call<ActorResultWrapper> searchActorsByName(@Query("query") String query);

    @Override
    @GET(TmDbConstants.Italian.MOVIE_DETAILS_URL)
    Call<MovieDetails> getMoviesDetailsById(@Path("movieid") int movieid);

    @Override
    @GET(TmDbConstants.Italian.NOW_PLAYING_URL)
    Call<MovieBasicResultWrapper> getNowPlayingBasic(@Query("page") int page);

    @Override
    @GET(TmDbConstants.Italian.POPULAR_URL)
    Call<MovieBasicResultWrapper> getPopularBasic(@Query("page") int page);

    @Override
    @GET(TmDbConstants.Italian.UPCOMING_URL)
    Call<MovieBasicResultWrapper> getUpcomingBasic(@Query("page") int page);

    @Override
    @GET(TmDbConstants.Italian.TOP_RATED_URL)
    Call<MovieBasicResultWrapper> getTopRatedBasic(@Query("page") int page);

    @Override
    @GET(TmDbConstants.Italian.UPCOMING_URL)
    Call<MovieResultWrapper> getUpcomingResult(@Query("page") int page);

    @GET(TmDbConstants.Italian.NOW_PLAYING_URL)
    Call<MovieResultWrapper> getNowPlayingResult(@Query("page") int page);

    @Override
    @GET(TmDbConstants.Italian.POPULAR_URL)
    Call<MovieResultWrapper> getPopularResult(@Query("page") int page);

    @Override
    @GET(TmDbConstants.Italian.TOP_RATED_URL)
    Call<MovieResultWrapper> getTopRatedResult(@Query("page") int page);

    @Override
    @GET(TmDbConstants.Italian.SEARCH_MOVIES_URL)
    Call<MovieResultWrapper> searchMoviesByTitle(@Query("query") String query, @Query("page") int page);

    @Override
    @GET(TmDbConstants.Italian.SEARCH_MOVIES_URL)
    Call<MovieResultWrapper> searchMoviesByTitleAndYear(@Query("query") String query, @Query("page") int page, @Query("year") int year);

    @Override
    @GET(TmDbConstants.Italian.SEARCH_MOVIES_BY_ACTOR)
    Call<MovieResultWrapper> searchMoviesByActor(@Query("with_cast") int actorId, @Query("page") int page);

    @Override
    @GET(TmDbConstants.Italian.SEARCH_MOVIES_BY_ACTOR)
    Call<MovieResultWrapper> searchMoviesByTitleAndYearAndActor( @Query("page") int page, @Query("year") int year,@Query("with_cast") int actorId);

}
