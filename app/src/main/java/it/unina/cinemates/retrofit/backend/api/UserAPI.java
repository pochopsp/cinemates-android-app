package it.unina.cinemates.retrofit.backend.api;

import java.util.List;

import it.unina.cinemates.models.Notification;
import it.unina.cinemates.models.User;
import it.unina.cinemates.retrofit.NonPaginatedResponse;
import it.unina.cinemates.retrofit.PaginatedResponse;
import it.unina.cinemates.views.backend.DiscoverListsElement;
import it.unina.cinemates.views.backend.UserProfile;
import it.unina.cinemates.views.backend.UserResult;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserAPI {

    @GET("api/Users/{username}")
    Call<UserProfile> getUser(@Path("username") String username, @Query("requester") String requester);

    @GET("api/Users/byEmail/{email}")
    Call<UserProfile> getUserUsernameByEmail(@Path("email") String email);

    @POST("api/Users")
    Call<NonPaginatedResponse<User>> postUser(@Body User user);

    @PUT("api/Users/{username}/tokenFirebase")
    Call<ResponseBody> putUserTokenFirebase(@Path("username") String username, @Query("newTokenFirebase") String newTokenFirebase);

    @PUT("api/Users/{username}/bio")
    Call<ResponseBody> putUserBio(@Path("username") String username, @Query("newBio") String newBio);

    @DELETE("api/Users/{username}/bio")
    Call<ResponseBody> deleteUserBio(@Path("username") String username);

    @PUT("api/Users/{username}/idPhoto")
    Call<ResponseBody> putUserIdPhoto(@Path("username") String username, @Query("newIdPhoto") Integer newIdPhoto);

    @DELETE("api/Users/{username}/idPhoto")
    Call<ResponseBody> deleteUserIdPhoto(@Path("username") String username, @Query("IdPhotoToBeDeleted") Integer IdPhotoToBeDeleted);

    @DELETE("api/Users/{username}")
    Call<ResponseBody> deleteUser(@Path("username") String username);

    @GET("api/Users/{username}/notifications")
    Call<PaginatedResponse<List<Notification>>> getUserNotifications(@Path("username") String username, @Query("PageNumber") Integer pageNumber);

    @PUT("api/Users/{username}/notifications")
    Call<ResponseBody> putAllNotificationRead(@Path("username") String username);

    @GET("api/Users/search")
    Call<PaginatedResponse<List<UserResult>>> searchUser(@Query("Query") String query, @Query("PageNumber") Integer pageNumber,@Query("Requester") String requester);

    @GET("api/Users/{username}/following")
    Call<PaginatedResponse<List<UserResult>>> getUserFollowing(@Path("username") String username, @Query("PageNumber") Integer pageNumber, @Query("Requester") String requester);

    @GET("api/Users/{username}/following/movielists")
    Call<PaginatedResponse<List<DiscoverListsElement>>> getUserFollowingMovieLists(@Path("username") String username, @Query("Filter") String category, @Query("PageNumber") int pageNumber);

    @GET("api/Users/{username}/followers")
    Call<PaginatedResponse<List<UserResult>>> getUserFollowers(@Path("username") String username, @Query("PageNumber") Integer pageNumber, @Query("Requester") String requester);

}
