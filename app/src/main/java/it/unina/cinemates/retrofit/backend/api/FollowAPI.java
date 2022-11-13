package it.unina.cinemates.retrofit.backend.api;

import it.unina.cinemates.models.Follow;
import it.unina.cinemates.retrofit.NonPaginatedResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface FollowAPI {

    @POST("api/Follows")
    Call<NonPaginatedResponse<Follow>> postFollow(@Body Follow follow);

    @PUT("api/Follows/{compositeKey}")
    Call<NonPaginatedResponse<Follow>> putFollow(@Path("compositeKey") String compositeKey, @Body Follow follow);

    @DELETE("api/Follows/{compositeKey}")
    Call<ResponseBody> deleteFollow(@Path("compositeKey") String compositeKey);

}

