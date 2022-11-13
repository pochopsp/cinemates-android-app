package it.unina.cinemates.viewmodels.user.movielist.common;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;

import java.util.ArrayList;
import java.util.List;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.models.Notification;
import it.unina.cinemates.models.Report;
import it.unina.cinemates.retrofit.NonPaginatedResponse;
import it.unina.cinemates.retrofit.PaginatedResponse;
import it.unina.cinemates.retrofit.backend.BackendRetrofitService;
import it.unina.cinemates.retrofit.backend.jsonwrappers.post.CommentPostJsonWrapper;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.viewmodels.enums.CommentAction;
import it.unina.cinemates.views.backend.BasicUser;
import it.unina.cinemates.views.backend.Comment;
import it.unina.cinemates.views.backend.MovieListBasic;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentViewModel extends ViewModel {

    private final MutableLiveData<List<Comment>> movieListCommentsLiveData = new MutableLiveData<>();
    private final List<Comment> comments = new ArrayList<>();
    private final MutableLiveData<Boolean> deletedCommentLiveData = new MutableLiveData<>(false);

    public LiveData<Boolean> getDeletedCommentLiveData(){
        return deletedCommentLiveData;
    }

    public LiveData<List<Comment>> getMovieListCommentsLiveData() {
        return movieListCommentsLiveData;
    }

    public void setMovieListCommentsLiveData(List<Comment> comments) {
        this.movieListCommentsLiveData.setValue(comments);
    }

    private final MutableLiveData<Integer> movieListTotalCommentsLiveData = new MutableLiveData<>();

    public LiveData<Integer> getMovieListTotalCommentsLiveData() {
        return movieListTotalCommentsLiveData;
    }

    public void setMovieListTotalCommentsLiveData(Integer totalComments) {
        this.movieListTotalCommentsLiveData.setValue(totalComments);
    }

    private final MutableLiveData<CommentAction> commentActionLiveData = new MutableLiveData<>();

    public LiveData<CommentAction> getCommentActionLiveData(){
        return  commentActionLiveData;
    }

    public void setCommentActionLiveData(CommentAction action){
        commentActionLiveData.setValue(action);
    }

    public void requestMovieListComments(Integer movieListId, Integer page) {
        if(page == 1)
            resetCommentList();
        BackendRetrofitService.getInstance().getMovieListAPI().getMovieListComments(movieListId, page).enqueue(
                new Callback<PaginatedResponse<List<Comment>>>() {
                    @Override
                    public void onResponse(Call<PaginatedResponse<List<Comment>>> call, Response<PaginatedResponse<List<Comment>>> response) {
                        if(!response.isSuccessful())
                            return;

                        if (response.body().getData().size() != 0) {
                                 comments.addAll(response.body().getData());
                        }else{
                            setMovieListTotalCommentsLiveData(0);
                        }
                        setMovieListTotalCommentsLiveData(response.body().getTotalRecords());
                        setMovieListCommentsLiveData(comments);
                    }
                    @Override
                    public void onFailure(Call<PaginatedResponse<List<Comment>>> call, Throwable t) {
                    }
                });
    }

    public void requestCommentByNotification(Notification notification, NavController navController, FragmentActivity activity){
        BackendRetrofitService.getInstance().getCommentAPI().getComment(notification.getContentId()).enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                if(response.body().getId() == null){
                    SnackbarUtils.failureSnackbar(activity, activity.getString(R.string.content_removed)).show();
                    return;
                }
                BackendRetrofitService.getInstance().getMovieListAPI().getMovieListBasic(response.body().getCommentedListId()).enqueue(new Callback<MovieListBasic>() {
                    @Override
                    public void onResponse(Call<MovieListBasic> call, Response<MovieListBasic> response) {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("movieList",response.body());
                        navController.navigate(R.id.navigation_comment,bundle);
                    }

                    @Override
                    public void onFailure(Call<MovieListBasic> call, Throwable t) {
                        SnackbarUtils.failureSnackbar(activity, activity.getString(R.string.snackbar_server_unreachable)).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {
                SnackbarUtils.failureSnackbar(activity, activity.getString(R.string.snackbar_server_unreachable)).show();
            }
        });
    }

    public void postReport(Report report, Integer movieListId){
        BackendRetrofitService.getInstance().getReportAPI().postReport(report).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                        resetCommentList();
                        commentActionLiveData.setValue(CommentAction.REPORT_COMMENT_SUCCESS);
                        requestMovieListComments(movieListId,1);
                }else{
                    resetCommentList();
                    requestMovieListComments(movieListId,1);
                    commentActionLiveData.setValue(CommentAction.GENERIC_FAILURE);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                commentActionLiveData.setValue(CommentAction.GENERIC_FAILURE);
            }
        });
    }

    public void sendComment(CommentPostJsonWrapper comment){
        BackendRetrofitService.getInstance().getCommentAPI().postComment(comment).enqueue(new Callback<NonPaginatedResponse<Comment>>() {
            @Override
            public void onResponse(Call<NonPaginatedResponse<it.unina.cinemates.views.backend.Comment>> call, Response<NonPaginatedResponse<it.unina.cinemates.views.backend.Comment>> response) {
                if(!response.isSuccessful()){
                    commentActionLiveData.setValue(CommentAction.GENERIC_FAILURE);
                }

                it.unina.cinemates.views.backend.Comment commentResponse = response.body().getData();
                BasicUser author = new BasicUser(LoggedUser.getInstance().getUsername(), LoggedUser.getInstance().getIdPhoto());
                commentResponse.setAuthor(author);
                if(response.code() == 200){
                    comments.add(0,commentResponse);
                    movieListCommentsLiveData.setValue(comments);
                    movieListTotalCommentsLiveData.setValue(comments.size());
                    commentActionLiveData.setValue(CommentAction.POST_COMMENT_SUCCESS);
                }
            }

            @Override
            public void onFailure(Call<NonPaginatedResponse<it.unina.cinemates.views.backend.Comment>> call, Throwable t) {
                commentActionLiveData.setValue(CommentAction.GENERIC_FAILURE);
            }
        });
    }

    public void deleteComment(Comment comment){
        BackendRetrofitService.getInstance().getCommentAPI().deleteComment(comment.getId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    deletedCommentLiveData.setValue(true);
                    comments.remove(comment);
                    resetCommentList();
                    requestMovieListComments(comment.getCommentedListId(),1);
                }else{
                    commentActionLiveData.setValue(CommentAction.GENERIC_FAILURE);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                commentActionLiveData.setValue(CommentAction.GENERIC_FAILURE);
            }
        });
    }

    public void resetCommentList(){
        this.comments.clear();
    }
}
