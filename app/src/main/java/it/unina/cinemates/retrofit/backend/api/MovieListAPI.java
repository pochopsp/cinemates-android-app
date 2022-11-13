package it.unina.cinemates.retrofit.backend.api;

import java.util.List;

import it.unina.cinemates.views.backend.MovieListFull;
import it.unina.cinemates.views.backend.Rating;
import it.unina.cinemates.retrofit.NonPaginatedResponse;
import it.unina.cinemates.retrofit.PaginatedResponse;
import it.unina.cinemates.retrofit.backend.jsonwrappers.post.MovieListPostJsonWrapper;
import it.unina.cinemates.views.backend.Comment;
import it.unina.cinemates.views.backend.MovieListBasic;
import it.unina.cinemates.views.backend.MovieListPreview;
import it.unina.cinemates.views.backend.MoviesInList;
import it.unina.cinemates.models.Reaction;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MovieListAPI {


    @PUT("api/MovieLists/{movieListId}/movies/{idMovie}")
    Call<NonPaginatedResponse<MovieListPreview>> insertMovieInMovieList(@Path("movieListId") Integer movieListId, @Path("idMovie") Integer idMovie);

    @DELETE("api/MovieLists/{movieListId}/movies")
    Call<ResponseBody> deleteMoviesFromMovieList(@Path("movieListId") Integer movieListId, @Query("id") String movieIds);

    @GET("api/MovieLists/{movieListId}")
    Call<MovieListFull> getMovieListFull(@Path("movieListId") Integer movieListId, @Query("Requester") String requester);

    @GET("api/MovieLists/{movieListId}")
    Call<MoviesInList> getMoviesInList(@Path("movieListId") Integer movieListId);

    @GET("api/MovieLists/search")
    Call<PaginatedResponse<List<MovieListPreview>>> searchMovieListByName(@Query("Owner") String owner, @Query("Query") String query, @Query("PageNumber") Integer pageNumber);

    @GET("api/MovieLists/{movieListId}/comments")
    Call<PaginatedResponse<List<Comment>>> getMovieListComments(@Path("movieListId") Integer movieListId, @Query("PageNumber") Integer pageNumber);

    @GET("api/MovieLists/{movieListId}/reactions")
    Call<PaginatedResponse<List<Reaction>>> getMovieListReactions(@Path("movieListId") Integer movieListId, @Query("PageNumber") Integer pageNumber);

    @GET("api/MovieLists/{movieListId}/ratings")
    Call<PaginatedResponse<List<Rating>>> getMovieListRatings(@Path("movieListId") Integer movieListId, @Query("PageNumber") Integer pageNumber);

    @GET("api/MovieLists/{movieListId}")
    Call<MovieListBasic> getMovieListBasic(@Path("movieListId") Integer commentedListId);

    @POST("api/MovieLists")
    Call<NonPaginatedResponse<MovieListPreview>> postMovieList(@Body MovieListPostJsonWrapper movieList);

    @DELETE("api/MovieLists/{movieListId}")
    Call<ResponseBody> deleteMovieList(@Path("movieListId") Integer movieListId);

    @PUT("api/MovieLists/{movieListId}/newName")
    Call<ResponseBody> putMovieListName(@Path("movieListId") Integer movieListId, @Query("newName") String newName);
}
