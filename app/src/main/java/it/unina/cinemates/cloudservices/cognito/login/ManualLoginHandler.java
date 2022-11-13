package it.unina.cinemates.cloudservices.cognito.login;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.VerificationHandler;

import java.util.Objects;

import it.unina.cinemates.LoginActivity;
import it.unina.cinemates.cloudservices.cognito.CognitoSettings;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.cloudservices.firebase.FirebaseCloudMessagingTokens;
import it.unina.cinemates.views.backend.UserProfile;
import it.unina.cinemates.retrofit.backend.BackendRetrofitService;
import it.unina.cinemates.viewmodels.user.profile.logged.LoginStatusViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManualLoginHandler implements AuthenticationHandler {

    private static final String TAG = "MANUAL_LOGIN_HANDLER";

    private final LoginActivity loginActivity;
    private final LoginStatusViewModel loginStatusViewModel;
    private CognitoSettings cognitoSettings;

    private String userPassword;
    private String userEmail;


    public ManualLoginHandler(LoginActivity loginActivity) {
        this.loginActivity = loginActivity;
        loginStatusViewModel = new ViewModelProvider(loginActivity).get(LoginStatusViewModel.class);
    }


    @Override
    public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
        Log.e(TAG, "Accesso effettuato con successo, token rilasciato");

        LoggedUser.getInstance().setEmail(this.userEmail);

        LoggedUser.getInstance().setCognitoToken(userSession.getIdToken().getJWTToken());

        cognitoSettings.getUserPool().getCurrentUser().getDetailsInBackground(new GetDetailsHandler() {
            @Override
            public void onSuccess(CognitoUserDetails cognitoUserDetails) {
                LoggedUser.getInstance().setUsername(cognitoUserDetails.getAttributes().getAttributes().get("nickname"));
                String username = LoggedUser.getInstance().getUsername();
                BackendRetrofitService.getInstance().getUserAPI().getUser(username,username).enqueue(new Callback<UserProfile>() {
                    @Override
                    public void onResponse(@NonNull Call<UserProfile> call, @NonNull Response<UserProfile> response) {
                        if(response.code() == 200){
                            assert response.body() != null;
                            FirebaseCloudMessagingTokens.acquireAndSaveToken();
                            Integer toWatchListId = response.body().getMovieLists().get(0).getId();
                            Integer favoritesListId = response.body().getMovieLists().get(1).getId();
                            Integer idPhoto = response.body().getIdPhoto();

                            LoggedUser.getInstance().setToWatchListId(toWatchListId);
                            LoggedUser.getInstance().setFavoritesListId(favoritesListId);
                            LoggedUser.getInstance().setIdPhoto(idPhoto);

                            loginStatusViewModel.setUserStatus(LoginStatus.LOGGED_IN);
                        }
                        else{
                            loginStatusViewModel.setUserStatus(LoginStatus.SERVER_UNREACHABLE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserProfile> call, @NonNull Throwable t) {
                        loginStatusViewModel.setUserStatus(LoginStatus.SERVER_UNREACHABLE);
                    }
                });
            }

            @Override
            public void onFailure(Exception exception) {
                loginStatusViewModel.setUserStatus(LoginStatus.WRONG_CREDENTIALS);
            }
        });
    }

    @Override
    public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
        Log.e(TAG, "Recuperando i dettagli di autenticazione");

        AuthenticationDetails authDetails = new AuthenticationDetails(userId, userPassword, null);
        authenticationContinuation.setAuthenticationDetails(authDetails);
        authenticationContinuation.continueTask();
    }

    @Override
    public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
        Log.e(TAG, "Multi Factor Authentication, Non dovresti mai leggere questo log");
    }

    @Override
    public void authenticationChallenge(ChallengeContinuation continuation) {
        Log.e(TAG, "Authentication Challenge");
    }

    @Override
    public void onFailure(Exception exception) {
        Log.e(TAG, "Login fallito: " + exception);

        if (Objects.requireNonNull(exception.getLocalizedMessage()).contains("User is not confirmed"))
            loginStatusViewModel.setUserStatus(LoginStatus.ACCOUNT_NOT_CONFIRMED);

        if(Objects.requireNonNull(exception.getLocalizedMessage()).contains("Incorrect username or password"))
            loginStatusViewModel.setUserStatus(LoginStatus.WRONG_CREDENTIALS);

        if(exception.getLocalizedMessage().contains("Failed to authenticate user") ||
                exception.getLocalizedMessage().contains("Unable to resolve host")){
            cognitoSettings.getUserPool().getCurrentUser().signOut();
            loginStatusViewModel.setUserStatus(LoginStatus.SERVER_UNREACHABLE);
        }

    }

    public void manualLogin(String email, String password) {
        Log.e(TAG, "Inizio procedura di login manuale");
        cognitoSettings = new CognitoSettings(loginActivity.getBaseContext());

        CognitoUser cognitoUser = cognitoSettings.getUserPool().getUser(email);
        userPassword = password;
        this.userEmail = email;
        cognitoUser.getSessionInBackground(this);
    }

    public void sendNewConfirmationEmail(String email){
        cognitoSettings.getUserPool().getUser(email).resendConfirmationCodeInBackground(new VerificationHandler() {
            @Override
            public void onSuccess(CognitoUserCodeDeliveryDetails verificationCodeDeliveryMedium) {
                Log.e(TAG,"Email inviata");
            }

            @Override
            public void onFailure(Exception exception) {
                Log.e(TAG,"Invio email fallito");
            }
        });
    }

}
