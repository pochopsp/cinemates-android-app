package it.unina.cinemates.cloudservices.cognito.signup;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.Map;
import java.util.Objects;

import it.unina.cinemates.LoginActivity;
import it.unina.cinemates.cloudservices.cloudinary.CloudinaryHelper;
import it.unina.cinemates.cloudservices.cloudinary.OnSuccessfulUploadAction;
import it.unina.cinemates.cloudservices.cognito.CognitoSettings;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.cloudservices.firebase.FirebaseCloudMessagingTokens;
import it.unina.cinemates.models.User;
import it.unina.cinemates.retrofit.backend.BackendRetrofitService;
import it.unina.cinemates.retrofit.NonPaginatedResponse;
import it.unina.cinemates.utils.IntegerUtils;
import it.unina.cinemates.viewmodels.SignUpViewModel;
import it.unina.cinemates.views.backend.UserProfile;
import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupHandler implements SignUpHandler {
    private static final String TAG = "SIGNUP_HANDLER";

    private final CognitoSettings cognitoSettings;
    private final CognitoUserAttributes userAttributes;
    private final SignUpViewModel signUpViewModel;
    private Uri profilePicUri;


    public SignupHandler(LoginActivity loginActivity) {
        cognitoSettings = new CognitoSettings(loginActivity);
        userAttributes = new CognitoUserAttributes();
        signUpViewModel = new ViewModelProvider(loginActivity).get(SignUpViewModel.class);

    }

    @Override
    public void onSuccess(CognitoUser user, boolean signUpConfirmationState, CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
        if (signUpConfirmationState) {
            Log.e(TAG, "l'account non richiede verifica");
        } else {
            if (cognitoUserCodeDeliveryDetails != null) {
                Log.e(
                        TAG, "l'account richiede verifica " +
                                cognitoUserCodeDeliveryDetails.getDestination()
                );
            }
        }
        String username = userAttributes.getAttributes().get("nickname");
        String email = userAttributes.getAttributes().get("email");
        LoggedUser.getInstance().setUsername(username);
        LoggedUser.getInstance().setEmail(email);

        if (profilePicUri != null) {
            Integer randomId = IntegerUtils.randomInteger();
            CloudinaryHelper.uploadImageWithIntegerId(profilePicUri, randomId, () -> databaseSignUp(username, email, randomId), TAG);
        } else
            databaseSignUp(username, email, null);

    }

    @Override
    public void onFailure(Exception exception) {
        String emailTakenError = "An account with the given email already exists";
        if(Objects.requireNonNull(exception.getLocalizedMessage()).contains(emailTakenError))
            signUpViewModel.setSignUpStatusLiveData(SignUpStatus.EMAIL_TAKEN);
        else
            signUpViewModel.setSignUpStatusLiveData(SignUpStatus.GENERIC_ERROR);
        Log.e(TAG, exception.getLocalizedMessage());
    }

    public void signupUser(String username, String email, String password, Uri image) {
        this.profilePicUri = image;
        final SignupHandler signupHandler = this;
        BackendRetrofitService.getInstance().getUserAPI().getUser(username, username).enqueue(new Callback<UserProfile>() {
            @SneakyThrows
            @Override
            public void onResponse(@NonNull Call<UserProfile> call, @NonNull Response<UserProfile> response) {
                if (response.isSuccessful() && response.body().getUsername() != null)
                    signUpViewModel.setSignUpStatusLiveData(SignUpStatus.USERNAME_TAKEN);
                else {
                    userAttributes.addAttribute("nickname", username);
                    userAttributes.addAttribute("email", email);
                    cognitoSettings.getUserPool().signUpInBackground(email, password,
                            userAttributes, null, signupHandler);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfile> call, @NonNull Throwable t) {
                signUpViewModel.setSignUpStatusLiveData(SignUpStatus.SERVER_UNREACHABLE);
            }
        });
    }

    public void databaseSignUp(String username, String email, Integer idPhoto) {

        User user = new User(username, email, idPhoto, null, 0, 0);

        BackendRetrofitService.getInstance().getUserAPI().postUser(user).enqueue(new Callback<NonPaginatedResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<NonPaginatedResponse<User>> call, @NonNull Response<NonPaginatedResponse<User>> response) {
                if (response.code() == 200) {
                    signUpViewModel.setSignUpStatusLiveData(SignUpStatus.SUCCESS);
                    FirebaseCloudMessagingTokens.acquireAndSaveToken();
                } else {
                    signUpViewModel.setSignUpStatusLiveData(SignUpStatus.SERVER_UNREACHABLE);
                }
                assert response.body() != null;
                Log.e(TAG, response.body().toString());
            }


            @Override
            public void onFailure(@NonNull Call<NonPaginatedResponse<User>> call, @NonNull Throwable t) {
                signUpViewModel.setSignUpStatusLiveData(SignUpStatus.SERVER_UNREACHABLE);
            }
        });
    }

}




