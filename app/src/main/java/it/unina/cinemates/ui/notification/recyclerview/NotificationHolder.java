package it.unina.cinemates.ui.notification.recyclerview;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cloudinary.CloudinaryHelper;
import it.unina.cinemates.ui.common.utils.GlideUtils;

public class NotificationHolder extends RecyclerView.ViewHolder {

    private final TextView usernameTextView;
    private final TextView notificationTextTextView;
    private final TextView timeTextTextView;
    private final TextView acceptedRequestTextView;
    private final TextView declinedRequestTextView;
    private final ImageView userImageView;
    private final Button acceptFollowRequestButton;
    private final Button declineFollowRequestButton;
    private final View view;
    private final RelativeLayout relativeLayout;

    public NotificationHolder(View itemView) {
        super(itemView);

        view = itemView;
        usernameTextView = itemView.findViewById(R.id.comment_text_textview);
        notificationTextTextView = itemView.findViewById(R.id.username_comment_author_textview);
        timeTextTextView = itemView.findViewById(R.id.notification_time_text_view);
        acceptedRequestTextView = itemView.findViewById(R.id.accepted_request_text_view);
        declinedRequestTextView = itemView.findViewById(R.id.declined_request_text_view);
        userImageView = itemView.findViewById(R.id.comment_profile_pic_image_view);
        acceptFollowRequestButton = itemView.findViewById(R.id.search_follow_button);
        declineFollowRequestButton = itemView.findViewById(R.id.decline_follow_button);
        relativeLayout = itemView.findViewById(R.id.comment_holder_relative_layout);

    }

    public void setUsername(String name) {
        usernameTextView.setText(name);
    }

    public void setUserImage(Integer imageId) {
        GlideUtils.loadAndSetCircleImageWithUserPlaceholder(CloudinaryHelper.imagePath(String.valueOf(imageId)), userImageView, view.getContext());
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void setIcon(Integer imageId) {
        userImageView.setImageDrawable(view.getContext().getDrawable(imageId));
    }

    public void setNotificationText(String text) {
        notificationTextTextView.setText(text);
    }

    public void setTime(String time) {
        timeTextTextView.setText(time);
    }

    public Button getAcceptFollowRequestButton() {
        return acceptFollowRequestButton;
    }

    public Button getDeclineFollowRequestButton() {
        return declineFollowRequestButton;
    }

    public TextView getAcceptedFollowTextView() {
        return acceptedRequestTextView;
    }

    public TextView getDeclinedRequestTextView() {
        return declinedRequestTextView;
    }

    public RelativeLayout getRelativeLayoutHolder() {
        return relativeLayout;
    }
}
