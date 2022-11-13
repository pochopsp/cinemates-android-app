package it.unina.cinemates.ui.search.recyclerview.user;


import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.models.Follow;
import it.unina.cinemates.retrofit.NonPaginatedResponse;
import it.unina.cinemates.retrofit.backend.BackendRetrofitService;
import it.unina.cinemates.ui.common.utils.AlertDialogUtils;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.views.backend.UserResult;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserResultAdapter extends RecyclerView.Adapter<UserResultHolder> {

    private final List<UserResult> userResults;
    private final View contextView;
    private final FragmentActivity activity;

    
    public UserResultAdapter(List<UserResult> userResults, View contextView, FragmentActivity activity){
        this.userResults = userResults;
        this.contextView = contextView;
        this.activity = activity;
    }

    @NonNull
    @Override
    public UserResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_search_result_holder,parent,false);
        return new UserResultHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserResultHolder holder, int position) {
        UserResult result = userResults.get(position);
        holder.setUserImage(result.getIdPhoto());
        holder.getUsernameTextView().setText(result.getUsername());
        holder.getFollowingButton().setVisibility(View.GONE);
        holder.getPendingFollowButton().setVisibility(View.GONE);
        holder.getFollowButton().setVisibility(View.GONE);



        holder.getView().setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("otherUserUsername", result.getUsername());
            NavController navController = Navigation.findNavController(holder.getView());
            navController.navigate(R.id.navigation_other_user_profile, bundle);
        });


        switch (result.getFollowRequestStatus()){
            case Accepted:
                holder.getFollowingButton().setVisibility(View.VISIBLE);
                break;
            case Pending:
                holder.getPendingFollowButton().setVisibility(View.VISIBLE);
                break;
            case NoRequest:
                holder.getFollowButton().setVisibility(View.VISIBLE);
                break;
        }

        holder.getFollowButton().setOnClickListener(v -> {
            Follow follow = new Follow();
            follow.setFollowerId(LoggedUser.getInstance().getUsername());
            follow.setFollowingId(result.getUsername());
            BackendRetrofitService.getInstance().getFollowAPI().postFollow(follow).enqueue(new Callback<NonPaginatedResponse<Follow>>() {
                @Override
                public void onResponse(Call<NonPaginatedResponse<Follow>> call, Response<NonPaginatedResponse<Follow>> response) {
                    if(response.code() == 200) {
                        holder.getFollowButton().setVisibility(View.GONE);
                        holder.getPendingFollowButton().setVisibility(View.VISIBLE);
                    }else{
                        SnackbarUtils.failureSnackbar(activity,activity.getString(R.string.snackbar_server_unreachable)).show();
                    }
                }

                @Override
                public void onFailure(Call<NonPaginatedResponse<Follow>> call, Throwable t) {

                }
            });
        });

        DialogInterface.OnClickListener deleteFollowListener = (dialog, which) -> {
            String compositeKey = LoggedUser.getInstance().getUsername() + "-" + result.getUsername();
            BackendRetrofitService.getInstance().getFollowAPI().deleteFollow(compositeKey).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if(response.code() == 200){
                        holder.getFollowingButton().setVisibility(View.GONE);
                        holder.getFollowButton().setVisibility(View.VISIBLE);
                    }else{
                        SnackbarUtils.failureSnackbar(activity, activity.getString(R.string.snackbar_server_unreachable));
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    SnackbarUtils.failureSnackbar(activity, activity.getString(R.string.snackbar_server_unreachable));
                }
            });
        };

        holder.getFollowingButton().setOnClickListener(v -> AlertDialogUtils.alertDialog(contextView.getContext(), R.string.remove_follow_dialog_title,
                R.string.remove_follow_dialog_message,R.string.remove_follow_dialog_positive_button,deleteFollowListener).show());

        holder.getPendingFollowButton().setEnabled(false);

    }

    @Override
    public int getItemCount() {
        return userResults.size();
    }

}
