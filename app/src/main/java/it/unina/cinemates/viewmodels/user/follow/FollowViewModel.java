package it.unina.cinemates.viewmodels.user.follow;

import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.models.Follow;
import it.unina.cinemates.retrofit.PaginatedResponse;
import it.unina.cinemates.retrofit.backend.BackendRetrofitService;
import it.unina.cinemates.retrofit.backend.callbacks.AlterDataCallback;
import it.unina.cinemates.retrofit.backend.callbacks.AlterDataWithResponseCallback;
import it.unina.cinemates.retrofit.backend.enums.BackendOperationResult;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.views.backend.UserResult;
import it.unina.cinemates.views.backend.enums.FollowPageType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowViewModel extends ViewModel {

    private static final String TAG = "FOLLOW_VIEWMODEL";
    private final List<UserResult> followList = new ArrayList<>();
    private final MutableLiveData<List<UserResult>> followLiveData = new MutableLiveData<>();
    public LiveData<List<UserResult>> getFollowLiveData() { return followLiveData; }

    public void requestMyFollowing(Integer page) {
        BackendRetrofitService.getInstance().getUserAPI().getUserFollowing(LoggedUser.getInstance().getUsername(), page, LoggedUser.getInstance()
                .getUsername()).enqueue(new Callback<PaginatedResponse<List<UserResult>>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<List<UserResult>>> call, Response<PaginatedResponse<List<UserResult>>> response) {
                if (response.isSuccessful()) {
                    if(!followList.containsAll(response.body().getData()))
                         followList.addAll(response.body().getData());
                    followLiveData.setValue(followList);
                }
            }
            @Override
            public void onFailure(Call<PaginatedResponse<List<UserResult>>> call, Throwable t) { }
        });
    }

    public void requestFollowers(String username, Integer page) {
        if(page == 1)
            followList.clear();

        BackendRetrofitService.getInstance().getUserAPI().getUserFollowers(username, page, LoggedUser.getInstance()
                .getUsername()).enqueue(new Callback<PaginatedResponse<List<UserResult>>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<List<UserResult>>> call, Response<PaginatedResponse<List<UserResult>>> response) {
                if (response.isSuccessful()) {
                    if(!followList.containsAll(response.body().getData()))
                        followList.addAll(response.body().getData());
                    followLiveData.setValue(followList);
                }
            }
            @Override
            public void onFailure(Call<PaginatedResponse<List<UserResult>>> call, Throwable t) { }
        });
    }

    public void requestFollowing(String username, Integer page) {
        if(page == 1)
            followList.clear();

        BackendRetrofitService.getInstance().getUserAPI().getUserFollowing(username, page, LoggedUser.getInstance()
                .getUsername()).enqueue(new Callback<PaginatedResponse<List<UserResult>>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<List<UserResult>>> call, Response<PaginatedResponse<List<UserResult>>> response) {
                if (response.isSuccessful()) {
                    if(!followList.containsAll(response.body().getData()))
                        followList.addAll(response.body().getData());
                    followLiveData.setValue(followList);
                }
            }
            @Override
            public void onFailure(Call<PaginatedResponse<List<UserResult>>> call, Throwable t) { }
        });
    }

    public void requestMyFollowers(Integer page) {
        BackendRetrofitService.getInstance().getUserAPI().getUserFollowers(LoggedUser.getInstance().getUsername(), page, LoggedUser.getInstance()
                .getUsername()).enqueue(new Callback<PaginatedResponse<List<UserResult>>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<List<UserResult>>> call, Response<PaginatedResponse<List<UserResult>>> response) {
                if (response.isSuccessful()) {
                    if(!followList.containsAll(response.body().getData()))
                        followList.addAll(response.body().getData());
                    followLiveData.setValue(followList);
                }
            }
            @Override
            public void onFailure(Call<PaginatedResponse<List<UserResult>>> call, Throwable t) { }
        });
    }

    public void deleteMyFollowing(String compositeKey, UserResult user, String otherUsername, FollowPageType followPageType, FragmentActivity activity) {
        BackendRetrofitService.getInstance().getFollowAPI().deleteFollow(compositeKey).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    SnackbarUtils.successSnackbar(activity, activity.getString(R.string.delete_my_following_success)).show();
                    followList.remove(user);
                    deletedFollowLiveData.setValue(true);
                    clearFollowList();
                    if(otherUsername.equals(LoggedUser.getInstance().getUsername()))
                        requestMyFollowing(1);
                    else if(followPageType == FollowPageType.OTHER_USER_FOLLOWING)
                        requestFollowing(otherUsername,1);
                    else if(followPageType == FollowPageType.OTHER_USER_FOLLOWERS)
                        requestFollowers(otherUsername,1);
                } else {
                    SnackbarUtils.failureSnackbar(activity, activity.getString(R.string.snackbar_server_unreachable)).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                SnackbarUtils.failureSnackbar(activity, activity.getString(R.string.snackbar_server_unreachable)).show();
            }
        });
    }

    public void deleteMyFollower(String compositeKey, UserResult user, FragmentActivity activity) {
        BackendRetrofitService.getInstance().getFollowAPI().deleteFollow(compositeKey).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    SnackbarUtils.successSnackbar(activity, activity.getString(R.string.delete_my_follower_success)).show();
                    deletedFollowLiveData.setValue(true);
                    followList.remove(user);
                    clearFollowList();
                    requestMyFollowers(1);
                } else {
                    SnackbarUtils.failureSnackbar(activity, activity.getString(R.string.snackbar_server_unreachable)).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                SnackbarUtils.failureSnackbar(activity, activity.getString(R.string.snackbar_server_unreachable)).show();
            }
        });
    }

    public void clearFollowList() {
        followList.clear();
    }

    private final MutableLiveData<FollowPageType> followPageTypeMutableLiveData = new MutableLiveData<>();

    public LiveData<FollowPageType> getFollowPageTypeLiveData() {
        return followPageTypeMutableLiveData;
    }

    public void setFollowPageType(FollowPageType type) {
        followPageTypeMutableLiveData.setValue(type);
    }

    private final MutableLiveData<Boolean> deletedFollowLiveData = new MutableLiveData<>(false);

    public LiveData<Boolean> getDeletedFollowLiveData() {
        return deletedFollowLiveData;
    }



    //--------------------------------- POST FOLLOW -----------------------------------

    private MutableLiveData<BackendOperationResult> postFollowResult = new MutableLiveData<>(BackendOperationResult.IDLE);
    public LiveData<BackendOperationResult> getPostFollowResult() {return  postFollowResult;}
    public void resetPostFollowResult() {postFollowResult.postValue(BackendOperationResult.IDLE);}

    public void requestPostFollow(String followerUsername, String followingUsername) {
        Follow follow = new Follow();
        follow.setFollowerId(followerUsername);
        follow.setFollowingId(followingUsername);
        BackendRetrofitService.getInstance().getFollowAPI().postFollow(follow).enqueue(new AlterDataWithResponseCallback<>(postFollowResult, TAG));
    }

    //--------------------------------- DELETE FOLLOW -----------------------------------

    private MutableLiveData<BackendOperationResult> deleteFollowResult = new MutableLiveData<>(BackendOperationResult.IDLE);
    public LiveData<BackendOperationResult> getDeleteFollowResult() {return  deleteFollowResult;}
    public void resetDeleteFollowResult() {deleteFollowResult.postValue(BackendOperationResult.IDLE);}

    public void requestDeleteFollow(String followerUsername, String followingUsername) {
        String compositeKey = followerUsername + "-" + followingUsername;
        BackendRetrofitService.getInstance().getFollowAPI().deleteFollow(compositeKey).enqueue(new AlterDataCallback(deleteFollowResult,TAG));
    }
}
