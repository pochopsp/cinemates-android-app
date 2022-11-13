package it.unina.cinemates.viewmodels;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import it.unina.cinemates.LoginActivity;
import it.unina.cinemates.cloudservices.cognito.signup.SignUpStatus;
import it.unina.cinemates.cloudservices.cognito.signup.SignupHandler;

public class SignUpViewModel extends ViewModel {

    private final MutableLiveData<SignUpStatus> signUpLiveData = new MutableLiveData<>(SignUpStatus.IDLE);

    public LiveData<SignUpStatus> getSignUpStatusLiveData(){
        return signUpLiveData;
    }

    public void setSignUpStatusLiveData(SignUpStatus signUp){
        signUpLiveData.setValue(signUp);
    }


    private SignupHandler signupHandler;
    public void setupSignupHandler(LoginActivity loginActivity){
        signupHandler = new SignupHandler(loginActivity);
    }

    public void signUp(String username, String email, String password, Uri imageUri){
        signupHandler.signupUser(username, email, password, imageUri);
    }

}
