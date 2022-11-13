package it.unina.cinemates.retrofit;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostDataInMutableLiveDataCallback<T> implements Callback<T> {

    private final MutableLiveData<T> mutableLiveData;
    private final String callingClassTAG;

    public PostDataInMutableLiveDataCallback(MutableLiveData<T> mutableLiveData, String callingClassTAG){
        this.mutableLiveData = mutableLiveData;
        this.callingClassTAG = callingClassTAG;
    }

    @Override
    public void onResponse(@NonNull Call<T> call, Response<T> response) {
        if(response.code() == 200){
            mutableLiveData.postValue(response.body());
        }else
            Log.e(callingClassTAG, "onResponse: " + response.message());
    }

    @Override
    public void onFailure(@NonNull Call<T> call, Throwable t) {
        Log.e(callingClassTAG, "onFailure: " + t.getLocalizedMessage());
    }
}
