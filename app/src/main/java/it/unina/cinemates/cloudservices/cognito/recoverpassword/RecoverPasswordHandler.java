package it.unina.cinemates.cloudservices.cognito.recoverpassword;

import android.util.Log;

import androidx.lifecycle.ViewModelProvider;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler;

import java.util.Objects;

import it.unina.cinemates.LoginActivity;
import it.unina.cinemates.cloudservices.cognito.CognitoSettings;
import it.unina.cinemates.viewmodels.RecoverPasswordViewModel;

public class RecoverPasswordHandler implements ForgotPasswordHandler {

    private final CognitoSettings cognitoSettings;
    private ForgotPasswordContinuation resultContinuation;
    private final RecoverPasswordViewModel recoverPasswordViewModel;

    public RecoverPasswordHandler(LoginActivity loginActivity) {
        cognitoSettings = new CognitoSettings(loginActivity);
        recoverPasswordViewModel = new ViewModelProvider(loginActivity).get(RecoverPasswordViewModel.class);
    }


    @Override
    public void onSuccess() {
        Log.e("Forgot Password", "Password cambiata con successo");
        recoverPasswordViewModel.setUserRecoverPasswordSteps(RecoverPasswordSteps.CHANGE_PASSWORD_SUCCESS);
    }

    @Override
    public void getResetCode(ForgotPasswordContinuation continuation) {
        CognitoUserCodeDeliveryDetails codeSentHere = continuation.getParameters();
        Log.e("Forgot Password", "Codice inviato qui: " + codeSentHere.getDestination());
        recoverPasswordViewModel.setUserRecoverPasswordSteps(RecoverPasswordSteps.EMAIL_SUCCESS);
        resultContinuation = continuation;
    }

    @Override
    public void onFailure(Exception exception) {
        Log.e("Forgot Password", "Qualcosa è andato storto " + exception.getLocalizedMessage());

        if(Objects.requireNonNull(exception.getLocalizedMessage()).contains("LimitExceededException")) {
            recoverPasswordViewModel.setUserRecoverPasswordSteps(RecoverPasswordSteps.TOO_MANY_ATTEMPTS);
            return;
        }

        if (Objects.requireNonNull(exception.getLocalizedMessage()).contains("Attempt limit exceeded, please try after some time")) {
          recoverPasswordViewModel.setUserRecoverPasswordSteps(RecoverPasswordSteps.CONFIRMATION_CODE_FAILED);
        } else if (exception.getLocalizedMessage().contains("Invalid verification code provided, please try again")) {
            recoverPasswordViewModel.setUserRecoverPasswordSteps(RecoverPasswordSteps.CONFIRMATION_CODE_FAILED);
        } else if(exception.getLocalizedMessage().contains("Password does not conform to policy")){
            recoverPasswordViewModel.setUserRecoverPasswordSteps(RecoverPasswordSteps.CONFIRMATION_CODE_SUCCESS);
        }
    }

    public void setEmailForResetPassword(String email) {

        CognitoUser thisUser = cognitoSettings.getUserPool().getUser(email);
        thisUser.forgotPasswordInBackground(this);
    }

    public void setConfirmationCode(String verificationCode) {
        resultContinuation.setVerificationCode(verificationCode);
        resultContinuation.setPassword("placeholder");
        resultContinuation.continueTask();
    }

    public void setNewPassword(String password) {
            resultContinuation.setPassword(password);
            resultContinuation.continueTask();
    }

}
