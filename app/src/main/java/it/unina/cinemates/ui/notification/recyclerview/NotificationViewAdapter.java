package it.unina.cinemates.ui.notification.recyclerview;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import it.unina.cinemates.R;
import it.unina.cinemates.models.Follow;
import it.unina.cinemates.models.Notification;
import it.unina.cinemates.utils.TimeUtils;
import it.unina.cinemates.viewmodels.user.movielist.common.CommentViewModel;
import it.unina.cinemates.viewmodels.NotificationViewModel;
import it.unina.cinemates.viewmodels.user.movielist.common.RatingViewModel;
import it.unina.cinemates.viewmodels.user.movielist.common.ReactionViewModel;

public class NotificationViewAdapter extends RecyclerView.Adapter<NotificationHolder> {

    private List<Notification> notificationList;

    private final Context context;
    private final FragmentActivity activity;
    private final NotificationViewModel notificationViewModel;
    private final ReactionViewModel reactionViewModel;
    private final CommentViewModel commentViewModel;
    private final RatingViewModel ratingViewModel;


    public NotificationViewAdapter(List<Notification> userArrayList, Context context, FragmentActivity activity) {
        this.notificationList = userArrayList;
        this.context = context;
        this.activity = activity;
        this.notificationViewModel = new ViewModelProvider(activity).get(NotificationViewModel.class);
        this.reactionViewModel = new ViewModelProvider(activity).get(ReactionViewModel.class);
        this.commentViewModel = new ViewModelProvider(activity).get(CommentViewModel.class);
        this.ratingViewModel = new ViewModelProvider(activity).get(RatingViewModel.class);
    }

