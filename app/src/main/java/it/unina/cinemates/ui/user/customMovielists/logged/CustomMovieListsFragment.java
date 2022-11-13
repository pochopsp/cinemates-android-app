package it.unina.cinemates.ui.user.customMovielists.logged;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unina.cinemates.MainActivity;
import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.retrofit.backend.enums.BackendOperationResult;
import it.unina.cinemates.ui.common.recyclerview.ItemDecorationWithInitialSpace;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.ui.user.customMovielists.common.recyclerview.MovieListPreviewAdapter;
import it.unina.cinemates.ui.user.customMovielists.logged.manipulation.DeleteMovieListBottomSheet;
import it.unina.cinemates.viewmodels.user.customMovielists.CustomListsViewModel;

public class CustomMovieListsFragment extends Fragment {

    private static final String TAG = "LOGGEDUSEROTHERLISTS_FRAGMENT";

    private CustomListsViewModel customListsViewModel;

    //RECYCLER VIEW FIELDS
    private MovieListPreviewAdapter movieListPreviewAdapter;
    private RecyclerView movieListsRecyclerView;

    //RECYCLER VIEW LOGIC FIELDS
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private final AtomicBoolean loading = new AtomicBoolean(true);
    private int currentPage = 1;
    private String currentQuery = "";
    private String searchQuery = "";

    //SNACKBARS
    private Snackbar createMovieListSuccessSnackbar;
    private Snackbar deleteMovieListSuccessSnackbar;
    private Snackbar serverUnreachableSnackbar;


    private ExtendedFloatingActionButton createListFAB;
    private DeleteMovieListBottomSheet deleteMovieListBottomSheet;
    private int movieListToBeDeletedId;

    private CoordinatorLayout noMovieListPresentLayout;
    private ConstraintLayout noMovieListFoundLayout;

    private TextInputEditText searchEditText;
    private ConstraintLayout movieListsPresentLayout;
    private ExtendedFloatingActionButton createListOnEmptyLayoutFAB;

