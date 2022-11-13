package it.unina.cinemates.ui.search;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.ui.common.recyclerview.ItemDecorationWithInitialSpace;
import it.unina.cinemates.ui.search.recyclerview.movie.MovieResultAdapter;
import it.unina.cinemates.ui.search.recyclerview.user.UserResultAdapter;
import it.unina.cinemates.viewmodels.SearchViewModel;
import it.unina.cinemates.viewmodels.enums.SearchType;
import it.unina.cinemates.views.backend.UserResult;
import it.unina.cinemates.views.tmdb.MovieResult;

public class SearchTabFragment extends Fragment {

    private SearchViewModel searchViewModel;
    private List<MovieResult> movieResults;
    private List<UserResult> userResults;

    private ImageView noResultMoviesImageView;
    private TextView noResultMoviesTextView;
    private TextView noResultMoviesSubtitleTextView;

    private ImageView noResultUsersImageView;
    private TextView noResultUsersTextView;
    private TextView noResultUsersSubtitleTextView;

    private ConstraintLayout movieTabConstraintLayout;
    private ConstraintLayout userTabConstraintLayout;

    private SearchType selectedSearch;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private final AtomicBoolean loading = new AtomicBoolean(false);
    private int currentPage = 1;

    private RecyclerView movieRecyclerView;
    private RecyclerView userRecyclerView;

    private MovieResultAdapter movieAdapter;
    private UserResultAdapter userAdapter;

    private Boolean firstSearchDone = false;

    public SearchTabFragment() {
    }

    public static SearchTabFragment newInstance() {
        return new SearchTabFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        searchViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        movieResults = new ArrayList<>();
        userResults = new ArrayList<>();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_tab, container, false);

        setupGui(view);

        searchViewModel.getSearchFoundMoviesLiveData().observe(getViewLifecycleOwner(), foundItems -> {
            if (foundItems)
                hideNoResultGuiMovies();
            else if (Objects.requireNonNull(movieRecyclerView.getLayoutManager()).getItemCount() == 0)
                showNoResultGuiMovies();
        });

        searchViewModel.getSearchFoundUsersLiveData().observe(getViewLifecycleOwner(), foundItems -> {
            if (foundItems)
                hideNoResultGuiUsers();
            else if (Objects.requireNonNull(userRecyclerView.getLayoutManager()).getItemCount() == 0)
                showNoResultGuiUsers();
        });

        searchViewModel.getUserSearchLiveData().observe(getViewLifecycleOwner(), userResultList -> {

            if (userResultList == null || userResultList.size() == 0) {
                return;
            }
            Parcelable currentState = searchViewModel.getUserRecyclerState();
            if (currentState != null)
                Objects.requireNonNull(userRecyclerView.getLayoutManager()).onRestoreInstanceState(currentState);
            if (!userResults.containsAll(userResultList))
                userResults.addAll(userResultList);
            userAdapter.notifyDataSetChanged();
        });

        searchViewModel.getMovieSearchLiveData().observe(getViewLifecycleOwner(), searchedMovie -> {
            loading.set(false);

            Parcelable currentState = searchViewModel.getMovieRecyclerState();
            if (currentState != null)
                Objects.requireNonNull(movieRecyclerView.getLayoutManager()).onRestoreInstanceState(currentState);
            if (searchedMovie == null || searchedMovie.size() == 0) {
                return;
            }

            movieAdapter.setMovieList(searchedMovie);
            movieAdapter.notifyDataSetChanged();
        });

        selectedSearch = SearchType.MOVIES;
        ConstraintLayout noSearchMoviesLayout = view.findViewById(R.id.no_search_movie_layout);
        ConstraintLayout noSearchUserLayout = view.findViewById(R.id.no_search_user_layout);
        noSearchMoviesLayout.setVisibility(View.VISIBLE);

