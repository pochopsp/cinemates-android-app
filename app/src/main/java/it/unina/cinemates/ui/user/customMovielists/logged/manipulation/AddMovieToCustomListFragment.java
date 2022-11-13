package it.unina.cinemates.ui.user.customMovielists.logged.manipulation;


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
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unina.cinemates.MainActivity;
import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.ui.common.recyclerview.ItemDecorationWithInitialSpace;
import it.unina.cinemates.ui.user.customMovielists.common.recyclerview.MovieListPreviewAdapter;
import it.unina.cinemates.viewmodels.HomeViewModel;
import it.unina.cinemates.viewmodels.user.customMovielists.CustomListsViewModel;
import it.unina.cinemates.viewmodels.user.movielist.logged.AddMovieToMovieListViewModel;
import it.unina.cinemates.views.backend.MovieListPreview;

public class AddMovieToCustomListFragment extends Fragment {

    //private static final String TAG = "ADDTOCUSTOMMOVIELIST_FRAGMENT";

    private HomeViewModel homeViewModel;
    private AddMovieToMovieListViewModel addMovieToMovieListViewModel;
    private int movieToBeAddedId;

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

    private ConstraintLayout noMovieListFoundLayout;
    private TextInputEditText searchEditText;
    private TextView titleTextView;
    private ImageView backIcon;


