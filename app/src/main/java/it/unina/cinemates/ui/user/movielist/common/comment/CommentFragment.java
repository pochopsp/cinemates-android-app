package it.unina.cinemates.ui.user.movielist.common.comment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unina.cinemates.MainActivity;
import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.retrofit.backend.jsonwrappers.post.CommentPostJsonWrapper;
import it.unina.cinemates.ui.common.recyclerview.ItemDecorationWithInitialSpace;
import it.unina.cinemates.ui.common.utils.AlertDialogUtils;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.viewmodels.user.movielist.common.CommentViewModel;
import it.unina.cinemates.viewmodels.NotificationViewModel;
import it.unina.cinemates.viewmodels.ReportViewModel;
import it.unina.cinemates.viewmodels.enums.CommentAction;
import it.unina.cinemates.views.backend.Comment;
import it.unina.cinemates.views.backend.MovieListBasic;

public class CommentFragment extends Fragment {

    private CommentViewModel commentViewModel;
    private NotificationViewModel notificationViewModel;
    private ReportViewModel reportViewModel;

    private ImageView noCommentImageView;
    private TextView noCommentTextView;
    private TextView noCommentTextTextView;
    private TextView totalCommentsTextView;
    private EditText commentEditText;

    private Integer movieListId;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private List<Comment> comments;
    private CommentAdapter adapter;

    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private final AtomicBoolean loading = new AtomicBoolean(true);
    private boolean done = false;
    private int currentPage = 1;

    public CommentFragment() {
        // Required empty public constructor
    }

    public static CommentFragment newInstance() {
        return new CommentFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        commentViewModel = new ViewModelProvider(requireActivity()).get(CommentViewModel.class);
        notificationViewModel = new ViewModelProvider(requireActivity()).get(NotificationViewModel.class);
        reportViewModel = new ViewModelProvider(requireActivity()).get(ReportViewModel.class);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_comment, container, false);

        setupGui(view);

        reportViewModel.getReportMutableLiveData().observe(getViewLifecycleOwner(),report -> {

            if(report == null)
                return;

            commentViewModel.postReport(report,movieListId);
            reportViewModel.setReportMutableLiveData(null);
        });


        commentViewModel.getMovieListCommentsLiveData().observe(getViewLifecycleOwner(), allComments -> {
            loading.set(false);
            comments = allComments;
            if(comments.size() == 0){
                adapter.notifyDataSetChanged();
                return;
            }else {
                adapter.setComments(allComments);
                adapter.notifyDataSetChanged();
            }
            if(done)
                return;

            done = true;

            notificationViewModel.getClickedNotificationLiveData().observe(getViewLifecycleOwner(),notification -> {
                if(notification != null){
                    Integer commentId = notification.getContentId();
                    for(int i = 0; i < comments.size(); i++){
                        if(comments.get(i).getId().equals(commentId)){
                            recyclerView.scrollToPosition(i);
                            return;
                        }
                    }
                }
            });

        });

        commentViewModel.getMovieListTotalCommentsLiveData().observe(getViewLifecycleOwner(),totalComments ->{
            if(totalComments == 0)
                showNoCommentGui();
            else
                hideNoCommentGui();
            totalCommentsTextView.setText(String.format(getString(R.string.total_number_comments),totalComments));
            if(totalComments != 0)
                totalCommentsTextView.setVisibility(View.VISIBLE);
        });


        commentViewModel.getDeletedCommentLiveData().observe(getViewLifecycleOwner(),deleted -> currentPage = 1);

        TextView listNameTextView = view.findViewById(R.id.comment_title_list_name_textview);


        Bundle bundle = getArguments();
        assert bundle != null;
        MovieListBasic movieListBasic = (MovieListBasic) bundle.get("movieList");

