package it.unina.cinemates.ui.user.follow;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cloudinary.CloudinaryHelper;
import it.unina.cinemates.ui.common.utils.GlideUtils;

public class UserFollowHolder extends RecyclerView.ViewHolder {

    private final ImageView userImageView;
    private final TextView usernameTextView;
    private final View view;
    private final Button followingButton;
    private final Button removeButton;
    private final Button followButton;
    private final Button pendingButton;


    public UserFollowHolder(@NonNull View itemView) {
        super(itemView);

        view = itemView;
        userImageView = itemView.findViewById(R.id.follow_image_view);
        usernameTextView = itemView.findViewById(R.id.username_follow_textview);
        followingButton = itemView.findViewById(R.id.following_outlined_button);
        removeButton = itemView.findViewById(R.id.remove_follow_button);
        followButton = itemView.findViewById(R.id.follow_button);
        pendingButton = itemView.findViewById(R.id.pending_follow_button);
    }

    public void setUserImage(Integer imageId) {
        GlideUtils.loadAndSetCircleImageWithUserPlaceholder(CloudinaryHelper.imagePath(String.valueOf(imageId)),userImageView,view.getContext());
    }

    public TextView getUsernameTextView() {
        return usernameTextView;
    }

    public Button getFollowingButton() {
        return followingButton;
    }

    public Button getRemoveFollowButton() {
        return removeButton;
    }

    public Button getFollowButton() {
        return followButton;
    }

    public Button getPendingButton() {
        return pendingButton;
    }
}
