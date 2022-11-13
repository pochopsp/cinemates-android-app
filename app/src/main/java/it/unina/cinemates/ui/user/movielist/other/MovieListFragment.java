package it.unina.cinemates.ui.user.movielist.other;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.models.Reaction;
import it.unina.cinemates.retrofit.backend.enums.BackendOperationResult;
import it.unina.cinemates.retrofit.backend.jsonwrappers.post.MovieListPostJsonWrapper;
import it.unina.cinemates.retrofit.tmdb.TmDbCallbacks;
import it.unina.cinemates.retrofit.tmdb.TmDbRetrofitService;
import it.unina.cinemates.ui.common.interfaces.OnClickListenerGenerator;
import it.unina.cinemates.ui.common.recyclerview.GridSpacingItemDecoration;
import it.unina.cinemates.ui.common.utils.AlertDialogUtils;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.ui.user.movielist.common.recyclerview.MoviePostersAdapter;
import it.unina.cinemates.viewmodels.MovieFullViewModel;
import it.unina.cinemates.viewmodels.user.customMovielists.CustomListsViewModel;
import it.unina.cinemates.viewmodels.user.movielist.common.MovieListFullViewModel;
import it.unina.cinemates.views.backend.LoggedUserReaction;
import it.unina.cinemates.views.backend.MovieListBasic;
import it.unina.cinemates.views.backend.MovieListFull;
import it.unina.cinemates.views.backend.enums.ListType;
import it.unina.cinemates.views.backend.enums.ReactionType;


public class MovieListFragment extends Fragment {

    private static final String TAG = "OTHERUSERMOVIELIST_FRAGMENT";

    private MovieListFullViewModel movieListFullViewModel;
    private CustomListsViewModel customListsViewModel;

    private int movieListId;

    private MovieFullViewModel movieFullViewModel;

    private MoviePostersAdapter moviePostersAdapter;


    private MaterialButton reactionsButton;
    private Button commentsButton;
    private Button ratingsButton;
    private PopupWindow reactionsPopupWindow;
    private ConstraintLayout movieListInteractionsBar;
    private List<RelativeLayout> selectedReactionBackgrounds;
    private LoggedUserReaction loggedUserReaction;
    private int currentReactionsCount;

    private TextView listNameTextView;
    private FloatingActionButton copyListButton;
    private TextView listAuthorTextView;

    private ShowcaseView showcaseView;
    private Snackbar serverUnreachableSnackbar;
    private Snackbar listCopySuccessSnackbar;
    private Snackbar listNameAlreadyTakenErrorSnackbar;
    private Snackbar noReactionsSnackbar;
    private RecyclerView moviePostersRecyclerView;