        listNameTextView.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            Bundle bundle1 = new Bundle();
            bundle1.putInt("movieListId", movieListBasic.getId());
            if(movieListBasic.getOwnerId().equals(LoggedUser.getInstance().getUsername()))
                navController.navigate(R.id.navigation_logged_user_movie_list, bundle1);
            else
                navController.navigate(R.id.navigation_other_user_movie_list, bundle1);
        });

        movieListId = movieListBasic.getId();
            String listName = movieListBasic.getName();
            if(listName == null) {
                switch (movieListBasic.getType()) {
                    case ToWatch:
                        listNameTextView.setText(getString(R.string.to_watch_list_title));
                        break;
                    case Favorites:
                        listNameTextView.setText(getString(R.string.favorites_list_title));
                        break;
                }
            }else
                listNameTextView.setText(movieListBasic.getName());

        commentViewModel.requestMovieListComments(movieListId, 1);

        commentViewModel.getCommentActionLiveData().observe(getViewLifecycleOwner(), commentAction -> {
            if(commentAction == CommentAction.IDLE)
                return;

            switch (commentAction){
                case REPORT_COMMENT_SUCCESS:
                    AlertDialogUtils.alertDialogWithoutNegativeButton(requireContext(),R.string.report_sent,
                            R.string.report_sent_text,R.string.dialog_button_ok).show();
                    break;
                case POST_COMMENT_SUCCESS:
                    recyclerView.scrollToPosition(0);
                    adapter.notifyDataSetChanged();
                    SnackbarUtils.successAnchorSnackbar(requireActivity(),getString(R.string.comment_post_success),commentEditText).show();
                    break;
                case DELETE_COMMENT_SUCCESS:
                    SnackbarUtils.successAnchorSnackbar(requireActivity(),getString(R.string.delete_comment_success),commentEditText).show();
                    break;
                case GENERIC_FAILURE:
                    SnackbarUtils.failureAnchorSnackbar(requireActivity(),getString(R.string.snackbar_server_unreachable),commentEditText).show();
                    break;
            }
            commentViewModel.setCommentActionLiveData(CommentAction.IDLE);
        });

        return view;
    }

    private void setupGui(View view) {
        noCommentImageView = view.findViewById(R.id.no_comment_image_view);
        noCommentTextView = view.findViewById(R.id.no_comment_title_text_view);
        noCommentTextTextView = view.findViewById(R.id.no_comment_text_textview);
        totalCommentsTextView = view.findViewById(R.id.total_comments_textview);

        hideNoCommentGui();

        ImageView backIcon = view.findViewById(R.id.back_icon_comment);
        backIcon.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
        });

        recyclerView = view.findViewById(R.id.comment_recyclerview);
        refreshLayout = view.findViewById(R.id.comment_refresh_layout);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(new ItemDecorationWithInitialSpace(60,46));
        recyclerView.setLayoutManager(layoutManager);
        comments = new ArrayList<>();
        adapter = new CommentAdapter(comments,requireContext(),requireActivity());
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (dy > 0) { //check for scroll down
                    assert layoutManager != null;
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (!loading.get()) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading.set(true);
                            commentViewModel.requestMovieListComments(movieListId,++currentPage);
                        }
                    }
                }
            }
        });

        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(true);
            commentViewModel.resetCommentList();
            currentPage = 1;
            commentViewModel.requestMovieListComments(movieListId,1);
            refreshLayout.setRefreshing(false);
        });

        commentEditText = view.findViewById(R.id.comment_edit_text);
        commentEditText.setImeOptions(EditorInfo.IME_ACTION_SEND);
        commentEditText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        Button sendButton = view.findViewById(R.id.send_icon);
        sendButton.setOnClickListener(v -> prepareToSendComment(view));

        commentEditText.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_SEND){
                prepareToSendComment(view);
                return true;
            }
            return false;
        });

    }

    private void prepareToSendComment(View view) {
        String bodyComment = commentEditText.getText().toString();

        bodyComment = bodyComment.trim();

        commentEditText.setText(bodyComment);
        commentEditText.setSelection(commentEditText.length());

        if(bodyComment.length() < 5) {
            SnackbarUtils.failureAnchorSnackbar(requireActivity(),getString(R.string.minimum_comment_length),commentEditText).show();
            return;
        }

        ((MainActivity)requireActivity()).closeKeyboard(view);

        commentEditText.setText("");
        CommentPostJsonWrapper comment = new CommentPostJsonWrapper(bodyComment, LoggedUser.getInstance().getUsername(), movieListId);
        sendComment(comment);
    }


    private void hideNoCommentGui(){
        noCommentImageView.setVisibility(View.GONE);
        noCommentTextView.setVisibility(View.GONE);
        noCommentTextTextView.setVisibility(View.GONE);
    }

    private void showNoCommentGui(){
        noCommentImageView.setVisibility(View.VISIBLE);
        noCommentTextView.setVisibility(View.VISIBLE);
        noCommentTextTextView.setVisibility(View.VISIBLE);
    }

    private void sendComment(CommentPostJsonWrapper comment){
        refreshLayout.requestFocus();

        FirebaseAnalytics.getInstance(requireContext()).logEvent("CommentToList", null);

        commentViewModel.sendComment(comment);
    }

    @Override
    public void onDetach() {
        commentViewModel.resetCommentList();
        notificationViewModel.setClickedNotificationLiveData(null);
        super.onDetach();
    }
}