    public AddMovieToCustomListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customListsViewModel = new ViewModelProvider(requireActivity()).get(CustomListsViewModel.class);
        addMovieToMovieListViewModel = new ViewModelProvider(requireActivity()).get(AddMovieToMovieListViewModel.class);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                AddMovieToCustomListFragment.this.onBackPressed();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_to_custom_movie_list, container, false);

        //retrieve and set the movieId to be added to a list
        Bundle bundle = getArguments();
        assert bundle != null;
        this.movieToBeAddedId = (Integer) bundle.get("movieId");

        titleTextView = view.findViewById(R.id.add_to_custom_movie_list_title_text_view);

        ConstraintLayout addToCustomMovieListListsLayout = view.findViewById(R.id.addtocustommovielist_lists_layout);
        noMovieListFoundLayout = view.findViewById(R.id.add_to_custom_list_no_search_result_layout);

        customListsViewModel.getCreateMovieListResult().observe(getViewLifecycleOwner(), createMovieListResult -> {
            switch (createMovieListResult) {
                case SUCCESS: {

                    FirebaseAnalytics.getInstance(requireContext()).logEvent("AddedMovieToCustomList", null);

                    MovieListPreview createdMovieList = customListsViewModel.getLastCreatedList();

                    addMovieToMovieListViewModel.addToMovieList(createdMovieList.getId(), movieToBeAddedId);

                    customListsViewModel.resetCreateMovieListStatus();

                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigateUp();
                    break;
                }
                case SERVER_UNREACHABLE: {
                    addMovieToMovieListViewModel.setServerUnreachableForInsertMovieInMovielistResult();
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigateUp();

                    customListsViewModel.resetCreateMovieListStatus();
                }
            }
        });

        setupBackIconButton(view);

        setupSearchEditText(view);

        setupRecyclerView(view);

        currentPage = 1;
        currentQuery = "";
        customListsViewModel.resetMovieListsFetch(false);
        customListsViewModel.fetchMovieLists(LoggedUser.getInstance().getUsername(), currentQuery, currentPage);

        Button createListButton = view.findViewById(R.id.add_to_custom_movie_list_create_new_list_button);
        createListButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_create_movie_list);
        });


        customListsViewModel.getCustomMovieLists().observe(getViewLifecycleOwner(), movieListPreviews -> {
            if (movieListPreviews == null)
                return;

            if (movieListPreviews.size() > 0) {
                movieListPreviewAdapter.loadMovieLists(movieListPreviews);
                movieListPreviewAdapter.notifyDataSetChanged();
                loading.set(false);
                makelistsPresentLayoutVisible(addToCustomMovieListListsLayout);
            }
        });

        customListsViewModel.getListsRetrieveResult().observe(getViewLifecycleOwner(), listsRetrieveResult -> {
            if (listsRetrieveResult == CustomListsViewModel.ListsRetrieveResult.NO_LISTS_IN_SEARCH) {
                view.requestFocus();
                movieListPreviewAdapter.loadMovieLists(new ArrayList<>());
                movieListPreviewAdapter.notifyDataSetChanged();
                loading.set(false);

                noMovieListFoundLayout.setVisibility(View.VISIBLE);
                customListsViewModel.resetNoListsRetrieved();
            } else if (listsRetrieveResult == CustomListsViewModel.ListsRetrieveResult.NO_LISTS_AT_ALL) {
                Bundle createListBundle = new Bundle();
                createListBundle.putBoolean("fromBottomSheet", true);
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.navigation_create_movie_list, createListBundle);
                customListsViewModel.resetNoListsRetrieved();
            }
        });


        return view;
    }

    private void makelistsPresentLayoutVisible(ConstraintLayout addToCustomMovieListListsLayout) {
        addToCustomMovieListListsLayout.setVisibility(View.VISIBLE);
        titleTextView.setVisibility(View.VISIBLE);
        backIcon.setVisibility(View.VISIBLE);
        noMovieListFoundLayout.setVisibility(View.INVISIBLE);
    }


    private void setupBackIconButton(View view) {
        backIcon = view.findViewById(R.id.add_to_custom_movie_list_back_icon);

        backIcon.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
            homeViewModel.setBackFromMovieFullOrInsertList(true);
        });
    }


    private void setupSearchEditText(View view) {
        View verticalLine = view.findViewById(R.id.add_to_custom_movie_list_vertical_line);
        Button clearIcon = view.findViewById(R.id.add_to_custom_movie_list_clear_icon);
        clearIcon.setOnClickListener(v -> {
            clearIcon.setVisibility(View.INVISIBLE);
            verticalLine.setVisibility(View.INVISIBLE);
            searchEditText.setText("");
            searchQuery = "";
        });

        if (!searchQuery.equals("")) {
            clearIcon.setVisibility(View.VISIBLE);
            verticalLine.setVisibility(View.VISIBLE);
        }

        searchEditText = view.findViewById(R.id.add_to_custom_movie_list_search_edit_text);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals("")) {
                    clearIcon.setVisibility(View.VISIBLE);
                    verticalLine.setVisibility(View.VISIBLE);
                } else {
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

        Button searchIcon = view.findViewById(R.id.add_to_custom_movie_list_search_icon);
        searchIcon.setOnClickListener(v -> performSearch(view));
    }

    private void performSearch(View view) {
        if (!searchQuery.equals(currentQuery)) {

            String searchedString = searchQuery.trim();
            searchEditText.setText(searchedString);
            searchEditText.setSelection(Objects.requireNonNull(searchEditText.getText()).length());

            currentQuery = searchQuery;
            currentPage = 1;
            customListsViewModel.resetMovieListsFetch(true);
            customListsViewModel.fetchMovieLists(LoggedUser.getInstance().getUsername(), currentQuery, currentPage);
            ((MainActivity) requireActivity()).closeKeyboard(view);
        }
    }

    private void setupRecyclerView(View view) {
        movieListsRecyclerView = view.findViewById(R.id.add_to_custom_movie_list_recyclerview);
        LinearLayoutManager linearLayout = new LinearLayoutManager(requireContext());
        movieListsRecyclerView.addItemDecoration(new ItemDecorationWithInitialSpace(54, 60));
        movieListsRecyclerView.setLayoutManager(linearLayout);

        movieListPreviewAdapter = new MovieListPreviewAdapter(
                new ArrayList<>(),
                movieList -> v -> {

                    FirebaseAnalytics.getInstance(requireContext()).logEvent("AddedMovieToCustomList", null);

                    AddMovieToCustomListFragment.this.addMovieToMovieListViewModel.addToMovieList(movieList.getId(), AddMovieToCustomListFragment.this.movieToBeAddedId);
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigateUp();
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

    private void onBackPressed() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigateUp();
        homeViewModel.setBackFromMovieFullOrInsertList(true);
    }

}