        searchViewModel.getSearchedTextLiveData().observe(getViewLifecycleOwner(), s -> {
            if (s.getSearchedText() == null || s.getSearchedText().isEmpty())
                return;

            firstSearchDone = true;

            noSearchMoviesLayout.setVisibility(View.INVISIBLE);
            noSearchUserLayout.setVisibility(View.INVISIBLE);
            if (selectedSearch.equals(SearchType.MOVIES)) {
                hideNoResultGuiMovies();
                userTabConstraintLayout.setVisibility(View.GONE);
                movieTabConstraintLayout.setVisibility(View.VISIBLE);
                if (s.isByActor() && s.getYear() != null) {
                    searchByActorAndYear(movieRecyclerView, s);
                } else if (s.isByActor())
                    searchByActor(movieRecyclerView, s);
                else if (s.getYear() != null)
                    searchByYear(movieRecyclerView, s);
                else
                    searchNoFilter(movieRecyclerView, s);
            } else if (selectedSearch.equals(SearchType.USERS)) {

                hideNoResultGuiUsers();
                userTabConstraintLayout.setVisibility(View.VISIBLE);
                movieTabConstraintLayout.setVisibility(View.GONE);
                userRecyclerView.scrollToPosition(0);
                searchUser(movieRecyclerView, s);
            }
        });

        searchViewModel.getSearchTypeLiveData().observe(getViewLifecycleOwner(), searchType -> {
            selectedSearch = searchType;
            switch (searchType) {
                case MOVIES: {
                    if (!firstSearchDone) {
                        noSearchUserLayout.setVisibility(View.INVISIBLE);
                        noSearchMoviesLayout.setVisibility(View.VISIBLE);
                        return;
                    }
                    movieRecyclerView.setAdapter(movieAdapter);
                    searchViewModel.setSearchedTextLiveData(searchViewModel.getSearchedTextLiveData().getValue());
                    break;
                }
                case USERS: {
                    if (!firstSearchDone) {
                        noSearchMoviesLayout.setVisibility(View.INVISIBLE);
                        noSearchUserLayout.setVisibility(View.VISIBLE);
                        return;
                    }
                    userRecyclerView.setAdapter(userAdapter);
                    searchViewModel.setSearchedTextLiveData(searchViewModel.getSearchedTextLiveData().getValue());
                    break;
                }
            }
        });

        searchViewModel.getIsNewSearchLiveData().observe(getViewLifecycleOwner(), isNewSearch -> {
            if (isNewSearch == null)
                return;
            movieRecyclerView.scrollToPosition(0);
            searchViewModel.setIsNewSearchLiveData(null);
        });

