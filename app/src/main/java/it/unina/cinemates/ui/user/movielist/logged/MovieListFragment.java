package it.unina.cinemates.ui.user.movielist.logged;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import it.unina.cinemates.R;
import it.unina.cinemates.models.Reaction;
import it.unina.cinemates.retrofit.backend.enums.BackendOperationResult;
import it.unina.cinemates.retrofit.tmdb.TmDbCallbacks;
import it.unina.cinemates.retrofit.tmdb.TmDbRetrofitService;
import it.unina.cinemates.ui.common.interfaces.OnClickListenerGenerator;
import it.unina.cinemates.ui.common.recyclerview.GridSpacingItemDecoration;
import it.unina.cinemates.ui.common.utils.AlertDialogUtils;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.ui.user.movielist.common.recyclerview.MoviePostersAdapter;
import it.unina.cinemates.ui.user.movielist.common.recyclerview.selection.MoviePosterDetailsLookup;
import it.unina.cinemates.ui.user.movielist.common.recyclerview.selection.MoviePosterKeyProvider;
import it.unina.cinemates.viewmodels.MovieFullViewModel;
import it.unina.cinemates.viewmodels.user.customMovielists.CustomListsViewModel;
import it.unina.cinemates.viewmodels.user.movielist.common.MovieListFullViewModel;
import it.unina.cinemates.views.backend.LoggedUserReaction;
import it.unina.cinemates.views.backend.MovieListBasic;
import it.unina.cinemates.views.backend.MovieListFull;
import it.unina.cinemates.views.backend.enums.ListType;
import it.unina.cinemates.views.backend.enums.ReactionType;


public class MovieListFragment extends Fragment {

    private static final String TAG = "LOGGEDUSERMOVIELIST_FRAGMENT";

    private MovieListFullViewModel movieListFullViewModel;
    private CustomListsViewModel customListsViewModel;

    private int movieListId;

    private MovieFullViewModel movieFullViewModel;

    private RecyclerView moviePostersRecyclerView;
    private MoviePostersAdapter moviePostersAdapter;
    private SelectionTracker<Long> selectionTracker;

    private ActionMode actionMode;
    private AlertDialog deleteMoviesDialog;
    private Snackbar deleteMoviesSuccessSnackbar;
    private Snackbar serverUnreachableSnackbar;

    private MaterialButton reactionsButton;
    private Button commentsButton;
    private Button ratingsButton;
    private PopupWindow reactionsPopupWindow;
    private ConstraintLayout movieListInteractionsBar;
    private List<RelativeLayout> selectedReactionBackgrounds;
    private LoggedUserReaction loggedUserReaction;
    private int currentReactionsCount;


    private boolean deleteFromBackendSucceeded;


    private ActionMode.Callback deleteMoviesActionModeCallback;


    private AlertDialog deleteMovieListDialog;


    private EditText renameListEditTextField;
    private AlertDialog renameMovieListDialog;
    private Snackbar renameMovieListSuccessSnackbar;
    private TextView listNameTextView;
    private String newListName;
    private Snackbar listNameLengthErrorSnackbar;
    private Snackbar sameNameErrorSnackbar;
    private Snackbar noReactionsSnackbar;
    private ConstraintLayout emptyListLayout;

    private ShowcaseView showcaseView;


    boolean emptyAfterDeleteMovies = false;
    private MovieListBasic movieListBasic;

    public MovieListFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movieListFullViewModel = new ViewModelProvider(requireActivity()).get(MovieListFullViewModel.class);
        customListsViewModel = new ViewModelProvider(requireActivity()).get(CustomListsViewModel .class);
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

        View view = inflater.inflate(R.layout.fragment_logged_user_movie_list, container, false);

        Bundle bundle = getArguments();
        assert bundle != null;
        this.movieListId = (Integer) bundle.get("movieListId");


