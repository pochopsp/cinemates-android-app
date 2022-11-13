package it.unina.cinemates.retrofit.backend.callbacks;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.util.List;

import it.unina.cinemates.retrofit.NonPaginatedResponse;
import it.unina.cinemates.retrofit.backend.enums.BackendOperationResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlterDataWithResponseCallback<T> implements Callback<NonPaginatedResponse<T>> {

    private final String TAG;
    private final MutableLiveData<BackendOperationResult> operationResult;
    private List<T> responseDataContainer;

    public AlterDataWithResponseCallback(MutableLiveData<BackendOperationResult> operationResult, String TAG){
        this.operationResult = operationResult;
        this.TAG = TAG;
    }

    public AlterDataWithResponseCallback(MutableLiveData<BackendOperationResult> operationResult, String TAG, List<T> responseDataContainer){
        this.operationResult = operationResult;
        this.TAG = TAG;
        this.responseDataContainer = responseDataContainer;
    }

    @Override
    public void onResponse(@NonNull Call<NonPaginatedResponse<T>> call, Response<NonPaginatedResponse<T>> response) {
        if(response.code() == 200){
            if(responseDataContainer != null) {
                assert response.body() != null;
                responseDataContainer.add(response.body().getData());
            }
            operationResult.postValue(BackendOperationResult.SUCCESS);
        }
        else {
            try {
                assert response.errorBody() != null;
                if(response.errorBody().string().contains("The element is already present in the database."))
                    operationResult.postValue(BackendOperationResult.ALREADY_PRESENT);
                else
                    Log.e(TAG," " + response.code());
            } catch (IOException e) {
                Log.e(TAG,e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void onFailure(@NonNull Call<NonPaginatedResponse<T>> call, Throwable t) {
        Log.e(TAG,t.getLocalizedMessage());
        operationResult.postValue(BackendOperationResult.SERVER_UNREACHABLE);
    }
}