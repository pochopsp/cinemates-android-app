package it.unina.cinemates.viewmodels.user.customMovielists;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import it.unina.cinemates.retrofit.PaginatedResponse;
import it.unina.cinemates.retrofit.backend.callbacks.AlterDataCallback;
import it.unina.cinemates.retrofit.backend.enums.BackendOperationResult;
import it.unina.cinemates.retrofit.backend.BackendRetrofitService;
import it.unina.cinemates.retrofit.backend.callbacks.AlterDataWithResponseCallback;
import it.unina.cinemates.retrofit.backend.jsonwrappers.post.MovieListPostJsonWrapper;
import it.unina.cinemates.retrofit.tmdb.TmDbRetrofitService;
import it.unina.cinemates.views.backend.MovieListPreview;
import it.unina.cinemates.views.tmdb.MoviePoster;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomListsViewModel extends ViewModel {

    private static final String TAG = "LOGGEDUSEROTHERLISTS_VIEWMODEL";

    // --------------------------------------- RETRIEVE SEARCHED LISTS SECTION -----------------------------------------------

    private class RetrieveSearchedMovieListsCallback implements Callback<PaginatedResponse<List<MovieListPreview>>> {

        @Override
        public void onResponse(@NonNull Call<PaginatedResponse<List<MovieListPreview>>> call, Response<PaginatedResponse<List<MovieListPreview>>> response) {
            if (response.code() == 200) {
                assert response.body() != null;
                List<MovieListPreview> movieListPreviews = response.body().getData();
                movieListPreviews.forEach(movieListPreview -> {

                    if (!movieListPreview.getMoviesInList().isEmpty()) {
                        List<MoviePoster> moviePosters = TmDbRetrofitService.fetchMoviePosters(movieListPreview.getMoviesInList());
                        List<String> posterPaths = moviePosters.stream().map(MoviePoster::getPosterPath).collect(Collectors.toList());
                        movieListPreview.setMoviePosterPaths(posterPaths);
                    }
                });

                  /*in this way if user was scrolling and no other lists were found, we stop. But if the user wrote a new query string
                  and no lists were found with that query string, we update the value in livedata. */
                if (!movieListPreviews.isEmpty() || userWroteNewQueryString) {
                    Log.e(TAG, "recuperate " + movieListPreviews.size() + " liste");
                    currentQueryMovieListsRetrievedSoFar.addAll(movieListPreviews);
                    customMovieLists.postValue(currentQueryMovieListsRetrievedSoFar);
                    userWroteNewQueryString = false;
                    if(movieListPreviews.isEmpty())
                        listsRetrieveResult.postValue(ListsRetrieveResult.NO_LISTS_IN_SEARCH);
                }else if(requestedPage == 1)
                         listsRetrieveResult.postValue(ListsRetrieveResult.NO_LISTS_AT_ALL);

            } else
                Log.e(TAG, response.message());
        }

        @Override
        public void onFailure(@NonNull Call<PaginatedResponse<List<MovieListPreview>>> call, Throwable t) {
            Log.e(TAG, t.getLocalizedMessage());
        }
    }


    public enum ListsRetrieveResult {
        NO_LISTS_AT_ALL,
        NO_LISTS_IN_SEARCH,
        IDLE
    }

    private final MutableLiveData<ListsRetrieveResult> listsRetrieveResult = new MutableLiveData<>(ListsRetrieveResult.IDLE);
    public LiveData<ListsRetrieveResult> getListsRetrieveResult() { return listsRetrieveResult; }
    public void resetNoListsRetrieved(){ listsRetrieveResult.postValue(ListsRetrieveResult.IDLE); }

    private final RetrieveSearchedMovieListsCallback retrieveSearchedMovieListsCallback = new RetrieveSearchedMovieListsCallback();

    private final MutableLiveData<List<MovieListPreview>> customMovieLists = new MutableLiveData<>(null);
    public LiveData<List<MovieListPreview>> getCustomMovieLists() {
        return customMovieLists;
    }

    private volatile boolean userWroteNewQueryString = false;
    List<MovieListPreview> currentQueryMovieListsRetrievedSoFar = new ArrayList<>();

    private int requestedPage;
    public void fetchMovieLists(String username, String query, int page) {
        requestedPage = page;
        BackendRetrofitService.getInstance().getMovieListAPI().searchMovieListByName(username, query, page).enqueue(retrieveSearchedMovieListsCallback);
    }

    public void resetMovieListsFetch(boolean userWroteNewQueryString) {
        currentQueryMovieListsRetrievedSoFar.clear();
        this.userWroteNewQueryString = userWroteNewQueryString;
        requestedPage = 0;
    }
    
    // --------------------------------------- CREATE NEW LIST SECTION -----------------------------------------------

    private final MutableLiveData<BackendOperationResult> createMovieListResult = new MutableLiveData<>(BackendOperationResult.IDLE);
    public MutableLiveData<BackendOperationResult> getCreateMovieListResult() {
        return createMovieListResult;
    }

    public void resetCreateMovieListStatus() {
        createMovieListResult.postValue(BackendOperationResult.IDLE);
    }


    public MovieListPreview getLastCreatedList() {
        MovieListPreview ret = lastCreatedList.get(0);
        lastCreatedList.clear();
        return ret;
    }

    private final List<MovieListPreview> lastCreatedList = new ArrayList<>();

    public void requestCreateMovieList(MovieListPostJsonWrapper movieList) {
        BackendRetrofitService
                .getInstance()
                .getMovieListAPI()
                .postMovieList(movieList)
                .enqueue(new AlterDataWithResponseCallback<>(createMovieListResult, TAG, lastCreatedList));
    }


    // --------------------------------------- DELETE LIST SECTION -----------------------------------------------

    MutableLiveData<BackendOperationResult> deleteMovieListResult = new MutableLiveData<>(BackendOperationResult.IDLE);
    public LiveData<BackendOperationResult> getDeleteMovieListResult(){ return  deleteMovieListResult; }

    public void resetDeleteMovieListResult() { deleteMovieListResult.postValue(BackendOperationResult.IDLE); }

    public void requestDeleteMovieList(Integer id) {
        BackendRetrofitService.getInstance().getMovieListAPI().deleteMovieList(id).enqueue(new AlterDataCallback(deleteMovieListResult, TAG));
    }
}