        emptyListLayout = view.findViewById(R.id.logged_user_movie_list_no_movies_layout);
        emptyListLayout.setVisibility(View.INVISIBLE);
        emptyListLayout.findViewById(R.id.logged_user_movie_list_go_to_search_button).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_search);
            emptyListLayout.setVisibility(View.INVISIBLE);
        });

        setupRenameListEditText();

        setupBackIcon(view);

        listNameTextView = view.findViewById(R.id.logged_user_movie_list_name);

        setupRecyclerView(view);

        setupDeleteMoviesDialog();

        setupReactionsPopupWindow(view, inflater);

        setupSnackbars();


        movieListFullViewModel.requestFullMovieList(movieListId);


        movieListFullViewModel.getMovieListFull().observe(getViewLifecycleOwner(), movieListFull -> {

            emptyListLayout.setVisibility(View.INVISIBLE);

            if (movieListFull != null) {
                setupListNameTextView(movieListFull);

                setupMoreOptionsMenu(view, movieListFull.getType());
                this.movieListBasic = new MovieListBasic(movieListFull);
                setupReactionsCommentsRatingsButtons(view, this.movieListBasic);

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


        movieListFullViewModel.getMoviesInMovieList().observe(getViewLifecycleOwner(), moviePosters -> {
            moviePostersAdapter.loadMoviePosters(moviePosters);
            moviePostersRecyclerView.setVisibility(View.VISIBLE);
        });


        customListsViewModel.getDeleteMovieListResult().observe(getViewLifecycleOwner(), backendOperationResult -> {
            if (backendOperationResult == BackendOperationResult.SUCCESS) {
                this.goBack();
            } else if (backendOperationResult == BackendOperationResult.SERVER_UNREACHABLE) {
                serverUnreachableSnackbar.show();
                customListsViewModel.resetDeleteMovieListResult();
            }
        });


        movieListFullViewModel.getRenameMovieListResult().observe(getViewLifecycleOwner(), backendOperationResult -> {
            if (backendOperationResult == BackendOperationResult.SUCCESS) {
                renameMovieListDialog.dismiss();
                movieListInteractionsBar.setVisibility(View.VISIBLE);

                Objects.requireNonNull(movieListFullViewModel.getMovieListFull().getValue()).setName(newListName);

                movieListFullViewModel.resetRenameMovieListResult();
                renameMovieListSuccessSnackbar.show();

                listNameTextView.setText(newListName);

                renameListEditTextField.setText(newListName);
                renameListEditTextField.setSelection(newListName.length());

                this.movieListBasic.setName(newListName);

            } else if (backendOperationResult == BackendOperationResult.SERVER_UNREACHABLE) {
                renameMovieListDialog.dismiss();
                movieListInteractionsBar.setVisibility(View.VISIBLE);

                serverUnreachableSnackbar.show();
                movieListFullViewModel.resetRenameMovieListResult();

                renameListEditTextField.setText("");
            } else if (backendOperationResult == BackendOperationResult.ALREADY_PRESENT) {
                SnackbarUtils.failureAnchorSnackbar(requireActivity(), getString(R.string.movie_list_name_already_taken), movieListInteractionsBar).show();
                movieListFullViewModel.resetRenameMovieListResult();
            }
        });


        movieListFullViewModel.getDeleteMoviesFromListResult().observe(getViewLifecycleOwner(), backendOperationResult -> {
            if (!emptyAfterDeleteMovies)
                emptyListLayout.setVisibility(View.INVISIBLE);
            else
                emptyAfterDeleteMovies = false;

            if (backendOperationResult == BackendOperationResult.SUCCESS) {

                deleteFromBackendSucceeded = true;

                if (actionMode != null)
                    actionMode.finish();

                if (moviePostersAdapter.getMoviePosters().isEmpty()) {
                    emptyAfterDeleteMovies = true;
                    emptyListLayout.setVisibility(View.VISIBLE);
                }

                movieListFullViewModel.resetDeleteFromMovieListResult();
            } else if (backendOperationResult == BackendOperationResult.SERVER_UNREACHABLE) {

                if (actionMode != null)
                    actionMode.finish();

                serverUnreachableSnackbar.show();
                movieListFullViewModel.resetDeleteFromMovieListResult();
            }
        });


        movieListFullViewModel.getReactionHandler().getReactionOperationResult().observe(getViewLifecycleOwner(), backendOperationResult -> {
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
                if(loggedUserReaction != null){
                    selectedReactionBackgrounds.get(loggedUserReaction.getType().ordinal()).setVisibility(View.VISIBLE);
                    setReactionsIconYellow();
                }
                movieListFullViewModel.getReactionHandler().resetReactionOperationResult();
            }
        });


        Log.e(TAG, "opened full movielist with id " + this.movieListId);

        return view;
    }

    private void setupRenameListEditText() {
        renameListEditTextField = new EditText(this.getContext());
        renameListEditTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                newListName = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        renameListEditTextField.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        renameListEditTextField.setTextColor(Color.WHITE);
        renameListEditTextField.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.create_movie_list_edit_text_background));
    }


    private void setupBackIcon(View view) {
        ImageView backIconImageView = view.findViewById(R.id.logged_user_movie_list_back_icon);
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

    @SuppressLint("NonConstantResourceId")
    private void setupMoreOptionsMenu(View view, ListType listType) {
        ImageView moreOptionsIcon = view.findViewById(R.id.logged_user_movie_list_more_option_icon);

        PopupMenu popup = new PopupMenu(requireContext(), moreOptionsIcon);
        if (listType == ListType.Custom) {
            popup.getMenuInflater().inflate(R.menu.logged_user_custom_movie_list_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                Log.e("TAG", "settando i listener al moreOptions popup...");
                switch (item.getItemId()) {
                    case R.id.logged_user_movielist_rename_menu_item:

                        movieListInteractionsBar.setVisibility(View.INVISIBLE);

                        String currentListName = Objects.requireNonNull(movieListFullViewModel.getMovieListFull().getValue()).getName();

                        renameListEditTextField.setText(currentListName);
                        if (this.renameMovieListDialog == null) {
                            this.renameMovieListDialog = AlertDialogUtils.alertDialogWithTextEdit(
                                    requireContext(),
                                    renameListEditTextField,
                                    R.string.logged_user_movielist_rename,
                                    R.string.rename_movie_list_positive_button
                            );
                        }
                        renameMovieListDialog.show();

                        Button positiveButton = renameMovieListDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                        renameListEditTextField.setSelection(renameListEditTextField.getText().length());
                        positiveButton.setOnClickListener(v -> {
                            newListName = newListName.trim();
                            renameListEditTextField.setText(newListName);

                            if (newListName.equals(currentListName))
                                sameNameErrorSnackbar.show();
                            else if (!newListName.isEmpty() && newListName.length() < 26)
                                movieListFullViewModel.requestRenameMovieList(movieListId, newListName);
                            else
                                listNameLengthErrorSnackbar.show();
                        });
                        Button negativeButton = renameMovieListDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        negativeButton.setOnClickListener(v -> {
                            renameMovieListDialog.dismiss();
                            renameListEditTextField.setText("");
                            movieListInteractionsBar.setVisibility(View.VISIBLE);
                        });

                        break;
                    case R.id.logged_user_movielist_delete_movies_menu_item:
                        startDeleteMoviesActionMode();
                        break;
                    case R.id.logged_user_movielist_delete_list_menu_item:
                        if (deleteMovieListDialog == null) {
                            deleteMovieListDialog = AlertDialogUtils.alertDialog(
                                    requireContext(),
                                    R.string.delete_movie_list_dialog_title,
                                    R.string.irreversible_action_dialog_message,
                                    R.string.delete,
                                    (dialog, which) -> customListsViewModel.requestDeleteMovieList(movieListId)
                            );
                        }
                        deleteMovieListDialog.show();
                        break;
                }
                return true;
            });
        } else {
            popup.getMenuInflater().inflate(R.menu.logged_user_movie_list_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                Log.e("TAG", "settando i listener al moreOptions popup...");
                if (item.getItemId() == R.id.logged_user_movielist_delete_movies_menu_item) {
                    startDeleteMoviesActionMode();
                }
                return true;
            });
        }

        moreOptionsIcon.setOnClickListener(v -> popup.show());
    }

    private void setupRecyclerView(View view) {
        this.moviePostersRecyclerView = view.findViewById(R.id.logged_user_movie_list_recycler_view);
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

        setupRecyclerSelectionTracker();
    }

    private void setupRecyclerSelectionTracker() {

        selectionTracker = new SelectionTracker.Builder<>(
                "movieposter-tracker",
                moviePostersRecyclerView,
                new MoviePosterKeyProvider(moviePostersAdapter),
                new MoviePosterDetailsLookup(moviePostersRecyclerView),
                StorageStrategy.createLongStorage()
        ).build();

        moviePostersAdapter.setSelectionTracker(selectionTracker);


        deleteMoviesActionModeCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater menuInflater = requireActivity().getMenuInflater();
                menuInflater.inflate(R.menu.cab_movieposters_menu, menu);
                movieListInteractionsBar.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.delete_movies_from_list) {
                    Log.e(TAG, "deleting movies from movielist...");
                    if (selectionTracker.getSelection().size() == 1)
                        deleteMovies();
                    else
                        deleteMoviesDialog.show();
                    return true;
                }
                return false;
            }

            /*  CALLED IN FOLLOWING CASES:
            - User deselects all items
            - User presses back button
            - User presses delete button on contextual action bar
            - calling programmatically ActionMode.finish()
            */
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                actionMode = null;
                selectionTracker.clearSelection();
                movieListInteractionsBar.setVisibility(View.VISIBLE);
                moviePostersAdapter.exitSelectionMode();

                if (deleteFromBackendSucceeded) {    //refresh elements inside recycler so that we get animations
                    movieListFullViewModel.deleteMoviesFromCurrentList();
                    moviePostersAdapter.loadMoviePosters(movieListFullViewModel.getMoviesInMovieList().getValue());

                    deleteMoviesSuccessSnackbar.show();

                    deleteFromBackendSucceeded = false;
                }
            }
        };


        selectionTracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onSelectionChanged() {
                if (selectionTracker.hasSelection()) {
                    startDeleteMoviesActionMode();
                } else {
                    if (actionMode != null)
                        actionMode.finish();
                }
            }


        });

    }

    private void startDeleteMoviesActionMode() {
        if (emptyListLayout.getVisibility() == View.VISIBLE) {
            SnackbarUtils.failureAnchorSnackbar(requireActivity(), getString(R.string.empty_list_movie_delete_error), movieListInteractionsBar).show();
            return;
        }

        //deleteFromBackendSucceeded is used to prevent recreating the action mode when user presses delete on dialog box
        if (actionMode == null && !deleteFromBackendSucceeded) {
            actionMode = requireActivity().startActionMode(deleteMoviesActionModeCallback);
            moviePostersAdapter.enterSelectionMode();
            if (selectionTracker.getSelection().isEmpty())
                selectionTracker.select(Long.valueOf(Objects.requireNonNull(movieListFullViewModel.getMoviesInMovieList().getValue()).get(0).getId()));
        }
        updateContextualActionBarTitle();
    }

    private void updateContextualActionBarTitle() {
        if (actionMode != null)
            actionMode.setTitle(getString(R.string.selected_items_contextual_action_bar) + " " + selectionTracker.getSelection().size());
    }


    private void setupDeleteMoviesDialog() {
        int dialogTitleId = R.string.delete_movies_dialog_title;
        int dialogMessageId = R.string.delete_movies_dialog_message;
        int dialogPositiveButtonId = R.string.delete_movies_dialog_positive_button;
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> deleteMovies();
        deleteMoviesDialog = AlertDialogUtils.alertDialog(requireContext(), dialogTitleId, dialogMessageId, dialogPositiveButtonId, dialogClickListener);
    }


    private void deleteMovies() {
        Iterable<Long> ids = selectionTracker.getSelection();
        movieListFullViewModel.requestDeleteMovies(movieListId, ids);
    }

    private void setupSnackbars() {
        this.deleteMoviesSuccessSnackbar = SnackbarUtils.successAnchorSnackbar(requireActivity(), getString(R.string.delete_movies_from_list_success), movieListInteractionsBar);
        this.serverUnreachableSnackbar = SnackbarUtils.failureAnchorSnackbar(requireActivity(), getString(R.string.snackbar_server_unreachable), movieListInteractionsBar);
        this.renameMovieListSuccessSnackbar = SnackbarUtils.successAnchorSnackbar(requireActivity(), getString(R.string.rename_movie_list_success), movieListInteractionsBar);
        this.listNameLengthErrorSnackbar = SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.movie_list_name_length_error));
        this.sameNameErrorSnackbar = SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.movie_list_same_name_error));
        this.noReactionsSnackbar = SnackbarUtils.failureAnchorSnackbar(requireActivity(), getString(R.string.no_reactions_to_this_list), movieListInteractionsBar);
    }


    private void setupReactionsCommentsRatingsButtons(View view, MovieListBasic movieList) {

        reactionsButton = view.findViewById(R.id.logged_user_movie_list_reactions_button);
        reactionsButton.setOnClickListener(v -> {
            if (showcaseView != null)
                showcaseView.hide();

            reactionsPopupWindow.showAsDropDown(movieListInteractionsBar, 25, -290);
        });
        reactionsButton.setOnLongClickListener(v -> {
            if (showcaseView != null)
                showcaseView.hide();

            if(reactionsButton.getText().toString().equals("0"))
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

        commentsButton = view.findViewById(R.id.logged_user_movie_list_comments_button);
        commentsButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("movieList", movieList);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_comment, bundle);
        });

        ratingsButton = view.findViewById(R.id.logged_user_movie_list_ratings_button);
        ratingsButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("movieList", movieList);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_rating, bundle);
        });
    }


    private void setupReactionsPopupWindow(View view, LayoutInflater inflater) {
        movieListInteractionsBar = view.findViewById(R.id.logged_user_movie_list_interactions_bar);

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
        int currentValue = Integer.parseInt(reactionsCountString);
        int reactionCount = currentValue - 1;
        reactionsCountString = Integer.toString(reactionCount);
        this.reactionsButton.setText(reactionsCountString);
    }


    private void incrementReactionsCount() {
        String reactionsCountString = this.reactionsButton.getText().toString();
        int currentValue = Integer.parseInt(reactionsCountString);
        int reactionCount = currentValue + 1;
        reactionsCountString = Integer.toString(reactionCount);
        this.reactionsButton.setText(reactionsCountString);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        movieListFullViewModel.resetAllData();
    }

}