    // This method creates views for the RecyclerView by inflating the layout
    // Into the viewHolders which helps to display the items in the RecyclerView
    @Override
    public @NotNull
    NotificationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.notification_holder, parent, false);
        return new NotificationHolder(view);
    }

    @Override
    public int getItemCount() {
        return notificationList == null ? 0 : notificationList.size();
    }

    // This method is called when binding the data to the views being created in RecyclerView
    @Override
    public void onBindViewHolder(@NonNull NotificationHolder holder, final int position) {
        final Notification notification = notificationList.get(position);
        holder.getAcceptFollowRequestButton().setVisibility(View.GONE);
        holder.getDeclineFollowRequestButton().setVisibility(View.GONE);
        holder.getAcceptedFollowTextView().setVisibility(View.INVISIBLE);
        holder.getDeclinedRequestTextView().setVisibility(View.INVISIBLE);

        if (!notification.getNotificationType().equals("ReportToMyComment") && !notification.getNotificationType().equals("ReportOutcomeOfMyComment") && !notification.getNotificationType().equals("ReportOutcomeOfMyReport")) {
            holder.setUserImage(notification.getNotifierPhotoId());
        }

        holder.getRelativeLayoutHolder().setBackground(AppCompatResources.getDrawable(context, R.drawable.background_notification));
        if (notification.getRead()) {
            holder.getRelativeLayoutHolder().setBackground(AppCompatResources.getDrawable(context, R.drawable.read_background_notification));
        }
        // Set the data to the views here
        holder.setUsername(notification.getNotifierId());
        holder.setTime(TimeUtils.getRelativeTime(notification.getTime(), 1));

        switch (notification.getNotificationType()) {
            case "RatingOnList":
                holder.setNotificationText(context.getString(R.string.rating_text));
                break;
            case "CommentOnList":
                holder.setNotificationText(context.getString(R.string.comment_text));
                break;
            case "ReactionOnList":
                holder.setNotificationText(context.getString(R.string.reaction_text));
                break;
            case "ReportToMyComment":
                holder.setUsername(context.getString(R.string.administrator_text));
                holder.setIcon(R.drawable.ic_baseline_admin_panel_settings_24);
                holder.setNotificationText(context.getString(R.string.report_to_my_comment_text));
                break;
            case "FollowRequestAccepted":
                holder.setNotificationText(context.getString(R.string.follow_request_accepted_text));
                break;
            case "FollowRequestReceived":
                if (notification.getFollowRequestResult() == null) {
                    holder.getAcceptFollowRequestButton().setVisibility(View.VISIBLE);
                    holder.getDeclineFollowRequestButton().setVisibility(View.VISIBLE);
                } else if (notification.getFollowRequestResult()) {
                    holder.getAcceptFollowRequestButton().setVisibility(View.INVISIBLE);
                    holder.getDeclineFollowRequestButton().setVisibility(View.INVISIBLE);
                    holder.getAcceptedFollowTextView().setVisibility(View.VISIBLE);
                } else {
                    holder.getAcceptFollowRequestButton().setVisibility(View.INVISIBLE);
                    holder.getDeclineFollowRequestButton().setVisibility(View.INVISIBLE);
                    holder.getDeclinedRequestTextView().setVisibility(View.VISIBLE);
                }

                holder.setNotificationText(context.getString(R.string.follow_request_text));
                break;
            case "ReportOutcomeOfMyComment":
                holder.setUsername(context.getString(R.string.administrator_text));
                holder.setIcon(R.drawable.ic_baseline_admin_panel_settings_24);
                holder.setNotificationText(context.getString(R.string.outcome_report_to_my_comment_text));
                break;
            case "ReportOutcomeOfMyReport":
                holder.setUsername(context.getString(R.string.administrator_text));
                holder.setIcon(R.drawable.ic_baseline_admin_panel_settings_24);
                holder.setNotificationText(context.getString(R.string.outcome_report_to_my_report_text));
                break;
        }

        holder.getAcceptFollowRequestButton().setOnClickListener(v -> {
            notification.setFollowRequestResult(true);
            notification.setRead(true);
            holder.getAcceptFollowRequestButton().setVisibility(View.INVISIBLE);
            holder.getDeclineFollowRequestButton().setVisibility(View.INVISIBLE);
            holder.getAcceptedFollowTextView().setVisibility(View.VISIBLE);
            String compositeKey = Follow.getCompositeKeyFromNotification(notification);
            Follow follow = new Follow(notification.getNotifierId(), notification.getNotifiedId(), true);
            holder.getRelativeLayoutHolder().setBackground(AppCompatResources.getDrawable(context, R.drawable.read_background_notification));
            notificationViewModel.deleteNotificationAndPutFollow(notification, compositeKey, follow);
        });

        holder.getDeclineFollowRequestButton().setOnClickListener(v -> {
            notification.setFollowRequestResult(false);
            notification.setRead(true);
            holder.getAcceptFollowRequestButton().setVisibility(View.INVISIBLE);
            holder.getDeclineFollowRequestButton().setVisibility(View.INVISIBLE);
            holder.getDeclinedRequestTextView().setVisibility(View.VISIBLE);
            String compositeKey = Follow.getCompositeKeyFromNotification(notification);
            holder.getRelativeLayoutHolder().setBackground(AppCompatResources.getDrawable(context, R.drawable.read_background_notification));
            notificationViewModel.deleteNotificationAndFollow(notification, compositeKey);
        });

        holder.itemView.setOnClickListener(v -> {
            holder.getRelativeLayoutHolder().setBackground(AppCompatResources.getDrawable(context, R.drawable.read_background_notification));
            notificationViewModel.putNotificationRead(notification);
            NavController navController = Navigation.findNavController(holder.itemView);
            notificationViewModel.setClickedNotificationLiveData(notification);
            switch (notification.getNotificationType()) {
                case "RatingOnList":
                    ratingViewModel.requestRatingByNotification(notification, navController, activity);
                    break;
                case "CommentOnList":
                    commentViewModel.requestCommentByNotification(notification, navController, activity);
                    break;
                case "ReactionOnList":
                    reactionViewModel.requestReactionByNotification(notification, navController, activity);
                    break;
                case "ReportToMyComment":
                case "ReportOutcomeOfMyComment":
                case "ReportOutcomeOfMyReport":
                    navController.navigate(R.id.navigation_report);
                    break;
                case "FollowRequestAccepted":
                    Bundle bundle = new Bundle();
                    bundle.putString("otherUserUsername", notification.getNotifierId());
                    navController.navigate(R.id.navigation_other_user_profile, bundle);
                    break;
                case "FollowRequestReceived":
                    bundle = new Bundle();
                    bundle.putString("otherUserUsername", notification.getNotifierId());
                    navController.navigate(R.id.navigation_other_user_profile, bundle);
                    break;
            }
        });

    }

    public void setNotificationList(List<Notification> notifications) {
        this.notificationList = notifications;
    }
}