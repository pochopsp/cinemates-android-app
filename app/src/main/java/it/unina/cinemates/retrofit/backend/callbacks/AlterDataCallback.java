package it.unina.cinemates.retrofit.backend.callbacks;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;

import it.unina.cinemates.retrofit.backend.enums.BackendOperationResult;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlterDataCallback implements Callback<ResponseBody> {

    private final String TAG;
    private final MutableLiveData<BackendOperationResult> operationResult;

    public AlterDataCallback(MutableLiveData<BackendOperationResult> operationResult, String TAG){
        this.operationResult = operationResult;
        this.TAG = TAG;
    }

    @Override
    public void onResponse(@NonNull Call<ResponseBody> call, Response<ResponseBody> response) {
        if(response.code() == 200){
            operationResult.postValue(BackendOperationResult.SUCCESS);
        }
        else {
            try {
                assert response.errorBody() != null;
                if(response.errorBody().string().contains("The element is already present in the database."))
                    operationResult.postValue(BackendOperationResult.ALREADY_PRESENT);
                else
                    Log.e(TAG,"onResponse: Response code for the request is " + response.code());
            } catch (IOException e) {
                Log.e(TAG,"onResponse: " + e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void onFailure(@NonNull Call<ResponseBody> call, Throwable t) {
        Log.e(TAG,"onFailure: " + t.getLocalizedMessage());
        operationResult.postValue(BackendOperationResult.SERVER_UNREACHABLE);
    }
}