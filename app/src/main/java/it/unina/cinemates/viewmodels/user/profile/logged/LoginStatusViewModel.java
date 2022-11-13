package it.unina.cinemates.viewmodels.user.profile.logged;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import it.unina.cinemates.LoginActivity;
import it.unina.cinemates.cloudservices.cognito.login.LoginStatus;
import it.unina.cinemates.cloudservices.cognito.login.ManualLoginHandler;

public class LoginStatusViewModel extends ViewModel {

    private final MutableLiveData<LoginStatus> loginStatusLiveData = new MutableLiveData<>();

    public LiveData<LoginStatus> getUserStatus(){
        return loginStatusLiveData;
    }

    public void setUserStatus(LoginStatus status){
        loginStatusLiveData.setValue(status);
    }

    private ManualLoginHandler manualLoginHandler;

    public void setUpLoginHandler(LoginActivity activity){
        manualLoginHandler = new ManualLoginHandler(activity);
    }

    public void login(String email, String password) {
        manualLoginHandler.manualLogin(email,password);
    }

    public void sendNewConfirmationEmail(String email){
        manualLoginHandler.sendNewConfirmationEmail(email);
    }
}
