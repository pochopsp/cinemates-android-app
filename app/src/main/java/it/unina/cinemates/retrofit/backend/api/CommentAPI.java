package it.unina.cinemates.retrofit.backend.api;

import it.unina.cinemates.retrofit.NonPaginatedResponse;
import it.unina.cinemates.retrofit.backend.jsonwrappers.post.CommentPostJsonWrapper;
import it.unina.cinemates.views.backend.Comment;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CommentAPI {

    @GET("api/Comments/{id}")
    Call<Comment> getComment(@Path("id") Integer id);

    @POST("api/Comments")
    Call<NonPaginatedResponse<Comment>> postComment(@Body CommentPostJsonWrapper comment);

    @DELETE("api/Comments/{id}")
    Call<ResponseBody> deleteComment(@Path("id") Integer id);
}
