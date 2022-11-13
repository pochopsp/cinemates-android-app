package it.unina.cinemates.viewmodels;

import android.annotation.SuppressLint;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import it.unina.cinemates.retrofit.PaginatedResponse;
import it.unina.cinemates.retrofit.backend.BackendRetrofitService;
import it.unina.cinemates.retrofit.tmdb.TmDbRetrofitService;
import it.unina.cinemates.retrofit.tmdb.jsonwrappers.actor.ActorResultWrapper;
import it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.MovieResultWrapper;
import it.unina.cinemates.ui.search.SearchParameters;
import it.unina.cinemates.viewmodels.enums.SearchType;
import it.unina.cinemates.views.backend.UserResult;
import it.unina.cinemates.views.tmdb.MovieResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchViewModel extends ViewModel {

    private final MutableLiveData<SearchType> searchTypeLiveData = new MutableLiveData<>(SearchType.MOVIES);

    private final MutableLiveData<SearchParameters> searchedLiveData = new MutableLiveData<>(new SearchParameters(null,false,null));

    private final MutableLiveData<List<UserResult>> userSearchLiveData = new MutableLiveData<>();

    private final List<MovieResult> movieResults = new ArrayList<>();
    private final MutableLiveData<List<MovieResult>> movieSearchLiveData = new MutableLiveData<>(movieResults);

    private final MutableLiveData<Parcelable> movieRecyclerState = new MutableLiveData<>(null);

    public Parcelable getMovieRecyclerState(){
        return movieRecyclerState.getValue();
    }

    private final MutableLiveData<Parcelable> userRecyclerState = new MutableLiveData<>(null);

    public Parcelable getUserRecyclerState(){
        return userRecyclerState.getValue();
    }

    public void setMovieRecyclerState(Parcelable state){
        movieRecyclerState.setValue(state);
    }

    public void setUserRecyclerState(Parcelable state){
       userRecyclerState.setValue(state);
    }

    public LiveData<SearchType> getSearchTypeLiveData() {
        return searchTypeLiveData;
    }

    public void setSearchTypeLiveData(SearchType searchType) {
        this.searchTypeLiveData.setValue(searchType);
    }

    public LiveData<SearchParameters> getSearchedTextLiveData() {
        return searchedLiveData;
    }

    public void setSearchedTextLiveData(SearchParameters parameter) {
        searchedLiveData.setValue(parameter);
    }

    public LiveData<List<UserResult>> getUserSearchLiveData() {
        return userSearchLiveData;
    }

    public void setUserSearchLiveData(List<UserResult> userResults) {
        userSearchLiveData.setValue(userResults);
    }

    public LiveData<List<MovieResult>> getMovieSearchLiveData() {
        return movieSearchLiveData;
    }

    public void setMovieSearchLiveData(List<MovieResult> movieResults) {
        movieSearchLiveData.setValue(movieResults);
    }

    public MutableLiveData<Boolean> searchFoundMoviesLiveData = new MutableLiveData<>();
    public LiveData<Boolean> getSearchFoundMoviesLiveData(){
        return searchFoundMoviesLiveData;
    }


    public MutableLiveData<Boolean> searchFoundUsersLiveData = new MutableLiveData<>();
    public LiveData<Boolean> getSearchFoundUsersLiveData(){
        return searchFoundUsersLiveData;
    }

    public void requestSearchUser(String searchedUser, Integer page, String requester) {
        BackendRetrofitService.getInstance().getUserAPI().searchUser(searchedUser, page, requester).enqueue(
                new Callback<PaginatedResponse<List<UserResult>>>() {
                    @Override
                    public void onResponse(@NonNull Call<PaginatedResponse<List<UserResult>>> call, @NonNull Response<PaginatedResponse<List<UserResult>>> response) {
                        if(response.isSuccessful()) {
                            assert response.body() != null;
                            if (response.body().getData() != null && response.body().getData().size() == 0) {
                                searchFoundUsersLiveData.postValue(false);
                                setUserSearchLiveData(null);
                            } else {
                                searchFoundUsersLiveData.postValue(true);
                                setUserSearchLiveData(response.body().getData());
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<PaginatedResponse<List<UserResult>>> call, @NonNull Throwable t) {
                        setUserSearchLiveData(null);
                        searchFoundUsersLiveData.postValue(false);
                    }
                });
    }

    public void requestSearchMovieNoFilter(SearchParameters s, Integer page) {
        TmDbRetrofitService.getTmDbDaoInstance().searchMoviesByTitle(s.getSearchedText(), page).enqueue(
                new Callback<MovieResultWrapper>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieResultWrapper> call, @NonNull Response<MovieResultWrapper> response) {
                        if(response.isSuccessful()) {
                            assert response.body() != null;
                            if (response.body().getMovieResults() != null) {
                                if (!movieResults.containsAll(response.body().getMovieResults()))
                                    movieResults.addAll(response.body().getMovieResults());
                                setMovieSearchLiveData(movieResults);

                                if (movieResults.size() == 0)
                                    searchFoundMoviesLiveData.postValue(false);
                                else
                                    searchFoundMoviesLiveData.postValue(true);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MovieResultWrapper> call, @NonNull Throwable t) {
                        searchFoundMoviesLiveData.postValue(false);
                        setMovieSearchLiveData(null);
                    }
                });
    }

    public void requestSearchMovieByYear(SearchParameters s, Integer page) {
        TmDbRetrofitService.getTmDbDaoInstance().searchMoviesByTitleAndYear(s.getSearchedText(), page, Integer.parseInt(s.getYear())).enqueue(
                new Callback<MovieResultWrapper>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onResponse(@NonNull Call<MovieResultWrapper> call, @NonNull Response<MovieResultWrapper> response) {
                        assert response.body() != null;
                        if(response.body().getMovieResults() != null){
                            if(!movieResults.containsAll(response.body().getMovieResults()))
                                movieResults.addAll(response.body().getMovieResults());
                            setMovieSearchLiveData(movieResults);

                            if(movieResults.size() == 0)
                                searchFoundMoviesLiveData.postValue(false);
                            else
                                searchFoundMoviesLiveData.postValue(true);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MovieResultWrapper> call, @NonNull Throwable t) {
                        searchFoundMoviesLiveData.postValue(false);
                        setMovieSearchLiveData(null);
                    }
                });
    }

    public void requestSearchByActor(SearchParameters s, Integer page) {
        TmDbRetrofitService.getTmDbDaoInstance().searchActorsByName(s.getSearchedText()).enqueue(
                new Callback<ActorResultWrapper>() {
                    @Override
                    public void onResponse(@NonNull Call<ActorResultWrapper> call, @NonNull Response<ActorResultWrapper> response) {
                        if (response.body() != null && response.body().getActorResult().size() != 0) {
                            int actorId = response.body().getActorResult().get(0).getId();
                            TmDbRetrofitService.getTmDbDaoInstance().searchMoviesByActor(actorId, page).enqueue(
                                    new Callback<MovieResultWrapper>() {
                                        @SuppressLint("NotifyDataSetChanged")
                                        @Override
                                        public void onResponse(@NonNull Call<MovieResultWrapper> call, @NonNull Response<MovieResultWrapper> response) {
                                            assert response.body() != null;
                                            if(response.body().getMovieResults() != null){
                                                if(!movieResults.containsAll(response.body().getMovieResults()))
                                                    movieResults.addAll(response.body().getMovieResults());
                                                setMovieSearchLiveData(movieResults);

                                                if(movieResults.size() == 0)
                                                    searchFoundMoviesLiveData.postValue(false);
                                                else
                                                    searchFoundMoviesLiveData.postValue(true);
                                            }
                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<MovieResultWrapper> call, @NonNull Throwable t) {
                                            searchFoundMoviesLiveData.postValue(false);
                                            setMovieSearchLiveData(null);
                                        }
                                    });
                        }else{
                            searchFoundMoviesLiveData.postValue(false);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ActorResultWrapper> call, @NonNull Throwable t) {
                        setMovieSearchLiveData(null);
                        searchFoundMoviesLiveData.postValue(false);
                    }
                });
    }

    public void requestSearchByActorAndYear(SearchParameters s, Integer page) {
        TmDbRetrofitService.getTmDbDaoInstance().searchActorsByName(s.getSearchedText()).enqueue(
                new Callback<ActorResultWrapper>() {
                    @Override
                    public void onResponse(@NonNull Call<ActorResultWrapper> call, @NonNull Response<ActorResultWrapper> response) {
                        if (response.body() != null && response.body().getActorResult().size() != 0) {
                            int actorId = response.body().getActorResult().get(0).getId();
                            TmDbRetrofitService.getTmDbDaoInstance().searchMoviesByTitleAndYearAndActor(page,Integer.parseInt(s.getYear()),actorId).enqueue(
                                    new Callback<MovieResultWrapper>() {
                                        @SuppressLint("NotifyDataSetChanged")
                                        @Override
                                        public void onResponse(@NonNull Call<MovieResultWrapper> call, @NonNull Response<MovieResultWrapper> response) {
                                            assert response.body() != null;
                                            if(response.body().getMovieResults() != null){
                                                if(!movieResults.containsAll(response.body().getMovieResults()))
                                                    movieResults.addAll(response.body().getMovieResults());
                                                setMovieSearchLiveData(movieResults);
                                                
                                            }
                                            if(movieResults.size() == 0)
                                                searchFoundMoviesLiveData.postValue(false);
                                            else
                                                searchFoundMoviesLiveData.postValue(true);

                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<MovieResultWrapper> call, @NonNull Throwable t) {
                                            setMovieSearchLiveData(null);
                                            searchFoundMoviesLiveData.postValue(false);
                                        }
                                    });
                        }else{
                            searchFoundMoviesLiveData.postValue(false);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ActorResultWrapper> call, @NonNull Throwable t) {
                        setMovieSearchLiveData(null);
                    }
                });
    }

    private final MutableLiveData<Boolean> searchByActorLiveData = new MutableLiveData<>(false);
    public LiveData<Boolean> getSearchByActorLiveData(){
        return searchByActorLiveData;
    }
    public void setSearchedByActorLiveData(Boolean byActor){
        searchByActorLiveData.setValue(byActor);
    }

    private final MutableLiveData<String> searchByYearLiveData = new MutableLiveData<>(null);
    public LiveData<String> getSearchByYearLiveData(){
        return searchByYearLiveData;
    }
    public void setSearchByYearLiveData(String year){
        searchByYearLiveData.setValue(year);
    }

    private final MutableLiveData<Boolean> isNewSearchLiveData = new MutableLiveData<>(null);
    public LiveData<Boolean> getIsNewSearchLiveData(){
        return isNewSearchLiveData;
    }
    public void setIsNewSearchLiveData(Boolean isNewSearch){
        isNewSearchLiveData.setValue(isNewSearch);
    }

    public void resetMovieSearchList(){
        movieResults.clear();
    }

}
