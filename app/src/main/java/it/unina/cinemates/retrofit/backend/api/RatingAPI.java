package it.unina.cinemates.retrofit.backend.api;

import it.unina.cinemates.views.backend.Rating;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface RatingAPI {

    @GET("api/Ratings/{id}")
    Call<Rating> getRating(@Path("id")Integer id);

    @POST("api/Ratings")
    Call<ResponseBody> postRating(@Body it.unina.cinemates.models.Rating rating);

    @PUT("api/Ratings")
    Call<ResponseBody> putRating(@Body it.unina.cinemates.models.Rating rating);

    @DELETE("api/Ratings/{id}")
    Call<ResponseBody> deleteRating(@Path("id") Integer id);

}
