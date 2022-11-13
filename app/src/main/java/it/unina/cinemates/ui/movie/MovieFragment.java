package it.unina.cinemates.ui.movie;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import it.unina.cinemates.R;
import it.unina.cinemates.retrofit.backend.enums.BackendOperationResult;
import it.unina.cinemates.retrofit.tmdb.TmDbConstants;
import it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.details.MovieDetails;
import it.unina.cinemates.ui.common.recyclerview.ItemDecorationWithInitialSpace;
import it.unina.cinemates.ui.common.utils.AnimationUtils;
import it.unina.cinemates.ui.common.utils.GlideUtils;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.viewmodels.HomeViewModel;
import it.unina.cinemates.viewmodels.MovieFullViewModel;
import it.unina.cinemates.viewmodels.user.movielist.logged.AddMovieToMovieListViewModel;
import it.unina.cinemates.views.tmdb.MovieFull;

public class MovieFragment extends Fragment {

    //private static final String TAG = "MOVIEFULL_FRAGMENT";
    private AddMovieToMovieListViewModel addMovieToMovieListViewModel;

    private MovieFullViewModel movieFullViewModel;
    private HomeViewModel homeViewModel;

    private TextView movieTitleTextView;
    private TextView movieYearTextView;
    private TextView movieGenresTextView;

    private TextView movieDescriptionTextView;
    private ObjectAnimator expandDescriptionObjectAnimator;
    private ObjectAnimator collapseDescriptionObjectAnimator;


    private TextView movieReleaseDateTextView;
    private TextView movieStatusTextView;
    private TextView movieDirectorTextView;
    private TextView movieBudgetTextView;
    private TextView movieRevenueTextView;
    private TextView movieRatingTextView;
    private ImageView backdropImageView;
    private ImageView posterImageView;
    private TextView expandTextButton;

    private CastAdapter castAdapter;

    private Snackbar addToListSuccessSnackbar;
    private Snackbar addToListErrorSnackbar;
    private Snackbar checkYourConnectionErrorSnackbar;

    private MovieDetails lastMovieDetailsSeen;
    private boolean loaded = false;
    private String deviceLocale;

    private ScrollView movieScrollView;
    private ImageView expandedPosterImageView;


    public MovieFragment() {
    }

    public static MovieFragment newInstance() {

        return new MovieFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movieFullViewModel = new ViewModelProvider(requireActivity()).get(MovieFullViewModel.class);
        addMovieToMovieListViewModel = new ViewModelProvider(requireActivity()).get(AddMovieToMovieListViewModel.class);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                MovieFragment.this.goBack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        Locale locale = getResources().getConfiguration().getLocales().get(0);
        this.deviceLocale = locale.toLanguageTag();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_full, container, false);

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

