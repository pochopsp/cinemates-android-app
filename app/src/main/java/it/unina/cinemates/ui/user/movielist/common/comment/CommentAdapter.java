package it.unina.cinemates.ui.user.movielist.common.comment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.models.Report;
import it.unina.cinemates.ui.common.utils.AlertDialogUtils;
import it.unina.cinemates.utils.TimeUtils;
import it.unina.cinemates.viewmodels.user.movielist.common.CommentViewModel;
import it.unina.cinemates.viewmodels.ReportViewModel;
import it.unina.cinemates.views.backend.Comment;

public class CommentAdapter extends RecyclerView.Adapter<CommentHolder> {

    private List<Comment> comments;
    private final Context context;
    private final FragmentActivity activity;
    private final ReportViewModel reportViewModel;
    private final CommentViewModel commentViewModel;

    public CommentAdapter(List<Comment> comments, Context context, FragmentActivity activity) {
        this.comments = comments;
        this.context = context;
        this.activity = activity;
        this.reportViewModel = new ViewModelProvider(activity).get(ReportViewModel.class);
        this.commentViewModel = new ViewModelProvider(activity).get(CommentViewModel.class);
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_holder, parent, false);
        return new CommentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHolder holder, int position) {
        Comment comment = comments.get(position);

        holder.itemView.setOnLongClickListener(null);

        holder.getUsernameTextView().setText(comment.getAuthor().getUsername());
        holder.getBodyCommentTextView().setText(comment.getBody());

        String time = TimeUtils.getRelativeTime(comment.getTimestamp(), 1);
        holder.getTimeTextView().setText(time);

        holder.setUserImage(comment.getAuthor().getIdPhoto());

        holder.getReportIconImageView().setVisibility(View.VISIBLE);
        if (comment.getAuthor().getUsername().equals(LoggedUser.getInstance().getUsername()))
            holder.getReportIconImageView().setVisibility(View.GONE);

        String[] reportTypes = {context.getString(R.string.racist_report), context.getString(R.string.hate_speech_report),
                context.getString(R.string.sexist_report), context.getString(R.string.offensive_report), context.getString(R.string.spam_report)};

        String[] selectedItem = {context.getString(R.string.racist_report)};

        DialogInterface.OnClickListener positiveButtonListener = (dialog, which) -> {
            FirebaseAnalytics.getInstance(context).logEvent("CommentReported", null);

            Report report = new Report();
            report.setCommentId(comment.getId());
            report.setAuthorId(LoggedUser.getInstance().getUsername());
            report.setType(report.jsonReportTypeFromViewReportType(selectedItem[0]));
            reportViewModel.setReportMutableLiveData(report);
        };

        holder.getReportIconImageView().setOnClickListener(v -> {
            AlertDialogUtils.radioButtonAlertDialog(context, reportTypes, R.string.report_comment_dialog_title, R.string.report_comment_dialog_button, positiveButtonListener, selectedItem).show();
        });

        if (comment.getAuthor().getUsername().equals(LoggedUser.getInstance().getUsername())) {
            holder.itemView.setOnLongClickListener(v -> {
                PopupMenu popup = new PopupMenu(context, holder.itemView);
                popup.getMenuInflater().inflate(R.menu.comment_delete_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(item -> {
                    AlertDialogUtils.alertDialog(context, R.string.delete_comment_dialog_title, R.string.irreversible_action_dialog_message, R.string.delete,
                            (dialog, which) -> commentViewModel.deleteComment(comment)).show();
                    return true;
                });

                popup.show();
                return true;
            });
        }

        holder.getUserImageView().setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("otherUserUsername", comment.getAuthor().getUsername());
            NavController navController = Navigation.findNavController(holder.itemView);
            if (comment.getAuthor().getUsername().equals(LoggedUser.getInstance().getUsername()))
                navController.navigate(R.id.navigation_logged_user_profile);
            else
                navController.navigate(R.id.navigation_other_user_profile, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
