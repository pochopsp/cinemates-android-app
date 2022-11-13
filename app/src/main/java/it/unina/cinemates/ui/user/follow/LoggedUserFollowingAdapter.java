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
import it.unina.cinemates.ui.common.utils.AlertDialogUtils;
import it.unina.cinemates.viewmodels.user.follow.FollowViewModel;
import it.unina.cinemates.views.backend.UserResult;

public class LoggedUserFollowingAdapter extends RecyclerView.Adapter<UserFollowHolder> {

    private List<UserResult> users;
    private Context context;
    private FragmentActivity activity;
    private FollowViewModel followViewModel;

    public LoggedUserFollowingAdapter(List<UserResult> users, Context context, FragmentActivity activity){
        this.users = users;
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

        holder.setUserImage(userResult.getIdPhoto());
        holder.getUsernameTextView().setText(userResult.getUsername());

        holder.getFollowingButton().setVisibility(View.VISIBLE);
        holder.getFollowingButton().setOnClickListener(v -> {
            Follow follow = new Follow();
            follow.setFollowerId(LoggedUser.getInstance().getUsername());
            follow.setFollowingId(userResult.getUsername());
            AlertDialogUtils.alertDialog(context, R.string.remove_follow_dialog_title,
                    R.string.remove_follow_dialog_message,R.string.remove_follow_dialog_positive_button,
                    (dialog, which) -> followViewModel.deleteMyFollowing(follow.getCompositeKey(), userResult,LoggedUser.getInstance().getUsername(),null,activity)).show();
        });

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("otherUserUsername", userResult.getUsername());
            NavController navController = Navigation.findNavController(holder.itemView);
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