        ImageView backIcon = view.findViewById(R.id.moviefull_back_icon);
        backIcon.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
            homeViewModel.setBackFromMovieFullOrInsertList(true);
        });

        expandTextButton = view.findViewById(R.id.expand_description_text_button);
        expandTextButton.setOnClickListener(v -> {

            if (expandTextButton.getText().toString().equals(getString(R.string.expand_description))) {
                expandDescriptionObjectAnimator.start();
                movieDescriptionTextView.setMaxLines(100);
                expandTextButton.setText(getString(R.string.collapse_description));
            } else {
                collapseDescriptionObjectAnimator.start();
                movieDescriptionTextView.setMaxLines(3);
                expandTextButton.setText(getString(R.string.expand_description));
            }
        });

        ImageView trailerButton = view.findViewById(R.id.moviefull_trailer_outlined_button);


        movieFullViewModel.getMovieFullLiveData().observe(getViewLifecycleOwner(), movieDetails -> {
            if (movieDetails == null && !loaded)
                movieDetails = this.lastMovieDetailsSeen;
            setupAfterGotMovie(view, trailerButton, movieDetails);
        });

        return view;
    }


    @SuppressLint("NotifyDataSetChanged")
    private void setupAfterGotMovie(View view, ImageView trailerButton, MovieDetails movieDetails) {
        if (movieDetails == null)
            return;

        MovieFull movieFull = new MovieFull(movieDetails, this.deviceLocale);
        movieTitleTextView.setText(movieFull.getTitle());
        movieYearTextView.setText(movieFull.getYear());

        if (!movieFull.getRuntime().isEmpty()) {
            view.findViewById(R.id.moviefull_runtime_dot_separator).setVisibility(View.VISIBLE);
            TextView movieRuntimeTextView = view.findViewById(R.id.moviefull_runtime_textview);
            movieRuntimeTextView.setVisibility(View.VISIBLE);
            movieRuntimeTextView.setText(movieFull.getRuntime());
        }

        movieGenresTextView.setText(movieFull.getGenres());

        if (movieFull.getDescription().isEmpty()) {
            movieDescriptionTextView.setText(getString(R.string.no_description_available));
            expandTextButton.setVisibility(View.GONE);
        } else {
            movieDescriptionTextView.setText(movieFull.getDescription());

            movieDescriptionTextView.post(() -> {
                Layout layout = movieDescriptionTextView.getLayout();
                if (layout != null) {
                    int lines = layout.getLineCount();
                    if (lines > 0) {
                        int ellipsisCount = layout.getEllipsisCount(lines - 1);
                        if (ellipsisCount == 0) {
                            expandTextButton.setVisibility(View.GONE);
                        }
                    }
                }
            });

        }

        movieReleaseDateTextView.setText(movieFull.getReleaseDate());
        movieStatusTextView.setText(movieFull.getMovieStatus());
        movieDirectorTextView.setText(movieFull.getDirectorName());
        movieBudgetTextView.setText(movieFull.getBudget());
        movieRevenueTextView.setText(movieFull.getRevenue());
        movieRatingTextView.setText(movieFull.getTmDbRating());

        if (movieDetails.getBackdropPath() != null)
            GlideUtils.loadAndSetImage(TmDbConstants.Images.mediumBackdropPath(movieDetails.getBackdropPath()), backdropImageView, requireContext());
        else
            backdropImageView.setImageResource(R.drawable.no_backdrop_available);

        if (movieDetails.getPosterPath() != null) {
            GlideUtils.loadAndSetImage(TmDbConstants.Images.mediumPosterPath(movieDetails.getPosterPath()), posterImageView, requireContext());

            ConstraintLayout thisFragmentLayout = view.findViewById(R.id.moviefull_contraint_layout);

            expandedPosterImageView = view.findViewById(R.id.expanded_movie_poster);
            GlideUtils.loadAndSetImage(TmDbConstants.Images.largePosterPath(movieDetails.getPosterPath()), expandedPosterImageView, requireContext());

            posterImageView.setOnClickListener(v -> {
                View.OnClickListener reduceOnClickListener = AnimationUtils.expandAndReturnReduceClickListener(
                        posterImageView,
                        expandedPosterImageView,
                        thisFragmentLayout);

                expandedPosterImageView.setOnClickListener(reduceOnClickListener);
            });

        } else {
            posterImageView.setImageResource(R.drawable.no_poster_available);
        }

        if (movieFull.getTrailerUrl() == null) {
            trailerButton.setVisibility(View.INVISIBLE);
            trailerButton.setEnabled(false);
        } else {
            trailerButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(movieFull.getTrailerUrl().toString()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setPackage("com.google.android.youtube");
                startActivity(intent);
            });
        }

        TextView castTextView = view.findViewById(R.id.moviefull_cast_textview);
        castTextView.setVisibility(View.VISIBLE);
        if (movieFull.getCastMembers() == null || movieFull.getCastMembers().size() == 0) {
            castTextView.setVisibility(View.GONE);
        } else {
            castAdapter.setCastMembers(movieFull.getCastMembers());
            castAdapter.notifyDataSetChanged();
        }
        MaterialButton addToListButton = view.findViewById(R.id.moviefull_add_to_list_outlined_button);
        addToListButton.setOnClickListener(v -> {
            loaded = false;
            Bundle bundle = new Bundle();
            bundle.putInt("movieId", movieDetails.getId());
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_add_movie_to_list_bottom_sheet, bundle);
        });
        loaded = true;
        lastMovieDetailsSeen = movieDetails;
        movieFullViewModel.setMovieFullLiveData(null);

        movieScrollView.setVisibility(View.VISIBLE);
    }

    private void setupGui(View view) {
        movieScrollView = view.findViewById(R.id.moviefull_scrollview);

        movieTitleTextView = view.findViewById(R.id.moviefull_title_textview);
        movieYearTextView = view.findViewById(R.id.moviefull_year_textview);
        movieGenresTextView = view.findViewById(R.id.moviefull_genre_textview);

        movieDescriptionTextView = view.findViewById(R.id.moviefull_description_textview);
        expandDescriptionObjectAnimator = ObjectAnimator.ofInt(movieDescriptionTextView, "maxLines", 100).setDuration(1000);
        collapseDescriptionObjectAnimator = ObjectAnimator.ofInt(movieDescriptionTextView, "maxLines", 3).setDuration(500);

        movieReleaseDateTextView = view.findViewById(R.id.moviefull_release_date_text_textview);
        movieStatusTextView = view.findViewById(R.id.moviefull_status_text_textview);
        movieDirectorTextView = view.findViewById(R.id.moviefull_director_text_textview);
        movieBudgetTextView = view.findViewById(R.id.moviefull_budget_text_textview);
        movieRevenueTextView = view.findViewById(R.id.moviefull_revenue_text_textview);
        movieRatingTextView = view.findViewById(R.id.moviefull_tmdb_rating_textview);
        backdropImageView = view.findViewById(R.id.moviefull_backdrop_imageview);
        posterImageView = view.findViewById(R.id.moviefull_poster_imageview);
        RecyclerView recyclerView = view.findViewById(R.id.cast_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.addItemDecoration(new ItemDecorationWithInitialSpace(63, true));


        this.castAdapter = new CastAdapter(
                new ArrayList<>(),
                requireContext(),
                clickedActor -> v -> {

                    MoviesFromActorBottomSheet[] moviesFromActorBottomSheet = new MoviesFromActorBottomSheet[1];

                    View.OnClickListener viewAllMoviesFromActorClickListener = v1 -> {
                        loaded = false;
                        Bundle bundle = new Bundle();
                        bundle.putString("clickedActor", clickedActor);
                        NavController navController = Navigation.findNavController(view);
                        navController.navigate(R.id.navigation_movies_from_actor, bundle);

                        moviesFromActorBottomSheet[0].dismiss();
                    };

                    moviesFromActorBottomSheet[0] = new MoviesFromActorBottomSheet(
                            viewAllMoviesFromActorClickListener,
                            clickedActor
                    );
                    moviesFromActorBottomSheet[0].show(requireActivity().getSupportFragmentManager(), moviesFromActorBottomSheet[0].getTag());
                }
        );
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(castAdapter);


        this.addToListSuccessSnackbar = SnackbarUtils.successSnackbar(requireActivity(), getString(R.string.movie_add_to_list_success));
        this.addToListErrorSnackbar = SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.movie_already_in_list_error));
        this.checkYourConnectionErrorSnackbar = SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.snackbar_server_unreachable));
    }

    private void goBack() {
        if (expandedPosterImageView != null && expandedPosterImageView.getVisibility() == View.VISIBLE) {
            expandedPosterImageView.callOnClick();
            return;
        }

        NavController navController = Navigation.findNavController(requireView());
        navController.navigateUp();
        homeViewModel.setBackFromMovieFullOrInsertList(true);
    }

}