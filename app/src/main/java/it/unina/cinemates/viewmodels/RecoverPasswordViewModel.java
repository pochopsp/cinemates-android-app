package it.unina.cinemates.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import it.unina.cinemates.cloudservices.cognito.recoverpassword.RecoverPasswordSteps;

public class RecoverPasswordViewModel extends ViewModel {

    private final MutableLiveData<RecoverPasswordSteps> userRecoverPasswordSteps = new MutableLiveData<>(RecoverPasswordSteps.IDLE);

    public LiveData<RecoverPasswordSteps> getUserRecoverPasswordStep(){
        return userRecoverPasswordSteps;
    }

    public void setUserRecoverPasswordSteps(RecoverPasswordSteps step){
        userRecoverPasswordSteps.setValue(step);
    }
}