    public MovieListFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movieListFullViewModel = new ViewModelProvider(requireActivity()).get(MovieListFullViewModel.class);
        customListsViewModel = new ViewModelProvider(requireActivity()).get(CustomListsViewModel.class);
        movieFullViewModel = new ViewModelProvider(requireActivity()).get(MovieFullViewModel.class);
        overrideBackPressedBehavior();
    }

    private void overrideBackPressedBehavior() {
        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                MovieListFragment.this.goBack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void goBack() {

        NavController navController = Navigation.findNavController(requireView());
        navController.navigateUp();

        movieListFullViewModel.resetAllData();
        clearSelectedReactionBackgrounds();
    }

    private void clearSelectedReactionBackgrounds() {
        for (RelativeLayout r : selectedReactionBackgrounds)
            r.setVisibility(View.INVISIBLE);
    }

    @SuppressLint({"SetTextI18n", "RestrictedApi"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_other_user_movie_list, container, false);

        Bundle bundle = getArguments();
        assert bundle != null;
        this.movieListId = (Integer) bundle.get("movieListId");

        ConstraintLayout emptyListLayout = view.findViewById(R.id.other_user_movie_list_no_movies_layout);

        setupBackIcon(view);

        listNameTextView = view.findViewById(R.id.other_user_movie_list_name);

        copyListButton = view.findViewById(R.id.other_user_movie_list_copy_button);

        listAuthorTextView = view.findViewById(R.id.other_user_movie_list_author_value);

        setupRecyclerView(view);

        setupReactionsPopupWindow(view, inflater);

        setupSnackbars();


        movieListFullViewModel.requestFullMovieList(movieListId);


        movieListFullViewModel.getMovieListFull().observe(getViewLifecycleOwner(), movieListFull -> {

            if (movieListFull != null) {
                setupListNameTextView(movieListFull);

                String nameWithUsername = "";
                if (movieListFull.getType() != ListType.Custom) {
                    Locale locale = getResources().getConfiguration().getLocales().get(0);
                    if (locale.toLanguageTag().equals("it-IT")) {
                        if (movieListFull.getType() == ListType.ToWatch)
                            nameWithUsername = getString(R.string.towatch_copied_list_title) + " " + movieListFull.getOwnerId();
                        else if (movieListFull.getType() == ListType.Favorites)
                            nameWithUsername = getString(R.string.favorites_copied_list_title) + " " + movieListFull.getOwnerId();
                    } else {
                        if (movieListFull.getType() == ListType.ToWatch)
                            nameWithUsername = movieListFull.getOwnerId() + "'s " + getString(R.string.towatch_copied_list_title);
                        else if (movieListFull.getType() == ListType.Favorites)
                            nameWithUsername = movieListFull.getOwnerId() + "'s " + getString(R.string.favorites_copied_list_title);
                    }
                }
                String copiedListName = movieListFull.getType() == ListType.Custom ? movieListFull.getName() : nameWithUsername;

                AlertDialog copyListDialog = AlertDialogUtils.alertDialog(
                        requireContext(),
                        R.string.copy_list_dialog_title,
                        R.string.copy_list_dialog_message,
                        R.string.copy_list_dialog_positive_button,
                        (dialog, which) -> customListsViewModel.requestCreateMovieList(
                                new MovieListPostJsonWrapper(
                                        LoggedUser.getInstance().getUsername(),
                                        copiedListName,
                                        movieListFull.getMoviesInList())
                        )
                );

                copyListButton.setOnClickListener(v -> copyListDialog.show());

                listAuthorTextView.setText(movieListFull.getOwnerId());

                setupReactionsCommentsRatingsButtons(view, new MovieListBasic(movieListFull));

                this.loggedUserReaction = movieListFull.getRequesterReaction();

                clearSelectedReactionBackgrounds();

                if (loggedUserReaction != null) {
                    selectedReactionBackgrounds.get(loggedUserReaction.getType().ordinal()).setVisibility(View.VISIBLE);
                    setReactionsIconYellow();
                } else {
                    setReactionsIconWhite();
                }

                reactionsButton.setText(String.valueOf(movieListFull.getReactionsNumber()));
                commentsButton.setText(String.valueOf(movieListFull.getCommentsNumber()));
                ratingsButton.setText(movieListFull.getAverageRating().toString());

                if (movieListFull.getMoviesInList().isEmpty())
                    emptyListLayout.setVisibility(View.VISIBLE);
                else
                    movieListFullViewModel.requestMoviePosters(movieListFull.getMoviesInList());
            }

        });


        customListsViewModel.getCreateMovieListResult().observe(getViewLifecycleOwner(), createMovieListStatus -> {
            switch (createMovieListStatus) {
                case SUCCESS:
                    listCopySuccessSnackbar.show();
                    customListsViewModel.resetCreateMovieListStatus();
                    break;
                case ALREADY_PRESENT:
                    listNameAlreadyTakenErrorSnackbar.show();
                    customListsViewModel.resetCreateMovieListStatus();
                    break;
                case SERVER_UNREACHABLE:
                    serverUnreachableSnackbar.show();
                    customListsViewModel.resetCreateMovieListStatus();
                    break;
            }
        });


        movieListFullViewModel.getMoviesInMovieList().observe(getViewLifecycleOwner(), moviePosters -> {
                    moviePostersAdapter.loadMoviePosters(moviePosters);
                    moviePostersRecyclerView.setVisibility(View.VISIBLE);
        });


        movieListFullViewModel.getReactionHandler().

                getReactionOperationResult().

                observe(getViewLifecycleOwner(), backendOperationResult ->

                {
                    if (backendOperationResult == BackendOperationResult.SUCCESS) {
                        reactionsPopupWindow.dismiss();
                        movieListFullViewModel.getReactionHandler().resetReactionOperationResult();

                        //in case we are posting the reaction this will be not null
                        Reaction postedReaction;
                        if (!movieListFullViewModel.getReactionHandler().getPostedReaction().isEmpty()) {
                            postedReaction = movieListFullViewModel.getReactionHandler().getPostedReaction().get(0);
                            this.loggedUserReaction = new LoggedUserReaction(postedReaction.getId(), postedReaction.getType());
                        }
                    } else if (backendOperationResult == BackendOperationResult.SERVER_UNREACHABLE) {
                        reactionsPopupWindow.dismiss();
                        serverUnreachableSnackbar.show();
                        clearSelectedReactionBackgrounds();
                        reactionsButton.setText(Integer.toString(currentReactionsCount));
                        setReactionsIconWhite();
                        if (loggedUserReaction != null) {
                            selectedReactionBackgrounds.get(loggedUserReaction.getType().ordinal()).setVisibility(View.VISIBLE);
                            setReactionsIconYellow();
                        }
                        movieListFullViewModel.getReactionHandler().resetReactionOperationResult();
                    }
                });

        Log.e(TAG, "opened full movielist with id " + this.movieListId);

        return view;
    }


    private void setupBackIcon(View view) {
        ImageView backIconImageView = view.findViewById(R.id.other_user_movie_list_back_icon);
        backIconImageView.setOnClickListener(v -> this.goBack());
    }

    private void setupListNameTextView(MovieListFull movieListFull) {
        if (movieListFull.getType() == ListType.Custom)
            listNameTextView.setText(movieListFull.getName());
        else {
            int nameStringId = movieListFull.getType() == ListType.ToWatch ? R.string.to_watch_list_title : R.string.favorites_list_title;
            listNameTextView.setText(getString(nameStringId));
        }
    }


    private void setupRecyclerView(View view) {
        moviePostersRecyclerView = view.findViewById(R.id.other_user_movie_list_recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 3);

        int spanCount = 3; // 3 columns
        int spacing = 50; // 50px
        moviePostersRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));


        moviePostersRecyclerView.setLayoutManager(gridLayoutManager);


        OnClickListenerGenerator<Integer> onClickListenerGenerator = tmdbMovieId -> v -> {
            TmDbRetrofitService.getTmDbDaoInstance().getMoviesDetailsById(tmdbMovieId)
                    .enqueue(TmDbCallbacks.movieDetailsCallBack(movieFullViewModel));
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_movie_full);
        };

        moviePostersAdapter = new MoviePostersAdapter(new ArrayList<>(), onClickListenerGenerator);

        moviePostersRecyclerView.setAdapter(moviePostersAdapter);
    }


    private void setupSnackbars() {
        serverUnreachableSnackbar = SnackbarUtils.failureAnchorSnackbar(requireActivity(), getString(R.string.snackbar_server_unreachable), movieListInteractionsBar);
        listCopySuccessSnackbar = SnackbarUtils.successAnchorSnackbar(requireActivity(), getString(R.string.copy_list_success), movieListInteractionsBar);
        listNameAlreadyTakenErrorSnackbar = SnackbarUtils.failureAnchorSnackbar(requireActivity(), getString(R.string.movie_list_name_already_taken), movieListInteractionsBar);
        noReactionsSnackbar = SnackbarUtils.failureAnchorSnackbar(requireActivity(), getString(R.string.no_reactions_to_this_list), movieListInteractionsBar);
    }

    private void setupReactionsCommentsRatingsButtons(View view, MovieListBasic movieList) {

        reactionsButton = view.findViewById(R.id.other_user_movie_list_reactions_button);
        reactionsButton.setOnClickListener(v -> {
            if (showcaseView != null)
                showcaseView.hide();

            reactionsPopupWindow.showAsDropDown(movieListInteractionsBar, 25, -290);
        });
        reactionsButton.setOnLongClickListener(v -> {
            if (showcaseView != null)
                showcaseView.hide();

            if (reactionsButton.getText().toString().equals("0"))
                this.noReactionsSnackbar.show();
            else {
                Bundle bundle = new Bundle();
                bundle.putParcelable("movieList", movieList);
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.navigation_reaction, bundle);
            }
            return true;
        });

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("reactionsTutorial", false)) {

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("reactionsTutorial", true);
            editor.apply();

            showcaseView = new ShowcaseView.Builder(requireActivity())
                    .setTarget(new ViewTarget(reactionsButton))
                    .setContentTitle(getString(R.string.tooltip_reactions_title))
                    .setContentText(R.string.tooltip_reactions_body)
                    .hideOnTouchOutside()
                    .build();
        }

        commentsButton = view.findViewById(R.id.other_user_movie_list_comments_button);
        commentsButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("movieList", movieList);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_comment, bundle);
        });

        ratingsButton = view.findViewById(R.id.other_user_movie_list_ratings_button);
        ratingsButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("movieList", movieList);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_rating, bundle);
        });
    }


    private void setupReactionsPopupWindow(View view, LayoutInflater inflater) {
        movieListInteractionsBar = view.findViewById(R.id.other_user_movie_list_interactions_bar);

        // inflate the layout of the popup window
        @SuppressLint("InflateParams") View reactionsPopupView = inflater.inflate(R.layout.reactions_popup_window, null);
        // dismiss the popup window when touched
        reactionsPopupView.setOnTouchListener((v, event) -> {
            reactionsPopupWindow.dismiss();
            return v.performClick();
        });

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        reactionsPopupWindow = new PopupWindow(reactionsPopupView, width, height, true); // if true taps outside the popup dismiss it

        reactionsPopupWindow.setElevation(20f);

        setupReactionsButtons(reactionsPopupView);
    }


    private void setupReactionsButtons(View view) {
        selectedReactionBackgrounds = new ArrayList<>();

        Resources resources = view.getResources();

        IntStream.range(0, 6).forEach(reactionIndex -> {
            int reactionBackgroundId = resources.getIdentifier("popup_window_reaction_" + reactionIndex + "_background", "id", view.getContext().getPackageName());
            int reactionIconId = resources.getIdentifier("popup_window_reaction_" + reactionIndex, "id", view.getContext().getPackageName());

            RelativeLayout reactionBackground = view.findViewById(reactionBackgroundId);
            selectedReactionBackgrounds.add(reactionBackground);

            ImageView reactionIcon = view.findViewById(reactionIconId);
            reactionIcon.setOnClickListener(v -> {

                FirebaseAnalytics.getInstance(requireContext()).logEvent("ReactionToList", null);

                boolean noOneWasPreviouslySelected = true;
                for (RelativeLayout r : selectedReactionBackgrounds)
                    if (r.getVisibility() == View.VISIBLE) noOneWasPreviouslySelected = false;

                boolean thisOneWasPreviouslySelected = false;
                if (!noOneWasPreviouslySelected)
                    thisOneWasPreviouslySelected = selectedReactionBackgrounds.get(reactionIndex).getVisibility() == View.VISIBLE;

                String reactionsCountString = this.reactionsButton.getText().toString();
                this.currentReactionsCount = Integer.parseInt(reactionsCountString);
                ReactionType selectedReaction = ReactionType.values()[reactionIndex];
                clearSelectedReactionBackgrounds();

                if (noOneWasPreviouslySelected) {
                    movieListFullViewModel.getReactionHandler().postReaction(selectedReaction, this.movieListId);
                    selectedReactionBackgrounds.get(reactionIndex).setVisibility(View.VISIBLE);
                    setReactionsIconYellow();
                    incrementReactionsCount();
                    Log.e(TAG, "calling post of " + selectedReaction + " reaction...");
                } else {
                    if (thisOneWasPreviouslySelected) {
                        movieListFullViewModel.getReactionHandler().deleteReaction(this.loggedUserReaction.getReactionId());
                        setReactionsIconWhite();
                        decrementReactionsCount();
                        Log.e(TAG, "calling delete of " + selectedReaction + " reaction...");
                    } else {
                        movieListFullViewModel.getReactionHandler().putReaction(this.loggedUserReaction.getReactionId(), this.movieListId, selectedReaction);
                        selectedReactionBackgrounds.get(reactionIndex).setVisibility(View.VISIBLE);
                        setReactionsIconYellow();
                        Log.e(TAG, "calling put of " + selectedReaction + " reaction...");
                    }
                }

            });

        });
    }

    private void setReactionsIconYellow() {
        reactionsButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.yellow_button));
        reactionsButton.setIconTintResource(R.color.yellow_button);
    }

    private void setReactionsIconWhite() {
        reactionsButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        reactionsButton.setIconTintResource(R.color.white);
    }

    private void decrementReactionsCount() {
        String reactionsCountString = this.reactionsButton.getText().toString();
        int reactionCount = Integer.parseInt(reactionsCountString) - 1;
        reactionsCountString = Integer.toString(reactionCount);
        this.reactionsButton.setText(reactionsCountString);
    }

    private void incrementReactionsCount() {
        String reactionsCountString = this.reactionsButton.getText().toString();
        int reactionCount = Integer.parseInt(reactionsCountString) + 1;
        reactionsCountString = Integer.toString(reactionCount);
        this.reactionsButton.setText(reactionsCountString);
    }


}