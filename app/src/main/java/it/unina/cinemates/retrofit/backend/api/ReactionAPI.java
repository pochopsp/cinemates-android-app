package it.unina.cinemates.retrofit.backend.api;

import it.unina.cinemates.models.User;
import it.unina.cinemates.retrofit.NonPaginatedResponse;
import it.unina.cinemates.models.Reaction;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ReactionAPI {

    @GET("api/Reactions/{id}")
    Call<Reaction> getReaction(@Path("id") Integer id);

    @POST("api/Reactions")
    Call<NonPaginatedResponse<Reaction>> postReaction(@Body Reaction reaction);

    @PUT("api/Reactions/{id}")
    Call<NonPaginatedResponse<Reaction>> putReaction(@Path("id") Integer id, @Body Reaction reaction);

    @DELETE("api/Reactions/{id}")
    Call<ResponseBody> deleteReaction(@Path("id") Integer id);
}