    public CustomMovieListsFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customListsViewModel = new ViewModelProvider(requireActivity()).get(CustomListsViewModel.class);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logged_user_other_lists, container, false);

        noMovieListFoundLayout = view.findViewById(R.id.logged_user_other_lists_no_search_result_layout);
        noMovieListPresentLayout = view.findViewById(R.id.logged_user_other_lists_no_movie_list_present_layout);
        movieListsPresentLayout = view.findViewById(R.id.logged_user_other_lists_movie_lists_present_layout);

        setupBackIconButton(view);

        setupSearchEditText(view);

        setupRecyclerView(view);

        setupSnackbars();

        setupFAB(view);

        resetMovieListsFetch();
        customListsViewModel.fetchMovieLists(LoggedUser.getInstance().getUsername(), currentQuery, currentPage);

        customListsViewModel.getCustomMovieLists().observe(getViewLifecycleOwner(), movieListPreviews -> {
            if (movieListPreviews == null)
                return;

            if (movieListPreviews.size() > 0) {
                showListsGui();
                view.requestFocus();
                movieListPreviewAdapter.loadMovieLists(movieListPreviews);
                movieListPreviewAdapter.notifyDataSetChanged();
                loading.set(false);
            }
        });

        customListsViewModel.getListsRetrieveResult().observe(getViewLifecycleOwner(), listsRetrieveResult -> {
            if (listsRetrieveResult == CustomListsViewModel.ListsRetrieveResult.NO_LISTS_AT_ALL) {
                showNoListsGui();
                customListsViewModel.resetNoListsRetrieved();
            } else if (listsRetrieveResult == CustomListsViewModel.ListsRetrieveResult.NO_LISTS_IN_SEARCH) {
                showListsGui();
                view.requestFocus();
                movieListPreviewAdapter.loadMovieLists(new ArrayList<>());
                movieListPreviewAdapter.notifyDataSetChanged();
                loading.set(false);

                showNoListsFoundGui();
                customListsViewModel.resetNoListsRetrieved();
            }
        });

        customListsViewModel.getCreateMovieListResult().observe(getViewLifecycleOwner(), createMovieListResult -> {
            if (createMovieListResult == BackendOperationResult.SUCCESS) {

                moveUpFABsWithAnimation();

                createMovieListSuccessSnackbar.show();

                customListsViewModel.resetCreateMovieListStatus();
            }
        });

        customListsViewModel.getDeleteMovieListResult().observe(getViewLifecycleOwner(), backendOperationResult ->
        {
            if (backendOperationResult == BackendOperationResult.SUCCESS) {
                if (deleteMovieListBottomSheet != null)
                    deleteMovieListBottomSheet.dismiss();

                customListsViewModel.resetDeleteMovieListResult();

                movieListPreviewAdapter.removeList(movieListToBeDeletedId);

                if (movieListPreviewAdapter.getItemCount() == 0) {
                    showNoListsGui();
                    moveUpFABsWithAnimation();
                    resetMovieListsFetch();
                }

                deleteMovieListSuccessSnackbar.show();

            } else if (backendOperationResult == BackendOperationResult.SERVER_UNREACHABLE) {
                deleteMovieListBottomSheet.dismiss();
                createListFAB.setVisibility(View.VISIBLE);
                moveUpFABsWithAnimation();
                serverUnreachableSnackbar.show();
                customListsViewModel.resetDeleteMovieListResult();
            }

        });

        return view;
    }

    private void showNoListsFoundGui() {
        noMovieListPresentLayout.setVisibility(View.INVISIBLE);
        noMovieListFoundLayout.setVisibility(View.VISIBLE);
    }

    private void showNoListsGui() {
        movieListsPresentLayout.setVisibility(View.INVISIBLE);
        noMovieListFoundLayout.setVisibility(View.INVISIBLE);
        noMovieListPresentLayout.setVisibility(View.VISIBLE);
    }

    private void showListsGui() {
        noMovieListFoundLayout.setVisibility(View.INVISIBLE);
        noMovieListPresentLayout.setVisibility(View.INVISIBLE);
        movieListsPresentLayout.setVisibility(View.VISIBLE);
    }

    private void resetMovieListsFetch() {
        currentPage = 1;
        currentQuery = "";
        customListsViewModel.resetMovieListsFetch(false);
    }

    private void setupSearchEditText(View view) {
        View verticalLine = view.findViewById(R.id.logged_user_other_lists_vertical_line);
        Button clearIcon = view.findViewById(R.id.logged_user_other_lists_clear_icon);
        clearIcon.setOnClickListener(v -> {
            clearIcon.setVisibility(View.INVISIBLE);
            verticalLine.setVisibility(View.INVISIBLE);
            searchEditText.setText("");
            searchQuery = "";
        });

        if(!searchQuery.equals("")){
            clearIcon.setVisibility(View.VISIBLE);
            verticalLine.setVisibility(View.VISIBLE);
        }

        searchEditText = view.findViewById(R.id.logged_user_other_lists_search_edit_text);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals("")) {
                    clearIcon.setVisibility(View.VISIBLE);
                    verticalLine.setVisibility(View.VISIBLE);
                }else{
                    clearIcon.setVisibility(View.INVISIBLE);
                    verticalLine.setVisibility(View.INVISIBLE);
                }
                searchQuery = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(view);
                return true;
            }
            return false;
        });

        Button searchIcon = view.findViewById(R.id.logged_user_other_lists_search_icon);
        searchIcon.setOnClickListener(v -> performSearch(view));
    }

    private void performSearch(View view) {
        if (!searchQuery.equals(currentQuery)) {

            String searchedString = searchQuery.trim();
            searchEditText.setText(searchedString);

            currentQuery = searchQuery;
            currentPage = 1;
            customListsViewModel.resetMovieListsFetch(true);
            customListsViewModel.fetchMovieLists(LoggedUser.getInstance().getUsername(), currentQuery, currentPage);
            ((MainActivity) requireActivity()).closeKeyboard(view);
        }
    }


    private void setupSnackbars() {
        this.createMovieListSuccessSnackbar = SnackbarUtils.successSnackbar(requireActivity(), getString(R.string.create_movie_list_success));
        createMovieListSuccessSnackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, @DismissEvent int event) {
                moveDownFABsWithAnimation();
            }
        });
        this.deleteMovieListSuccessSnackbar = SnackbarUtils.successSnackbar(requireActivity(), getString(R.string.delete_movie_list_success));
        deleteMovieListSuccessSnackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, @DismissEvent int event) {
                createListFAB.setVisibility(View.VISIBLE);
                moveDownFABsWithAnimation();
            }
        });
        this.serverUnreachableSnackbar = SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.snackbar_server_unreachable));
        serverUnreachableSnackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, @DismissEvent int event) {
                moveDownFABsWithAnimation();
            }
        });
    }

    private void setupFAB(View view) {
        createListFAB = view.findViewById(R.id.logged_user_other_lists_add_list_fab);
        createListFAB.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_create_movie_list);
        });
        createListOnEmptyLayoutFAB = view.findViewById(R.id.logged_user_other_lists_no_lists_add_list_fab);
        createListOnEmptyLayoutFAB.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_create_movie_list);
        });
    }

    private void setupRecyclerView(View view) {
        movieListsRecyclerView = view.findViewById(R.id.logged_user_other_lists_recyclerview);
        LinearLayoutManager linearLayout = new LinearLayoutManager(requireContext());
        movieListsRecyclerView.addItemDecoration(new ItemDecorationWithInitialSpace(54, 60));
        movieListsRecyclerView.setLayoutManager(linearLayout);

        movieListPreviewAdapter = new MovieListPreviewAdapter(
                new ArrayList<>(),
                pressedMovieList -> v -> {
                    NavController navController = Navigation.findNavController(requireView());
                    Bundle bundle = new Bundle();
                    bundle.putInt("movieListId", pressedMovieList.getId());
                    navController.navigate(R.id.navigation_logged_user_movie_list, bundle);
                },
                longPressedMovieList -> v -> {
                    ImageView longPressedListOverlay = v.findViewById(R.id.long_pressed_movielist_overlay_imageview);
                    longPressedListOverlay.setVisibility(View.VISIBLE);
                    createListFAB.setVisibility(View.GONE);
                    movieListToBeDeletedId = longPressedMovieList.getId();
                    deleteMovieListBottomSheet = new DeleteMovieListBottomSheet(movieListToBeDeletedId, longPressedMovieList.getMoviesInList().isEmpty(), longPressedListOverlay, createListFAB);
                    deleteMovieListBottomSheet.show(requireActivity().getSupportFragmentManager(), deleteMovieListBottomSheet.getTag());
                    return true;
                }
        );
        movieListsRecyclerView.setAdapter(movieListPreviewAdapter);

        setupEndlessScrollLogic();
    }


    private void setupEndlessScrollLogic() {

        movieListsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                            customListsViewModel.fetchMovieLists(LoggedUser.getInstance().getUsername(), currentQuery, ++currentPage);
                        }
                    }
                }
            }
        });
    }


    private void setupBackIconButton(View view) {
        ImageView backIcon = view.findViewById(R.id.logged_user_other_lists_back_icon);

        backIcon.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
        });
    }


    private void moveUpFABsWithAnimation() {
        createListFAB.animate().setDuration(200).translationY(-170).start();
        createListOnEmptyLayoutFAB.animate().setDuration(200).translationY(-170).start();
    }

    private void moveDownFABsWithAnimation() {
        createListFAB.animate().setDuration(200).translationY(0).start();
        createListOnEmptyLayoutFAB.animate().setDuration(200).translationY(0).start();
    }


}