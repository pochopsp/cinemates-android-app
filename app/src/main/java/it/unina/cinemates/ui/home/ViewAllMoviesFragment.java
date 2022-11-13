package it.unina.cinemates.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unina.cinemates.MainActivity;
import it.unina.cinemates.R;
import it.unina.cinemates.retrofit.backend.enums.BackendOperationResult;
import it.unina.cinemates.ui.common.recyclerview.ItemDecorationWithInitialSpace;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.ui.search.recyclerview.movie.MovieResultAdapter;
import it.unina.cinemates.viewmodels.HomeViewModel;
import it.unina.cinemates.viewmodels.user.movielist.logged.AddMovieToMovieListViewModel;
import it.unina.cinemates.views.tmdb.MovieResult;


public class ViewAllMoviesFragment extends Fragment {

    private HomeViewModel homeViewModel;

    private AddMovieToMovieListViewModel addMovieToMovieListViewModel;

    private MovieResultAdapter adapter;

    private TextView titleTextView;
    private RecyclerView recyclerView;

    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private final AtomicBoolean loading = new AtomicBoolean(true);
    private int currentPage = 1;

    private Snackbar addToListSuccessSnackbar;
    private Snackbar addToListErrorSnackbar;
    private Snackbar checkYourConnectionErrorSnackbar;

    public ViewAllMoviesFragment() {
    }

    public static ViewAllMoviesFragment newInstance() {

        return new ViewAllMoviesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        addMovieToMovieListViewModel = new ViewModelProvider(requireActivity()).get(AddMovieToMovieListViewModel.class);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_all_movies, container, false);

        setupGui(view);
        addMovieToMovieListViewModel.getInsertMovieInMovieListResult().observe(getViewLifecycleOwner(), insertListResult -> {

            if (insertListResult == BackendOperationResult.IDLE)
                return;

            switch (insertListResult) {
                case SUCCESS:
                    this.addToListSuccessSnackbar.show();
                    break;
                case ALREADY_PRESENT:
                    this.addToListErrorSnackbar.show();
                    break;
                case SERVER_UNREACHABLE:
                    this.checkYourConnectionErrorSnackbar.show();
                    break;
            }

            addMovieToMovieListViewModel.resetInsertMovieInMovielistResult();
        });

        homeViewModel.getViewAllTypeMutableLiveData().observe(getViewLifecycleOwner(), type -> {

            switch (type) {
                case POPULAR:
                    setupPopularGUI(titleTextView, recyclerView);
                    break;
                case UPCOMING:
                    setupUpcomingGUI(titleTextView, recyclerView);
                    break;
                case TOP_RATED:
                    setupTopRatedGUI(titleTextView, recyclerView);
                    break;
                case NOW_PLAYING:
                    setupNowPlayingGUI(titleTextView, recyclerView);
                    break;
            }
        });

        if (!homeViewModel.backFromMovieFull()) {
            currentPage = 1;
            homeViewModel.resetMovieListResult();
        }

        homeViewModel.getViewAllMovieResultLiveData().observe(getViewLifecycleOwner(), movieResults -> {
            loading.set(false);

            if (movieResults == null || movieResults.size() == 0)
                return;

            adapter.setMovieList(movieResults);
            adapter.notifyDataSetChanged();
            recyclerView.setVisibility(View.VISIBLE);
        });

        return view;
    }

    private void setupGui(View view) {
        titleTextView = view.findViewById(R.id.viewall_title_textview);
        recyclerView = view.findViewById(R.id.all_movies_recyclerview);
        List<MovieResult> movies = new ArrayList<>();
        LinearLayoutManager linearLayout = new LinearLayoutManager(requireContext());
        recyclerView.addItemDecoration(new ItemDecorationWithInitialSpace(30));
        recyclerView.setLayoutManager(linearLayout);
        adapter = new MovieResultAdapter(movies, requireContext(), requireActivity());
        recyclerView.setAdapter(adapter);

        View bottomNavigationView = ((MainActivity) requireActivity()).getBottomNavigationView();

        this.addToListSuccessSnackbar = SnackbarUtils.successAnchorSnackbar(requireActivity(), getString(R.string.movie_add_to_list_success), bottomNavigationView);
        this.addToListErrorSnackbar = SnackbarUtils.failureAnchorSnackbar(requireActivity(), getString(R.string.movie_already_in_list_error), bottomNavigationView);
        this.checkYourConnectionErrorSnackbar = SnackbarUtils.failureAnchorSnackbar(requireActivity(), getString(R.string.snackbar_server_unreachable), bottomNavigationView);
    }

    private void setupNowPlayingGUI(TextView titleTextView, RecyclerView recyclerView) {
        titleTextView.setText(R.string.all_now_playing);
        homeViewModel.requestNowPlayingMovieResult(1);
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
                            homeViewModel.requestNowPlayingMovieResult(++currentPage);
                        }
                    }
                }
            }
        });
    }

    private void setupTopRatedGUI(TextView titleTextView, RecyclerView recyclerView) {
        titleTextView.setText(R.string.all_top_rated);
        homeViewModel.requestTopRatedMovieResult(1);
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
                            homeViewModel.requestTopRatedMovieResult(++currentPage);
                            loading.set(true);
                        }
                    }
                }
            }
        });
    }

    private void setupUpcomingGUI(TextView titleTextView, RecyclerView recyclerView) {
        titleTextView.setText(R.string.all_upcomings);
        homeViewModel.requestUpcomingMovieResult(1);
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
                            homeViewModel.requestUpcomingMovieResult(++currentPage);
                            loading.set(true);
                        }
                    }
                }
            }
        });
    }

    private void setupPopularGUI(TextView titleTextView, RecyclerView recyclerView) {
        titleTextView.setText(R.string.all_popular);
        homeViewModel.requestPopularMovieResult(1);
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
                            homeViewModel.requestPopularMovieResult(++currentPage);
                            loading.set(true);
                        }
                    }
                }
            }
        });
    }
}