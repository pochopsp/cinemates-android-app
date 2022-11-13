package it.unina.cinemates.cloudservices.cognito.login;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;

import it.unina.cinemates.StartActivity;
import it.unina.cinemates.cloudservices.cognito.CognitoSettings;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.cloudservices.firebase.FirebaseCloudMessagingTokens;
import it.unina.cinemates.retrofit.backend.BackendRetrofitService;
import it.unina.cinemates.viewmodels.user.profile.logged.LoginStatusViewModel;
import it.unina.cinemates.views.backend.UserProfile;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  Si occupa del login automatico nel caso in cui un utente si fosse autenticato correttamente in precedenza.
 *  Durante il login automatico verrà mostrato il logo dell'applicazione (StartActivity)
 */
public class AutomaticLoginHandler implements AuthenticationHandler {
    
    private static final String TAG = "AUTOMATIC_LOGIN_HANDLER";

    private final LoginStatusViewModel loginStatusViewModel;

    protected final CognitoSettings cognitoSettings;

    public AutomaticLoginHandler(StartActivity activity) {
        cognitoSettings = new CognitoSettings(activity);
        loginStatusViewModel = new ViewModelProvider(activity).get(LoginStatusViewModel.class);
    }

   

    @Override
    public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
        Log.e(TAG, "Accesso effettuato con successo, token rilasciato");

        String email = cognitoSettings.getUserPool().getCurrentUser().getUserId().toLowerCase();

        LoggedUser.getInstance().setCognitoToken(userSession.getIdToken().getJWTToken());
        LoggedUser.getInstance().setEmail(email);
        Log.e(TAG,email);

        BackendRetrofitService.getInstance().getUserAPI().getUserUsernameByEmail(email).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(@NonNull Call<UserProfile> call, @NonNull Response<UserProfile> response) {
                if(response.code() == 200){
                    assert response.body() != null;
                    LoggedUser.getInstance().setUsername(response.body().getUsername());
                    Integer toWatchListId = response.body().getMovieLists().get(0).getId();
                    Integer favoritesListId = response.body().getMovieLists().get(1).getId();
                    Integer idPhoto = response.body().getIdPhoto();

                    LoggedUser.getInstance().setToWatchListId(toWatchListId);
                    LoggedUser.getInstance().setFavoritesListId(favoritesListId);
                    LoggedUser.getInstance().setIdPhoto(idPhoto);
                    loginStatusViewModel.setUserStatus(LoginStatus.LOGGED_IN);
                }
                else{
                    Log.e(TAG," " + response.code());
                    loginStatusViewModel.setUserStatus(LoginStatus.SERVER_UNREACHABLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfile> call, @NonNull Throwable t) {
                Log.e(TAG,t.getLocalizedMessage());
                loginStatusViewModel.setUserStatus(LoginStatus.SERVER_UNREACHABLE);
            }
        });
    }

    @Override
    public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
        loginStatusViewModel.setUserStatus(LoginStatus.LOGGED_OUT);
    }

    @Override
    public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
        loginStatusViewModel.setUserStatus(LoginStatus.LOGGED_OUT);
    }

    @Override
    public void authenticationChallenge(ChallengeContinuation continuation) {
        loginStatusViewModel.setUserStatus(LoginStatus.LOGGED_OUT);
    }

    @Override
    public void onFailure(Exception exception) {
        Log.e(TAG,exception.getLocalizedMessage());
        loginStatusViewModel.setUserStatus(LoginStatus.SERVER_UNREACHABLE);
    }

    public void automaticLogin() {
        Log.e(TAG, "Inizio procedura di login automatico");
        cognitoSettings.getUserPool().getCurrentUser().getSessionInBackground(this);
    }

}
