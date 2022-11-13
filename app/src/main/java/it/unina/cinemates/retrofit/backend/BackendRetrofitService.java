package it.unina.cinemates.retrofit.backend;

import android.util.Pair;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import it.unina.cinemates.BuildConfig;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.models.User;
import it.unina.cinemates.retrofit.backend.api.CommentAPI;
import it.unina.cinemates.retrofit.backend.api.FollowAPI;
import it.unina.cinemates.retrofit.backend.api.MovieListAPI;
import it.unina.cinemates.retrofit.backend.api.NotificationAPI;
import it.unina.cinemates.retrofit.backend.api.RatingAPI;
import it.unina.cinemates.retrofit.backend.api.ReactionAPI;
import it.unina.cinemates.retrofit.backend.api.ReportAPI;
import it.unina.cinemates.retrofit.backend.api.UserAPI;
import lombok.AccessLevel;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Getter
public class BackendRetrofitService {

    private static final BackendRetrofitService instance;

    //by default it waits 30 seconds before giving connectTimeout
    private static final Pair<Integer,TimeUnit> connectTimeout = new Pair<>(2, TimeUnit.SECONDS);
    private static final Pair<Integer,TimeUnit> readTimeout = new Pair<>(30, TimeUnit.SECONDS);
    private static final Pair<Integer,TimeUnit> writeTimeout = new Pair<>(15, TimeUnit.SECONDS);

    static {
        instance = new BackendRetrofitService();
    }

    public BackendRetrofitService(){
        buildRetrofit();
    }

    @Getter(AccessLevel.NONE)
    private static Retrofit retrofit;

    private UserAPI userAPI;
    private FollowAPI followAPI;
    private NotificationAPI notificationAPI;
    private MovieListAPI movieListAPI;
    private ReportAPI reportAPI;
    private CommentAPI commentAPI;
    private ReactionAPI reactionApi;
    private RatingAPI ratingApi;

    private void buildRetrofit() {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.writeTimeout(writeTimeout.first, writeTimeout.second);
        httpClient.readTimeout(readTimeout.first, readTimeout.second);
        httpClient.connectTimeout(connectTimeout.first, connectTimeout.second);

        httpClient.addInterceptor(chain -> {
            Request original = chain.request();

            if (LoggedUser.getInstance().getCognitoToken() == null) return chain.proceed(original);

            Request request = original.newBuilder()
                    .header("Auth", LoggedUser.getInstance().getCognitoToken())
                    .method(original.method(), original.body())
                    .build();

            return chain.proceed(request);
        });

        // Build retrofit once when creating a single instance
        retrofit = new retrofit2.Retrofit.Builder()
                .baseUrl(BuildConfig.BACKEND_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        this.userAPI = retrofit.create(UserAPI.class);
        this.followAPI = retrofit.create(FollowAPI.class);
        this.notificationAPI = retrofit.create(NotificationAPI.class);
        this.movieListAPI = retrofit.create(MovieListAPI.class);
        this.reportAPI = retrofit.create(ReportAPI.class);
        this.commentAPI = retrofit.create(CommentAPI.class);
        this.reactionApi = retrofit.create(ReactionAPI.class);
        this.ratingApi = retrofit.create(RatingAPI.class);
    }


    public static BackendRetrofitService getInstance() {
       return instance;
    }

}