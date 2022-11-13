package it.unina.cinemates.ui.notification;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cloudinary.CloudinaryHelper;
import it.unina.cinemates.retrofit.backend.BackendRetrofitService;
import it.unina.cinemates.ui.common.utils.GlideUtils;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.utils.TimeUtils;
import it.unina.cinemates.viewmodels.NotificationViewModel;
import it.unina.cinemates.views.backend.BasicUser;
import it.unina.cinemates.views.backend.ListInformation;
import it.unina.cinemates.views.backend.NotifiedReport;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportFragment extends Fragment {

    private NotificationViewModel notificationViewModel;

    public ReportFragment() {
    }

    public static ReportFragment newInstance() {
        return new ReportFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        notificationViewModel = new ViewModelProvider(requireActivity()).get(NotificationViewModel.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);
        TextView titleTextView = view.findViewById(R.id.report_title_text_view);

        View includedCommentView = view.findViewById(R.id.included_comment);
        TextView usernameCommentTextView = includedCommentView.findViewById(R.id.username_comment_author_textview);
        TextView commentTextTextView = includedCommentView.findViewById(R.id.comment_text_textview);
        ImageView userImageView = includedCommentView.findViewById(R.id.comment_profile_pic_image_view);
        TextView timeTextView = includedCommentView.findViewById(R.id.comment_time_textview);

        TextView listTextView = view.findViewById(R.id.report_list_text_textview);
        TextView ownerListTextView = view.findViewById(R.id.report_list_owner_text_textview);
        TextView reportTypeTextView = view.findViewById(R.id.report_type_text_textview);
        TextView reportOutcomeTextView = view.findViewById(R.id.report_outcome_text_textview);
        TextView reportOutcomeDescriptionTextView = view.findViewById(R.id.report_outcome_description_textview);

        ImageView backIcon = view.findViewById(R.id.back_icon_report);
        backIcon.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
        });

        notificationViewModel.getClickedNotificationLiveData().observe(getViewLifecycleOwner(), notification -> {
            BackendRetrofitService.getInstance().getReportAPI().getReport(notification.getContentId()).enqueue(new Callback<NotifiedReport>() {
                @Override
                public void onResponse(Call<NotifiedReport> call, Response<NotifiedReport> response) {
                    if (!response.isSuccessful())
                        return;
                    NotifiedReport notifiedReport = response.body();

                    assert notifiedReport != null;
                    if (notifiedReport.getOutcome() == null)
                        titleTextView.setText(getString(R.string.report_information_title));
                    else
                        titleTextView.setText(getString(R.string.report_outcome_title));

                    BasicUser author = notifiedReport.getComment().getCommentAuthor();
                    usernameCommentTextView.setText(author.getUsername());
                    commentTextTextView.setText(notifiedReport.getComment().getBody());

                    if (author.getIdPhoto() != null)
                        GlideUtils.loadAndSetCircleImageWithUserPlaceholder(CloudinaryHelper.imagePath(author.getIdPhoto().toString()), userImageView, view.getContext());

                    timeTextView.setText(TimeUtils.getRelativeTime(notifiedReport.getComment().getTimestamp(), 1));

                    ListInformation listInformation = notifiedReport.getListInformation();
                    String listName = listInformation.getName();
                    if (listName == null) {
                        switch (listInformation.getType()) {
                            case ToWatch:
                                listTextView.setText(getString(R.string.to_watch_list_title));
                                break;
                            case Favorites:
                                listTextView.setText(getString(R.string.favorites_list_title));
                                break;
                        }
                    } else
                        listTextView.setText(listName);

                    ownerListTextView.setText(listInformation.getOwnerId());

                    switch (notifiedReport.getType()) {
                        case HateSpeech:
                            reportTypeTextView.setText(getString(R.string.hate_speech_report));
                            break;
                        case Spam:
                            reportTypeTextView.setText(getString(R.string.spam_report));
                            break;
                        case Racist:
                            reportTypeTextView.setText(getString(R.string.racist_report));
                            break;
                        case Offensive:
                            reportTypeTextView.setText(getString(R.string.offensive_report));
                            break;
                        case Sexist:
                            reportTypeTextView.setText(getString(R.string.sexist_report));
                            break;
                    }
                    if (notifiedReport.getOutcome() == null) {
                        reportOutcomeTextView.setText(getString(R.string.report_outcome_null));
                        reportOutcomeDescriptionTextView.setText(R.string.reported_comment_description_text);
                    } else if (notifiedReport.getOutcome()) {
                        reportOutcomeTextView.setText(getString(R.string.report_outcome_true));
                        reportOutcomeDescriptionTextView.setText(R.string.report_outcome_true_description_text);
                    } else {
                        reportOutcomeTextView.setText(getString(R.string.report_outcome_false));
                        reportOutcomeDescriptionTextView.setText(R.string.report_outcome_false_description_text);
                    }
                }

                @Override
                public void onFailure(Call<NotifiedReport> call, Throwable t) {
                    SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.snackbar_server_unreachable)).show();
                }
            });

        });

        return view;
    }
}