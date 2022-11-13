package it.unina.cinemates.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.models.Follow;
import it.unina.cinemates.models.Notification;
import it.unina.cinemates.retrofit.NonPaginatedResponse;
import it.unina.cinemates.retrofit.PaginatedResponse;
import it.unina.cinemates.retrofit.backend.BackendRetrofitService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationViewModel extends ViewModel {

    private final MutableLiveData<List<Notification>> allNotificationLiveData = new MutableLiveData<>();

    private final MutableLiveData<Notification> clickedNotificationLiveData = new MutableLiveData<>();

    private final MutableLiveData<Boolean> newNotificationsLiveData = new MutableLiveData<>(false);

    public LiveData<Boolean> getNewNotificationsLiveData(){
        return  newNotificationsLiveData;
    }

    private final List<Notification> notificationList = new ArrayList<>();

    public LiveData<List<Notification>> getAllNotificationLiveData(){
        return allNotificationLiveData;
    }

    public void setAllNotificationLiveData(List<Notification> notifications){
        allNotificationLiveData.setValue(notifications);
    }

    public LiveData<Notification> getClickedNotificationLiveData(){
        return clickedNotificationLiveData;
    }

    public void setClickedNotificationLiveData(Notification notification){
        clickedNotificationLiveData.setValue(notification);
    }

    public void requestNotification(Integer page){

        BackendRetrofitService.getInstance().getUserAPI().getUserNotifications(LoggedUser.getInstance().getUsername(), page)
                .enqueue(new retrofit2.Callback<PaginatedResponse<List<Notification>>>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedResponse<List<Notification>>> call, @NonNull Response<PaginatedResponse<List<Notification>>> response) {
                if(!response.isSuccessful()) {
                    setNoNotificationLiveData(true);
                    setAllNotificationLiveData(null);
                    return;
                }
                assert response.body() != null;
                if (response.body().getData().size() != 0) {
                    setNoNotificationLiveData(false);
                    if(!notificationList.containsAll(response.body().getData()))
                        notificationList.addAll(response.body().getData());
                }else if(notificationList.size() == 0)
                    setNoNotificationLiveData(true);
                setAllNotificationLiveData(notificationList);
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedResponse<List<Notification>>> call, @NonNull Throwable t) {
                setAllNotificationLiveData(null);
                setNoNotificationLiveData(true);
            }
        });
    }

    public void putNotificationRead(Notification notification){
        notification.setRead(true);
        BackendRetrofitService.getInstance().getNotificationAPI().putNotification(notification.getId(),notification).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });

    }

    public void deleteNotificationAndFollow(Notification notification, String followCompositeKey){
        BackendRetrofitService.getInstance().getFollowAPI().deleteFollow(followCompositeKey).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                Log.e("delete follow",String.valueOf(response.code()));
                //Delete notification only if the other call succeed
                BackendRetrofitService.getInstance().getNotificationAPI().deleteNotification(notification.getId()).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        Log.e("delete notification",String.valueOf(response.code()));
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {

                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {

            }
        });
    }

    public void deleteNotificationAndPutFollow(Notification notification, String followCompositeKey,Follow follow){
        BackendRetrofitService.getInstance().getFollowAPI().putFollow(followCompositeKey, follow).enqueue(new Callback<NonPaginatedResponse<Follow>>() {
            @Override
            public void onResponse(@NonNull Call<NonPaginatedResponse<Follow>> call, @NonNull Response<NonPaginatedResponse<Follow>> response) {
                Log.e("Put follow",String.valueOf(response.code()));
                //Delete notification only if the other call succeed
                BackendRetrofitService.getInstance().getNotificationAPI().deleteNotification(notification.getId()).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        Log.e("delete notification",String.valueOf(response.code()));
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {

                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<NonPaginatedResponse<Follow>> call, @NonNull Throwable t) {

            }
        });
    }

    public void checkNewNotifications(){
        BackendRetrofitService.getInstance().getUserAPI().getUserNotifications(LoggedUser.getInstance().getUsername(), 1).enqueue(new Callback<PaginatedResponse<List<Notification>>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<List<Notification>>> call, Response<PaginatedResponse<List<Notification>>> response) {
                if(!response.isSuccessful()) {
                    newNotificationsLiveData.setValue(false);
                    return;
                }

                for(Notification notification: response.body().getData()){
                    if(!notification.getRead()){
                        newNotificationsLiveData.setValue(true);
                        return;
                    }
                }

                newNotificationsLiveData.setValue(false);
            }

            @Override
            public void onFailure(Call<PaginatedResponse<List<Notification>>> call, Throwable t) {
                newNotificationsLiveData.setValue(false);
            }
        });
    }

    private final MutableLiveData<Boolean> noNotificationLiveData = new MutableLiveData<>();
    public LiveData<Boolean> getNoNotificationLiveData(){
        return noNotificationLiveData;
    }
    public void setNoNotificationLiveData(Boolean noNotifications){
        noNotificationLiveData.setValue(noNotifications);
    }

    public void resetAllNotifications(){
        this.notificationList.clear();
    }

}
