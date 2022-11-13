package it.unina.cinemates.cloudservices.cognito.changepassword;


import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;


import java.util.Objects;

import it.unina.cinemates.MainActivity;
import it.unina.cinemates.cloudservices.cognito.CognitoSettings;
import it.unina.cinemates.retrofit.backend.enums.BackendOperationResult;

public class ChangePasswordHandler implements GenericHandler {
    private static final String TAG = "CHANGE_PASSWORD_HANDLER";

    private final MainActivity mainActivity;
    private MutableLiveData<BackendOperationResult> changePasswordResult;

    public ChangePasswordHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }


    @Override
    public void onSuccess() {
        changePasswordResult.postValue(BackendOperationResult.SUCCESS);
        Log.e(TAG, "Password cambiata con successo");
    }

    @Override
    public void onFailure(Exception exception) {
        Log.e(TAG, "Non è stato possibile cambiare la password: " + exception.getLocalizedMessage());

        if (Objects.requireNonNull(exception.getLocalizedMessage()).contains("Incorrect username or password.")) {
            changePasswordResult.postValue(BackendOperationResult.BAD_REQUEST);
        } else if (exception.getLocalizedMessage().contains("Attempt limit exceeded, please try after some time")) {
            changePasswordResult.postValue(BackendOperationResult.TOO_MANY_ATTEMPTS);
        }
    }

    public void setNewPassword(String oldPassword, String newPassword, MutableLiveData<BackendOperationResult> changePasswordResult) {
            CognitoSettings cSettings = new CognitoSettings(mainActivity);
            this.changePasswordResult = changePasswordResult;
            cSettings.getUserPool().getCurrentUser().changePasswordInBackground(oldPassword, newPassword, this);
    }
}
