package it.unina.cinemates.cloudservices.cognito.logout;

import android.util.Log;

import androidx.lifecycle.ViewModelProvider;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;

import it.unina.cinemates.MainActivity;
import it.unina.cinemates.cloudservices.cognito.CognitoSettings;
import it.unina.cinemates.cloudservices.cognito.login.LoginStatus;
import it.unina.cinemates.cloudservices.firebase.FirebaseCloudMessagingTokens;
import it.unina.cinemates.viewmodels.user.profile.logged.LoginStatusViewModel;

public class LogoutHandler {

    private static final String TAG = "LOGOUT_HANDLER";

    private final MainActivity mainActivity;
    private final LoginStatusViewModel loginStatusViewModel;

    public LogoutHandler(MainActivity mainActivity) {

        this.mainActivity = mainActivity;
        loginStatusViewModel = new ViewModelProvider(mainActivity).get(LoginStatusViewModel.class);
    }

    public void logout(){
        new CognitoSettings(this.mainActivity).getUserPool().getCurrentUser().globalSignOutInBackground(new GenericHandler() {
            @Override
            public void onSuccess() {
                FirebaseCloudMessagingTokens.deleteToken();
                loginStatusViewModel.setUserStatus(LoginStatus.LOGGED_OUT);
            }

            @Override
            public void onFailure(Exception exception) {
                loginStatusViewModel.setUserStatus(LoginStatus.SERVER_UNREACHABLE);
                Log.e(TAG, exception.getLocalizedMessage());
            }
        });
    }

}