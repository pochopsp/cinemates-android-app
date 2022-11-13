package it.unina.cinemates.ui.user.movielist.common.rating.recyclerview;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cloudinary.CloudinaryHelper;
import it.unina.cinemates.ui.common.utils.GlideUtils;

public class RatingHolder extends RecyclerView.ViewHolder {

    private final TextView usernameTextView;
    private final ImageView userImageView;
    private final SimpleRatingBar ratingBar;
    private final View view;

    public RatingHolder(@NonNull View itemView) {
        super(itemView);
        view = itemView;
        usernameTextView = itemView.findViewById(R.id.username_rating_author_textview);
        userImageView = itemView.findViewById(R.id.rating_profile_pic_image_view);
        ratingBar = itemView.findViewById(R.id.user_rating_bar);
        ratingBar.setIndicator(true);
    }

    public TextView getUsernameTextView() {
        return usernameTextView;
    }

    public void setUserImage(Integer imageId) {
        GlideUtils.loadAndSetCircleImageWithUserPlaceholder(CloudinaryHelper.imagePath(String.valueOf(imageId)), userImageView, view.getContext());
    }

    public ImageView getUserImageView() {
        return userImageView;
    }

    public SimpleRatingBar getRatingBar() {
        return ratingBar;
    }
}

