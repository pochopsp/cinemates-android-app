package it.unina.cinemates.retrofit.backend.api;

import it.unina.cinemates.models.Report;
import it.unina.cinemates.views.backend.NotifiedReport;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ReportAPI {

    @GET("api/Reports/{id}")
    Call<NotifiedReport> getReport(@Path("id") Integer id);

    @POST("api/Reports")
    Call<ResponseBody> postReport(@Body Report report);

}
