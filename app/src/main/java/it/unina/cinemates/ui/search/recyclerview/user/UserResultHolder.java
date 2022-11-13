package it.unina.cinemates.ui.search.recyclerview.user;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cloudinary.CloudinaryHelper;
import it.unina.cinemates.ui.common.utils.GlideUtils;


public class UserResultHolder extends RecyclerView.ViewHolder {

    private final ImageView userImageView;
    private final TextView usernameTextView;
    private View view;
    private final Button followButton;
    private final Button followingButton;
    private final Button pendingFollowButton;

    public UserResultHolder(@NonNull View itemView) {
        super(itemView);

        view = itemView;
        userImageView = itemView.findViewById(R.id.comment_profile_pic_image_view);
        usernameTextView = itemView.findViewById(R.id.username_comment_author_textview);
        followButton = itemView.findViewById(R.id.search_follow_button);
        followingButton = itemView.findViewById(R.id.search_following_outlined_button);
        pendingFollowButton = itemView.findViewById(R.id.search_pending_follow_button);
    }

    public void setUserImage(Integer imageId) {
        GlideUtils.loadAndSetCircleImageWithUserPlaceholder(CloudinaryHelper.imagePath(String.valueOf(imageId)),userImageView,view.getContext());
    }

    public TextView getUsernameTextView() {
        return usernameTextView;
    }

    public Button getFollowButton() {
        return followButton;
    }

    public Button getFollowingButton() {
        return followingButton;
    }

    public Button getPendingFollowButton() {
        return pendingFollowButton;
    }


    public View getView(){return view;}
}

