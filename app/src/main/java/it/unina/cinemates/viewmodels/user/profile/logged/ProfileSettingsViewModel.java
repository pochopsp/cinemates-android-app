package it.unina.cinemates.viewmodels.user.profile.logged;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import it.unina.cinemates.cloudservices.cloudinary.CloudinaryHelper;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.cloudservices.cognito.changepassword.ChangePasswordHandler;
import it.unina.cinemates.cloudservices.cognito.deleteaccount.DeleteAccountHandler;
import it.unina.cinemates.retrofit.backend.BackendRetrofitService;
import it.unina.cinemates.retrofit.backend.callbacks.AlterDataCallback;
import it.unina.cinemates.retrofit.backend.enums.BackendOperationResult;

public class ProfileSettingsViewModel extends ViewModel {

    private static final String TAG = "USERPROFILESETTINGS_VIEWMODEL";

    // ------------------------------- CHANGE PASSWORD ------------------------------
    private MutableLiveData<BackendOperationResult> changePasswordResult = new MutableLiveData<>(BackendOperationResult.IDLE);
    public MutableLiveData<BackendOperationResult> getChangePasswordResult() { return changePasswordResult; }


    public void changePassword(String currentPassword, String newPassword, ChangePasswordHandler changePasswordHandler) {
        changePasswordHandler.setNewPassword(currentPassword, newPassword, changePasswordResult);
    }

    public void resetChangePasswordResult() {
        changePasswordResult.postValue(BackendOperationResult.IDLE);
    }

    // ------------------------------- DELETE ACCOUNT -------------------------------


    public void requestDeleteAccount(String typedUsername, DeleteAccountHandler deleteAccountHandler) {
        Log.e(TAG,"eliminando l'account di " + typedUsername + "...");
        deleteAccountHandler.deleteAccount(typedUsername, deleteAccountResult);
    }

    private MutableLiveData<BackendOperationResult> deleteAccountResult = new MutableLiveData<>(BackendOperationResult.IDLE);
    public MutableLiveData<BackendOperationResult> getDeleteAccountResult() { return deleteAccountResult; }

    public void resetDeleteAccountResult() {
        deleteAccountResult.postValue(BackendOperationResult.IDLE);
    }

    // ------------------------------- EDIT BIO ----------------------------------

    private MutableLiveData<BackendOperationResult> updateBioResult = new MutableLiveData<>(BackendOperationResult.IDLE);

    public MutableLiveData<BackendOperationResult> getUpdateBioResult() {
        return updateBioResult;
    }


    private MutableLiveData<BackendOperationResult> updateIdPhotoResult = new MutableLiveData<>(BackendOperationResult.IDLE);

    public MutableLiveData<BackendOperationResult> getUpdateIdPhotoResult() {
        return updateIdPhotoResult;
    }

    public void requestBioUpdate(String newBio) {
        if(newBio == null){
            BackendRetrofitService
                    .getInstance()
                    .getUserAPI()
                    .deleteUserBio(LoggedUser.getInstance().getUsername())
                    .enqueue(new AlterDataCallback(updateBioResult, TAG));
            return;
        }

        BackendRetrofitService
                .getInstance()
                .getUserAPI()
                .putUserBio(LoggedUser.getInstance().getUsername(), newBio)
                .enqueue(new AlterDataCallback(updateBioResult, TAG));
    }

    // ------------------------------- CHANGE PROFILE PIC ----------------------------------

    public void requestProfilePicUpdate(Uri profilePicUri, Integer newIdPhoto) {
        CloudinaryHelper.uploadImageWithIntegerId(
                profilePicUri,
                newIdPhoto,
                () -> BackendRetrofitService
                        .getInstance()
                        .getUserAPI()
                        .putUserIdPhoto(LoggedUser.getInstance().getUsername(), newIdPhoto)
                        .enqueue(new AlterDataCallback(updateIdPhotoResult, TAG)),
                TAG
        );
    }


    public void resetBioAndIdPhotoUpdateResultLiveData() {
        updateBioResult.postValue(BackendOperationResult.IDLE);
        updateIdPhotoResult.postValue(BackendOperationResult.IDLE);
    }

    // ------------------------------- DELETE PROFILE PIC ---------------------------

    private MutableLiveData<BackendOperationResult> deleteProfilePicResult = new MutableLiveData<>(BackendOperationResult.IDLE);
    public MutableLiveData<BackendOperationResult> getDeleteProfilePicResult() { return deleteProfilePicResult; }

    public void resetDeleteProfilePicResult() {
        deleteProfilePicResult.postValue(BackendOperationResult.IDLE);
    }

    public void requestDeleteProfilePic(Integer currentIdPhoto) {
        CloudinaryHelper.deleteImageWithIntegerId(currentIdPhoto, TAG);
        BackendRetrofitService.getInstance()
                .getUserAPI()
                .deleteUserIdPhoto(LoggedUser.getInstance().getUsername(), currentIdPhoto)
                .enqueue(new AlterDataCallback(deleteProfilePicResult, TAG));
    }

    // ------------------------------------------------------------------------------

    public void resetAllData(){
        resetChangePasswordResult();
        resetDeleteAccountResult();
        resetDeleteProfilePicResult();
    }
}
