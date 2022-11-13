package it.unina.cinemates.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import it.unina.cinemates.retrofit.PaginatedResponse;
import it.unina.cinemates.retrofit.backend.BackendRetrofitService;
import it.unina.cinemates.retrofit.tmdb.TmDbRetrofitService;
import it.unina.cinemates.viewmodels.user.customMovielists.CustomListsViewModel;
import it.unina.cinemates.views.backend.DiscoverListsElement;
import it.unina.cinemates.views.tmdb.MoviePoster;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiscoverListsViewModel extends ViewModel {

    private static final String TAG = "DISCOVERLISTS_VIEWMODEL";
    private MutableLiveData<CustomListsViewModel.ListsRetrieveResult> retrieveResult = new MutableLiveData<>(CustomListsViewModel.ListsRetrieveResult.IDLE);
    private int currentRequestedPage;

    private MutableLiveData<Boolean> serverError = new MutableLiveData<>(null);
    public LiveData<Boolean> getServerError() { return serverError; }
    public void resetServerError() { serverError.postValue(null); }

    public LiveData<CustomListsViewModel.ListsRetrieveResult> getRetrieveResult(){ return retrieveResult; }

    private class RetrieveDiscoverListsElementsCallback implements Callback<PaginatedResponse<List<DiscoverListsElement>>> {

        @Override
        public void onResponse(Call<PaginatedResponse<List<DiscoverListsElement>>> call, Response<PaginatedResponse<List<DiscoverListsElement>>> response) {
            if (response.code() == 200) {
                List<DiscoverListsElement> discoverListsElements = response.body().getData();
                discoverListsElements.forEach(discoverListsElement -> {

                    if (!discoverListsElement.getList1().getMoviesInList().isEmpty()) {
                        List<MoviePoster> moviePosters = TmDbRetrofitService.fetchMoviePosters(discoverListsElement.getList1().getMoviesInList());
                        List<String> posterPaths = moviePosters.stream().map(MoviePoster::getPosterPath).collect(Collectors.toList());
                        discoverListsElement.getList1().setMoviePosterPaths(posterPaths);
                    }

                    if (!discoverListsElement.getList2().getMoviesInList().isEmpty()) {
                        List<MoviePoster> moviePosters = TmDbRetrofitService.fetchMoviePosters(discoverListsElement.getList2().getMoviesInList());
                        List<String> posterPaths = moviePosters.stream().map(MoviePoster::getPosterPath).collect(Collectors.toList());
                        discoverListsElement.getList2().setMoviePosterPaths(posterPaths);
                    }
                });

                if (!discoverListsElements.isEmpty()) {
                    currentCategoryValueElementsRetrievedSoFar.addAll(discoverListsElements);
                    DiscoverListsViewModel.this.discoverListsElements.postValue(currentCategoryValueElementsRetrievedSoFar);
                }else  if(currentRequestedPage == 1)
                    DiscoverListsViewModel.this.retrieveResult.postValue(CustomListsViewModel.ListsRetrieveResult.NO_LISTS_AT_ALL);

            } else
                Log.e(TAG, "onResponse: " + response.message());
        }

        @Override
        public void onFailure(Call<PaginatedResponse<List<DiscoverListsElement>>> call, Throwable t) {
            //Log.e(TAG, "onFailure: " + t.getLocalizedMessage());
            serverError.postValue(true);
        }
    }

    private RetrieveDiscoverListsElementsCallback retrieveDiscoverListsElementsCallback = new RetrieveDiscoverListsElementsCallback();

    private MutableLiveData<List<DiscoverListsElement>> discoverListsElements = new MutableLiveData<>(null);
    public LiveData<List<DiscoverListsElement>> getDiscoverListsElements() { return discoverListsElements; }

    List<DiscoverListsElement> currentCategoryValueElementsRetrievedSoFar = new ArrayList<>();

    public void fetchDiscoverListsElements(String username, String category, int pageNumber) {
        this.currentRequestedPage = pageNumber;
        BackendRetrofitService.getInstance().getUserAPI().getUserFollowingMovieLists(username, category, pageNumber).enqueue(retrieveDiscoverListsElementsCallback);
    }

    public void resetDiscoverListsElementsFetch() {
        currentCategoryValueElementsRetrievedSoFar.clear();
        retrieveResult.postValue(CustomListsViewModel.ListsRetrieveResult.IDLE);
    }
}
