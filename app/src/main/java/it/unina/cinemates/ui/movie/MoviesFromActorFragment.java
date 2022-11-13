package it.unina.cinemates.ui.movie;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unina.cinemates.R;
import it.unina.cinemates.retrofit.backend.enums.BackendOperationResult;
import it.unina.cinemates.ui.common.recyclerview.ItemDecorationWithInitialSpace;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.ui.search.recyclerview.movie.MovieResultAdapter;
import it.unina.cinemates.viewmodels.MoviesFromActorViewModel;
import it.unina.cinemates.viewmodels.user.movielist.logged.AddMovieToMovieListViewModel;
import it.unina.cinemates.views.tmdb.MovieResult;


public class MoviesFromActorFragment extends Fragment {

    private MoviesFromActorViewModel moviesFromActorViewModel;

    private AddMovieToMovieListViewModel addMovieToMovieListViewModel;

    private MovieResultAdapter adapter;

    private RecyclerView recyclerView;

    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private final AtomicBoolean loading = new AtomicBoolean(true);
    private int currentPage = 1;

    private Snackbar addToListSuccessSnackbar;
    private Snackbar addToListErrorSnackbar;
    private Snackbar checkYourConnectionErrorSnackbar;


    private String actorName = "";

    public MoviesFromActorFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        moviesFromActorViewModel = new ViewModelProvider(requireActivity()).get(MoviesFromActorViewModel.class);
        addMovieToMovieListViewModel = new ViewModelProvider(requireActivity()).get(AddMovieToMovieListViewModel.class);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_movies_from_actor, container, false);

        moviesFromActorViewModel.resetMovieListResult();

        Bundle bundle = getArguments();
        if (bundle != null) {
            String clickedActor = (String) bundle.get("clickedActor");
            if (clickedActor != null) {
                this.actorName = clickedActor;
                setupGui(view);
            }
        }

        ImageView backIcon = view.findViewById(R.id.movies_from_actor_back_icon);
        backIcon.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
        });

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

        moviesFromActorViewModel.getMoviesFromActor().observe(getViewLifecycleOwner(), movieResults -> {
            loading.set(false);

            if (movieResults == null || movieResults.size() == 0)
                return;

            adapter.setMovieList(movieResults);
            adapter.notifyDataSetChanged();
            recyclerView.setVisibility(View.VISIBLE);
        });

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void setupGui(View view) {
        TextView titleTextView = view.findViewById(R.id.movies_from_actor_title_textview);

        String titleText = titleTextView.getText().toString();

        titleTextView.setText(titleText + " " + this.actorName);

        recyclerView = view.findViewById(R.id.movies_from_actor_recyclerview);
        List<MovieResult> movies = new ArrayList<>();
        LinearLayoutManager linearLayout = new LinearLayoutManager(requireContext());
        recyclerView.addItemDecoration(new ItemDecorationWithInitialSpace(30));
        recyclerView.setLayoutManager(linearLayout);
        adapter = new MovieResultAdapter(movies, requireContext(), requireActivity());
        recyclerView.setAdapter(adapter);

        setupRecyclerView(recyclerView);

        this.addToListSuccessSnackbar = SnackbarUtils.successSnackbar(requireActivity(), getString(R.string.movie_add_to_list_success));
        this.addToListErrorSnackbar = SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.movie_already_in_list_error));
        this.checkYourConnectionErrorSnackbar = SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.snackbar_server_unreachable));
    }

    private void setupRecyclerView(RecyclerView recyclerView) {

        currentPage = 1;
        moviesFromActorViewModel.requestMoviesFromActor(this.actorName, currentPage);

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
                            moviesFromActorViewModel.requestMoviesFromActor(MoviesFromActorFragment.this.actorName, ++currentPage);
                        }
                    }
                }
            }
        });
    }


}