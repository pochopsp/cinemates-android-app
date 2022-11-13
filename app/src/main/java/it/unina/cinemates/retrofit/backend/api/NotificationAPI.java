package it.unina.cinemates.retrofit.backend.api;

import it.unina.cinemates.models.Notification;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface NotificationAPI {

    @DELETE("api/Notifications/{id}")
    Call<ResponseBody> deleteNotification(@Path("id") Integer id);

    @PUT("api/Notifications/{id}")
    Call<ResponseBody> putNotification(@Path("id") Integer id, @Body Notification notification);

}
