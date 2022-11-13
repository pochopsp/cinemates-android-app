package it.unina.cinemates.viewmodels.user.movielist.logged;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import it.unina.cinemates.retrofit.backend.enums.BackendOperationResult;
import it.unina.cinemates.retrofit.backend.BackendRetrofitService;
import it.unina.cinemates.retrofit.backend.callbacks.AlterDataWithResponseCallback;

public class AddMovieToMovieListViewModel extends ViewModel {

    private static final String TAG = "ADDMOVIETOMOVIELIST_VIEWMODEL";

    private final MutableLiveData<BackendOperationResult> insertMovieInMovielistResult = new MutableLiveData<>(BackendOperationResult.IDLE);

    public void addToMovieList(int movieListId, int movieToBeAddedId) {
        BackendRetrofitService
                .getInstance()
                .getMovieListAPI()
                .insertMovieInMovieList(movieListId,movieToBeAddedId)
                .enqueue(new AlterDataWithResponseCallback<>(insertMovieInMovielistResult, TAG));
    }

    public LiveData<BackendOperationResult> getInsertMovieInMovieListResult() {
        return insertMovieInMovielistResult;
    }



    public void resetInsertMovieInMovielistResult(){
        insertMovieInMovielistResult.postValue(BackendOperationResult.IDLE);
    }

    public void setServerUnreachableForInsertMovieInMovielistResult() {
        insertMovieInMovielistResult.postValue(BackendOperationResult.SERVER_UNREACHABLE);
    }
}
