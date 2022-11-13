package it.unina.cinemates.cloudservices.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;


import org.jetbrains.annotations.NotNull;

import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.models.User;
import it.unina.cinemates.retrofit.backend.BackendRetrofitService;
import it.unina.cinemates.retrofit.NonPaginatedResponse;
import it.unina.cinemates.views.backend.UserProfile;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirebaseCloudMessagingTokens extends FirebaseMessagingService {
    private static final String TAG = "FirebaseCloudMessagingTokens";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        Log.e(TAG, "Nuovo token generato");

        if (LoggedUser.getInstance().getFirebaseToken() == null ||
            LoggedUser.getInstance().getFirebaseToken().isEmpty() ||
            LoggedUser.getInstance().getFirebaseToken().equals("null"))
            return;

        saveTokenIntoBackend(token);
    }

    public static void acquireAndSaveToken() {
        Task<String> firebaseCloudMessaging = FirebaseMessaging.getInstance().getToken();
        firebaseCloudMessaging.addOnSuccessListener(FirebaseCloudMessagingTokens::saveTokenIntoBackend);
        firebaseCloudMessaging.addOnFailureListener(e -> Log.e(TAG,e.getLocalizedMessage()));
    }

    private static void saveTokenIntoBackend(String token) {
        Log.e(TAG, "Salvataggio token nel database");

        String username = LoggedUser.getInstance().getUsername();

        BackendRetrofitService.getInstance().getUserAPI().putUserTokenFirebase(username,token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "saveToken onResponse: Token save error " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "saveToken onResponse: error: " + t.getLocalizedMessage());
            }
        });
    }

    public static void deleteToken() {
        deleteTokenFromBackend();
        FirebaseMessaging.getInstance().deleteToken();
    }


    private static void deleteTokenFromBackend() {
        Log.e(TAG, "Rimozione token dal database");

        String username = LoggedUser.getInstance().getUsername();

        BackendRetrofitService.getInstance().getUserAPI().putUserTokenFirebase(username,"").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "saveToken onResponse: Token save error " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "saveToken onResponse: error: " + t.getLocalizedMessage());
            }
        });
    }
}
