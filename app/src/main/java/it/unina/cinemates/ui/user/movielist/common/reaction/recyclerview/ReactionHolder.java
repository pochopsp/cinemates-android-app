package it.unina.cinemates.ui.user.movielist.common.reaction.recyclerview;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cloudinary.CloudinaryHelper;
import it.unina.cinemates.ui.common.utils.GlideUtils;

public class ReactionHolder extends RecyclerView.ViewHolder {

    private final TextView usernameTextView;
    private final ImageView userImageView;
    private final ImageView reactionImageView;
    private final View view;


    public ReactionHolder(@NonNull View itemView) {
        super(itemView);
        view = itemView;
        usernameTextView = itemView.findViewById(R.id.username_reaction_author_textview);
        userImageView = itemView.findViewById(R.id.reaction_profile_pic_image_view);
        reactionImageView = itemView.findViewById(R.id.reaction_imageview);
    }

    public TextView getUsernameTextView() {
        return usernameTextView;
    }

    public void setUserImage(Integer imageId) {
        GlideUtils.loadAndSetCircleImageWithUserPlaceholder(CloudinaryHelper.imagePath(String.valueOf(imageId)), userImageView, view.getContext());
    }

    public ImageView getReactionImageView() {
        return reactionImageView;
    }

    public ImageView getUserImageView() {
        return userImageView;
    }
}
