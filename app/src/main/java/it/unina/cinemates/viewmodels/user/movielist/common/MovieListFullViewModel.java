package it.unina.cinemates.viewmodels.user.movielist.common;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.models.Movie;
import it.unina.cinemates.models.Reaction;
import it.unina.cinemates.retrofit.PostDataInMutableLiveDataCallback;
import it.unina.cinemates.retrofit.backend.BackendRetrofitService;
import it.unina.cinemates.retrofit.backend.callbacks.AlterDataCallback;
import it.unina.cinemates.retrofit.backend.callbacks.AlterDataWithResponseCallback;
import it.unina.cinemates.retrofit.backend.enums.BackendOperationResult;
import it.unina.cinemates.retrofit.tmdb.TmDbRetrofitService;
import it.unina.cinemates.views.backend.MovieListFull;
import it.unina.cinemates.views.backend.enums.ReactionType;
import it.unina.cinemates.views.tmdb.MoviePoster;

public class MovieListFullViewModel extends ViewModel {

    private static final String TAG = "MOVIELISTFULL_VIEWMODEL";

    private final MutableLiveData<MovieListFull> movieListFull = new MutableLiveData<>();
    public LiveData<MovieListFull> getMovieListFull() {
        return movieListFull;
    }

    public MovieListFullViewModel(){
        super();
        this.reactionHandler = new ReactionHandler();
    }

    public void requestFullMovieList(int movieListId){
        BackendRetrofitService.getInstance()
                .getMovieListAPI()
                .getMovieListFull(movieListId, LoggedUser.getInstance().getUsername())
                .enqueue(new PostDataInMutableLiveDataCallback<>(movieListFull,TAG));
    }

    public void requestMoviePosters(List<Movie> moviesInList) {
        TmDbRetrofitService.fetchMoviePosters(moviesInList, moviesInMovieList);
    }


    private final MutableLiveData<List<MoviePoster>> moviesInMovieList = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<MoviePoster>> getMoviesInMovieList() {
        return moviesInMovieList;
    }



    //------------------------------ DELETE MOVIES FROM LIST ------------------------------

    private final MutableLiveData<BackendOperationResult> deleteMoviesFromListResult = new MutableLiveData<>(BackendOperationResult.IDLE);
    public MutableLiveData<BackendOperationResult> getDeleteMoviesFromListResult() { return deleteMoviesFromListResult; }
    public void resetDeleteFromMovieListResult() { deleteMoviesFromListResult.postValue(BackendOperationResult.IDLE); }

    private ArrayList<Integer> moviesToBeDeletedIds;

    // once requestDeleteMovies is successful, we call this method to update our local list of movies
    public void deleteMoviesFromCurrentList(){
        moviesToBeDeletedIds.forEach(id -> {
            Log.e(TAG, ""+id);
            Objects.requireNonNull(moviesInMovieList.getValue()).removeIf(moviePoster -> moviePoster.getId().equals(id));
        });

        if(moviesInMovieList.getValue().isEmpty())
            Log.e(TAG, "vuotaaaaaaaa");
    }

    public void requestDeleteMovies(int movieListId, Iterable<Long> movieIds) {
        moviesToBeDeletedIds = new ArrayList<>();
        movieIds.forEach(movieId -> moviesToBeDeletedIds.add((int)((long)movieId)));
        String movieIdsString = moviesToBeDeletedIds.stream().map(Object::toString).collect(Collectors.joining(","));
        BackendRetrofitService.getInstance()
                .getMovieListAPI()
                .deleteMoviesFromMovieList(movieListId, movieIdsString)
                .enqueue(new AlterDataCallback(this.deleteMoviesFromListResult, TAG));
    }


    //------------------------------ USER REACTIONS ---------------------------------------

    private final ReactionHandler reactionHandler;
    public ReactionHandler getReactionHandler() { return reactionHandler; }


    public static class ReactionHandler {

        MutableLiveData<BackendOperationResult> reactionOperationResult = new MutableLiveData<>(BackendOperationResult.IDLE);
        public LiveData<BackendOperationResult> getReactionOperationResult(){ return  reactionOperationResult; }

        public void resetReactionOperationResult() { reactionOperationResult.postValue(BackendOperationResult.IDLE); }

        private final List<Reaction> postedReaction = new ArrayList<>();
        public List<Reaction> getPostedReaction(){
            return postedReaction;
        }

        public void postReaction(ReactionType type, int movieListId) {

            Reaction reaction = new Reaction(type, LoggedUser.getInstance().getUsername(), movieListId);

            BackendRetrofitService.getInstance().getReactionApi().postReaction(reaction).enqueue(new AlterDataWithResponseCallback<>(reactionOperationResult, TAG, postedReaction));
        }

        public void putReaction(int reactionId, int movieListId, ReactionType type) {

            Reaction reaction = new Reaction(reactionId, type, LoggedUser.getInstance().getUsername(), movieListId);

            BackendRetrofitService.getInstance().getReactionApi().putReaction(reaction.getId(), reaction).enqueue(new AlterDataWithResponseCallback<>(reactionOperationResult, TAG));
        }

        public void deleteReaction(int reactionId) {
            BackendRetrofitService.getInstance().getReactionApi().deleteReaction(reactionId).enqueue(new AlterDataCallback(reactionOperationResult,TAG));
        }

        public void reset() {
            resetReactionOperationResult();
            postedReaction.clear();
        }
    }

    //------------------------------ RENAME MOVIELIST ---------------------------------------

    MutableLiveData<BackendOperationResult> renameMovieListResult = new MutableLiveData<>(BackendOperationResult.IDLE);
    public LiveData<BackendOperationResult> getRenameMovieListResult(){ return  renameMovieListResult; }

    public void resetRenameMovieListResult() { renameMovieListResult.postValue(BackendOperationResult.IDLE); }

    public void requestRenameMovieList(Integer id, String newName) {
        Log.e(TAG,"renaming list to name: " + newName + "...");
        BackendRetrofitService.getInstance().getMovieListAPI().putMovieListName(id, newName).enqueue(new AlterDataCallback(renameMovieListResult, TAG));
    }

    //-------------------------------------------------------------------------------

    public void resetAllData(){

        movieListFull.postValue(null);

        moviesInMovieList.postValue(new ArrayList<>());

        resetDeleteFromMovieListResult();

        resetRenameMovieListResult();

        reactionHandler.reset();
    }

}
