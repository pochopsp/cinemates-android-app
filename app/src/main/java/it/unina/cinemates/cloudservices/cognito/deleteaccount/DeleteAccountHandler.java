package it.unina.cinemates.cloudservices.cognito.deleteaccount;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;

import org.jetbrains.annotations.NotNull;

import it.unina.cinemates.MainActivity;
import it.unina.cinemates.cloudservices.cognito.CognitoSettings;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.retrofit.backend.BackendRetrofitService;
import it.unina.cinemates.retrofit.backend.enums.BackendOperationResult;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DeleteAccountHandler implements GenericHandler {
    private static final String TAG = "DELETE_ACCOUNT_HANDLER";

    private final MainActivity mainActivity;

    private MutableLiveData<BackendOperationResult> deleteAccountResult;

    public DeleteAccountHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }


    @Override
    public void onSuccess() {
        Log.e(TAG, "Account eliminato con successo da cognito");
        deleteAccountResult.postValue(BackendOperationResult.SUCCESS);
        deleteAccountFromDatabase();
    }

    @Override
    public void onFailure(Exception exception) {
        Log.e(TAG, "Non è stato possibile eliminare l'account: " + exception.getLocalizedMessage());
        deleteAccountResult.postValue(BackendOperationResult.SERVER_UNREACHABLE);
    }

    public void deleteAccount(String confirmUsername, MutableLiveData<BackendOperationResult> deleteAccountResult) {
        this.deleteAccountResult = deleteAccountResult;
        CognitoSettings cognitoSettings = new CognitoSettings(mainActivity);
        String loggedUserUsername = LoggedUser.getInstance().getUsername();

        if (confirmUsername.equals(loggedUserUsername))
            cognitoSettings.getUserPool().getCurrentUser().deleteUserInBackground(this);
    }

    private void deleteAccountFromDatabase() {
        String username = LoggedUser.getInstance().getUsername();
        Call<ResponseBody> deleteUser = BackendRetrofitService.getInstance().getUserAPI().deleteUser(username);
        deleteUser.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "onResponse: " + "deleteUser() didn't work");
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                Log.d(TAG, "onFailure: " + "deleteUser() failed");
            }
        });
    }
}
