package it.unina.cinemates.ui.user.follow;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.models.Follow;
import it.unina.cinemates.retrofit.NonPaginatedResponse;
import it.unina.cinemates.retrofit.backend.BackendRetrofitService;
import it.unina.cinemates.ui.common.utils.AlertDialogUtils;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.viewmodels.user.follow.FollowViewModel;
import it.unina.cinemates.views.backend.UserResult;
import it.unina.cinemates.views.backend.enums.FollowPageType;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtherUserFollowAdapter extends RecyclerView.Adapter<UserFollowHolder> {

    private List<UserResult> users;
    private final Context context;
    private final FragmentActivity activity;
    private final FollowViewModel followViewModel;
    private final String otherUsername;
    private final FollowPageType pageType;

    public OtherUserFollowAdapter(List<UserResult> users, String otherUsername, FollowPageType pageType, Context context, FragmentActivity activity){
        this.users = users;
        this.otherUsername = otherUsername;
        this.pageType = pageType;
        this.context = context;
        this.activity = activity;
        this.followViewModel = new ViewModelProvider(activity).get(FollowViewModel.class);
    }

    @NonNull
    @Override
    public UserFollowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_follow_holder,parent,false);
        return new UserFollowHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserFollowHolder holder, int position) {

        UserResult userResult = users.get(position);

        holder.getFollowButton().setVisibility(View.GONE);
        holder.getFollowingButton().setVisibility(View.GONE);
        holder.getPendingButton().setVisibility(View.GONE);
        holder.setUserImage(userResult.getIdPhoto());
        holder.getUsernameTextView().setText(userResult.getUsername());
        if(!userResult.getUsername().equals(LoggedUser.getInstance().getUsername())) {
            switch (userResult.getFollowRequestStatus()) {
                case Accepted:
                    holder.getFollowingButton().setVisibility(View.VISIBLE);
                    break;
                case Pending:
                    holder.getPendingButton().setVisibility(View.VISIBLE);
                    break;
                case NoRequest:
                    holder.getFollowButton().setVisibility(View.VISIBLE);
                    break;
            }
        }

        holder.getFollowingButton().setOnClickListener(v -> {
            Follow follow = new Follow();
            follow.setFollowerId(LoggedUser.getInstance().getUsername());
            follow.setFollowingId(userResult.getUsername());
            AlertDialogUtils.alertDialog(context, R.string.remove_follow_dialog_title,
                    R.string.remove_follow_dialog_message,R.string.remove_follow_dialog_positive_button,
                    (dialog, which) -> followViewModel.deleteMyFollowing(follow.getCompositeKey(), userResult, otherUsername, pageType, activity)).show();
        });

        holder.getFollowButton().setOnClickListener(v -> {
            Follow follow = new Follow();
            follow.setFollowerId(LoggedUser.getInstance().getUsername());
            follow.setFollowingId(userResult.getUsername());
            BackendRetrofitService.getInstance().getFollowAPI().postFollow(follow).enqueue(new Callback<NonPaginatedResponse<Follow>>() {
                @Override
                public void onResponse(@NonNull Call<NonPaginatedResponse<Follow>> call, @NonNull Response<NonPaginatedResponse<Follow>> response) {
                    if(response.code() == 200) {
                        holder.getFollowButton().setVisibility(View.GONE);
                        holder.getPendingButton().setVisibility(View.VISIBLE);
                    }else{
                        SnackbarUtils.failureSnackbar(activity,activity.getString(R.string.snackbar_server_unreachable)).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<NonPaginatedResponse<Follow>> call, @NonNull Throwable t) {
                    SnackbarUtils.failureSnackbar(activity,activity.getString(R.string.snackbar_server_unreachable)).show();
                }
            });
        });

        /*if(holder.getFollowButton().getVisibility() == View.VISIBLE) {
            holder.getFollowButton().setOnClickListener(v -> {
                Follow follow = new Follow();
                follow.setFollowingId(LoggedUser.getInstance().getUsername());
                follow.setFollowerId(userResult.getUsername());
                AlertDialogUtils.alertDialog(context, R.string.remove_follow_dialog_title,
                        R.string.remove_follow_dialog_message, R.string.remove_follow_dialog_positive_button,
                        (dialog, which) -> followViewModel.deleteMyFollower(follow.getCompositeKey(), userResult, activity)).show();
            });
        }*/

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("otherUserUsername", userResult.getUsername());
            NavController navController = Navigation.findNavController(holder.itemView);
            if(userResult.getUsername().equals(LoggedUser.getInstance().getUsername()))
                navController.navigate(R.id.navigation_logged_user_profile);
            else
                navController.navigate(R.id.navigation_other_user_profile, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setUsers(List<UserResult> users){
        this.users = users;
    }
}
