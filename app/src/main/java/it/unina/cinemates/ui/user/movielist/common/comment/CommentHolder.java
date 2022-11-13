package it.unina.cinemates.ui.user.movielist.common.comment;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cloudinary.CloudinaryHelper;
import it.unina.cinemates.ui.common.utils.GlideUtils;

public class CommentHolder extends RecyclerView.ViewHolder {

    private final TextView usernameTextView;
    private final TextView bodyCommentTextView;
    private final TextView timeTextView;
    private final ImageView userImageView;
    private final ImageView reportIconImageView;
    private final RelativeLayout relativeLayout;
    private final View view;


    public CommentHolder(@NonNull View itemView) {
        super(itemView);
        view = itemView;
        usernameTextView = itemView.findViewById(R.id.username_comment_author_textview);
        bodyCommentTextView = itemView.findViewById(R.id.comment_text_textview);
        timeTextView = itemView.findViewById(R.id.comment_time_textview);
        userImageView = itemView.findViewById(R.id.comment_profile_pic_image_view);
        reportIconImageView = itemView.findViewById(R.id.report_comment_icon);
        relativeLayout = itemView.findViewById(R.id.comment_holder_relative_layout);
    }

    public TextView getUsernameTextView() {
        return usernameTextView;
    }

    public TextView getBodyCommentTextView() {
        return bodyCommentTextView;
    }

    public TextView getTimeTextView() {
        return timeTextView;
    }

    public void setUserImage(Integer imageId) {
        GlideUtils.loadAndSetCircleImageWithUserPlaceholder(CloudinaryHelper.imagePath(String.valueOf(imageId)), userImageView, view.getContext());
    }

    public ImageView getUserImageView() {
        return userImageView;
    }

    public ImageView getReportIconImageView() {
        return reportIconImageView;
    }

    public RelativeLayout getRelativeLayout() {
        return relativeLayout;
    }
}
