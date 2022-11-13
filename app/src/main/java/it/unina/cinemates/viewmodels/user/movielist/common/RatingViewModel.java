package it.unina.cinemates.viewmodels.user.movielist.common;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;

import java.util.List;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.models.Notification;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.ui.user.movielist.common.rating.RatingActions;
import it.unina.cinemates.views.backend.MovieListFull;
import it.unina.cinemates.views.backend.Rating;
import it.unina.cinemates.retrofit.PaginatedResponse;
import it.unina.cinemates.retrofit.backend.BackendRetrofitService;
import it.unina.cinemates.views.backend.MovieListBasic;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RatingViewModel extends ViewModel {

    private final MutableLiveData<List<Rating>> ratingsLiveData = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> movieListTotalRatingsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Float> averageRatingLiveData = new MutableLiveData<>();

    public LiveData<Float> getAverageRatingLiveData(){
        return averageRatingLiveData;
    }

    public LiveData<Integer> getMovieListTotalRatingsLiveData(){
        return movieListTotalRatingsLiveData;
    }

    public LiveData<List<Rating>> getRatingLiveData(){
        return ratingsLiveData;
    }

    public void setRatingsLiveData(List<Rating> ratings){
        ratingsLiveData.setValue(ratings);
    }


    public void requestMovieListRatings(Integer movieListId, Integer page) {
        BackendRetrofitService.getInstance().getMovieListAPI().getMovieListRatings(movieListId, page).enqueue(
                new Callback<PaginatedResponse<List<Rating>>>() {
                    @Override
                    public void onResponse(Call<PaginatedResponse<List<Rating>>> call, Response<PaginatedResponse<List<Rating>>> response) {
                        if (response.isSuccessful()) {
                            movieListTotalRatingsLiveData.setValue(response.body().getTotalRecords());
                            setRatingsLiveData(response.body().getData());
                            refreshAverageRating(movieListId);

                            if(response.body().getTotalRecords() == 0)
                                foundRatingsLiveData.setValue(false);
                            else
                                foundRatingsLiveData.setValue(true);

                        } else {
                            setRatingsLiveData(null);
                            foundRatingsLiveData.setValue(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<PaginatedResponse<List<Rating>>> call, Throwable t) {
                        setRatingsLiveData(null);
                        foundRatingsLiveData.setValue(false);
                    }
                });
    }

    public void requestRatingByNotification(Notification notification, NavController navController, FragmentActivity activity){
        BackendRetrofitService.getInstance().getRatingApi().getRating(notification.getContentId()).enqueue(new Callback<Rating>() {
            @Override
            public void onResponse(Call<Rating> call, Response<Rating> response) {
                if(response.body().getId() == null){
                    SnackbarUtils.failureSnackbar(activity, activity.getString(R.string.content_removed)).show();
                    return;
                }
                BackendRetrofitService.getInstance().getMovieListAPI().getMovieListBasic(response.body().getRatedListId()).enqueue(new Callback<MovieListBasic>() {
                    @Override
                    public void onResponse(Call<MovieListBasic> call, Response<MovieListBasic> response) {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("movieList",response.body());
                        averageRatingLiveData.setValue(response.body().getAverageRating());
                        navController.navigate(R.id.navigation_rating,bundle);
                    }

                    @Override
                    public void onFailure(Call<MovieListBasic> call, Throwable t) {
                        SnackbarUtils.failureSnackbar(activity,activity.getString(R.string.snackbar_server_unreachable));
                    }
                });
            }

            @Override
            public void onFailure(Call<Rating> call, Throwable t) {
                SnackbarUtils.failureSnackbar(activity,activity.getString(R.string.snackbar_server_unreachable));
            }
        });
    }

    public void requestPostRating(it.unina.cinemates.models.Rating rating){
        BackendRetrofitService.getInstance().getRatingApi().postRating(rating).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    ratingActionsMutableLiveData.setValue(RatingActions.RATING_POST_SUCCESS);
                }else
                    ratingActionsMutableLiveData.setValue(RatingActions.RATING_FAILURE);

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                ratingActionsMutableLiveData.setValue(RatingActions.RATING_FAILURE);
            }
        });
    }

    public void requestPutRating(it.unina.cinemates.models.Rating rating){
        BackendRetrofitService.getInstance().getRatingApi().putRating(rating).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    ratingActionsMutableLiveData.setValue(RatingActions.RATING_PUT_SUCCESS);
                }else
                    ratingActionsMutableLiveData.setValue(RatingActions.RATING_FAILURE);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                ratingActionsMutableLiveData.setValue(RatingActions.RATING_FAILURE);
            }
        });
    }

    public void requestDeleteRating(Integer id){
        BackendRetrofitService.getInstance().getRatingApi().deleteRating(id).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                   ratingActionsMutableLiveData.setValue(RatingActions.RATING_DELETE_SUCCESS);
                }else
                    ratingActionsMutableLiveData.setValue(RatingActions.RATING_FAILURE);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                ratingActionsMutableLiveData.setValue(RatingActions.RATING_FAILURE);
            }
        });
    }

    private final MutableLiveData<RatingActions> ratingActionsMutableLiveData = new MutableLiveData<>(RatingActions.IDLE);

    public void setRatingsActionLiveData(RatingActions action){
        ratingActionsMutableLiveData.setValue(action);
    }

    public LiveData<RatingActions> getRatingActionsLiveData() {
        return ratingActionsMutableLiveData;
    }

    private final MutableLiveData<Boolean> foundRatingsLiveData = new MutableLiveData<>();

    public LiveData<Boolean> getFoundRatingsLiveData(){
        return foundRatingsLiveData;
    }

    public void setFoundRatingsLiveData(Boolean found){
         foundRatingsLiveData.setValue(found);
    }

    private void refreshAverageRating(int ratedListId){
       BackendRetrofitService.getInstance().getMovieListAPI().getMovieListFull(ratedListId, LoggedUser.getInstance().getUsername()).enqueue(new Callback<MovieListFull>() {
           @Override
           public void onResponse(Call<MovieListFull> call, Response<MovieListFull> response) {
               if(response.isSuccessful())
                   averageRatingLiveData.setValue(response.body().getAverageRating());
               else
                   averageRatingLiveData.setValue(0f);
           }

           @Override
           public void onFailure(Call<MovieListFull> call, Throwable t) {
               averageRatingLiveData.setValue(0f);
           }
       });
    }


    public void resetRatingList(){
        ratingsLiveData.setValue(null);
    }
}