        return view;

    }

    private void setupGui(View view) {

        noResultMoviesImageView = view.findViewById(R.id.no_result_movies_imageview);
        noResultMoviesTextView = view.findViewById(R.id.no_result_movies_textview);
        noResultMoviesSubtitleTextView = view.findViewById(R.id.no_result_movies_subtitle_textview);

        noResultUsersImageView = view.findViewById(R.id.no_result_users_imageview);
        noResultUsersTextView = view.findViewById(R.id.no_result_users_textview);
        noResultUsersSubtitleTextView = view.findViewById(R.id.no_result_users_subtitle_textview);

        movieTabConstraintLayout = view.findViewById(R.id.movie_search_constraint_layout);
        userTabConstraintLayout = view.findViewById(R.id.user_search_constraint_layout);


        // ---------------- RECYCLERVIEW AND ADAPTERS --------------------
        RecyclerView.ItemDecoration dividerItemDecoration = new ItemDecorationWithInitialSpace(30);

        LinearLayoutManager movieLinearLayout = new LinearLayoutManager(requireContext());
        movieRecyclerView = view.findViewById(R.id.search_movie_results_recyclerview);
        movieRecyclerView.addItemDecoration(dividerItemDecoration);
        movieRecyclerView.setLayoutManager(movieLinearLayout);

        LinearLayoutManager userLinearLayout = new LinearLayoutManager(requireContext());
        userRecyclerView = view.findViewById(R.id.search_user_results_recyclerview);
        userRecyclerView.addItemDecoration(dividerItemDecoration);
        userRecyclerView.setLayoutManager(userLinearLayout);

        movieAdapter = new MovieResultAdapter(movieResults, requireContext(), requireActivity());
        userAdapter = new UserResultAdapter(userResults, view, requireActivity());

        movieRecyclerView.setAdapter(movieAdapter);
        userRecyclerView.setAdapter(userAdapter);
    }

    private void showNoResultGuiMovies() {
        noResultMoviesImageView.setVisibility(View.VISIBLE);
        noResultMoviesTextView.setVisibility(View.VISIBLE);
        noResultMoviesSubtitleTextView.setVisibility(View.VISIBLE);
    }

    private void hideNoResultGuiMovies() {
        noResultMoviesImageView.setVisibility(View.INVISIBLE);
        noResultMoviesTextView.setVisibility(View.INVISIBLE);
        noResultMoviesSubtitleTextView.setVisibility(View.INVISIBLE);
    }

    private void showNoResultGuiUsers() {
        noResultUsersImageView.setVisibility(View.VISIBLE);
        noResultUsersTextView.setVisibility(View.VISIBLE);
        noResultUsersSubtitleTextView.setVisibility(View.VISIBLE);
    }

    private void hideNoResultGuiUsers() {
        noResultUsersImageView.setVisibility(View.INVISIBLE);
        noResultUsersTextView.setVisibility(View.INVISIBLE);
        noResultUsersSubtitleTextView.setVisibility(View.INVISIBLE);
    }

    private void searchUser(RecyclerView recyclerView, SearchParameters s) {
        if (s.getSearchedText() != null && !s.getSearchedText().isEmpty()) {
            currentPage = 1;
            userResults.clear();
            searchViewModel.requestSearchUser(s.getSearchedText(), 1, LoggedUser.getInstance().getUsername());
        }
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                assert layoutManager != null;
                searchViewModel.setUserRecyclerState(layoutManager.onSaveInstanceState());
                if (dy > 0) { //check for scroll down
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    if (!loading.get()) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading.set(true);
                            searchViewModel.requestSearchUser(s.getSearchedText(), ++currentPage, LoggedUser.getInstance().getUsername());
                        }
                    }
                }
            }
        });
    }

    private void searchNoFilter(RecyclerView recyclerView, SearchParameters s) {
        if (s.getSearchedText() != null && !s.getSearchedText().isEmpty()) {
            currentPage = 1;
            movieResults.clear();
            searchViewModel.requestSearchMovieNoFilter(s, currentPage);
        }
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                assert layoutManager != null;
                searchViewModel.setMovieRecyclerState(layoutManager.onSaveInstanceState());
                if (dy > 0) { //check for scroll down
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    if (!loading.get()) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading.set(true);
                            searchViewModel.requestSearchMovieNoFilter(s, ++currentPage);
                        }
                    }
                }
            }
        });
    }

    private void searchByYear(RecyclerView recyclerView, SearchParameters s) {
        if (s.getSearchedText() != null && !s.getSearchedText().isEmpty()) {
            currentPage = 1;
            movieResults.clear();
            searchViewModel.requestSearchMovieByYear(s, 1);
        }
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                assert layoutManager != null;
                searchViewModel.setMovieRecyclerState(layoutManager.onSaveInstanceState());
                if (dy > 0) { //check for scroll down
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    if (!loading.get()) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading.set(true);
                            searchViewModel.requestSearchMovieByYear(s, ++currentPage);
                        }
                    }
                }
            }
        });
    }

    private void searchByActor(RecyclerView recyclerView, SearchParameters s) {
        if (s.getSearchedText() != null && !s.getSearchedText().isEmpty()) {
            currentPage = 1;
            movieResults.clear();
            searchViewModel.requestSearchByActor(s, 1);
        }
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                assert layoutManager != null;
                searchViewModel.setMovieRecyclerState(layoutManager.onSaveInstanceState());
                if (dy > 0) { //check for scroll down
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    if (!loading.get()) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading.set(true);
                            searchViewModel.requestSearchByActor(s, ++currentPage);
                        }
                    }
                }
            }
        });
    }

    private void searchByActorAndYear(RecyclerView recyclerView, SearchParameters s) {
        if (s.getSearchedText() != null && !s.getSearchedText().isEmpty()) {
            currentPage = 1;
            movieResults.clear();
            searchViewModel.requestSearchByActorAndYear(s, 1);
        }
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                assert layoutManager != null;
                searchViewModel.setMovieRecyclerState(layoutManager.onSaveInstanceState());
                if (dy > 0) { //check for scroll down
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    if (!loading.get()) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading.set(true);
                            searchViewModel.requestSearchByActorAndYear(s, ++currentPage);
                        }
                    }
                }
            }
        });
    }
}