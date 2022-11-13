package it.unina.cinemates.ui.suggestmovie;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import it.unina.cinemates.MainActivity;
import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.models.Movie;
import it.unina.cinemates.retrofit.backend.BackendRetrofitService;
import it.unina.cinemates.retrofit.tmdb.TmDbCallbacks;
import it.unina.cinemates.retrofit.tmdb.TmDbConstants;
import it.unina.cinemates.retrofit.tmdb.TmDbRetrofitService;
import it.unina.cinemates.ui.common.utils.AnimationUtils;
import it.unina.cinemates.ui.common.utils.GlideUtils;
import it.unina.cinemates.ui.common.utils.ImageUtils;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.viewmodels.MovieFullViewModel;
import it.unina.cinemates.views.backend.MoviesInList;
import it.unina.cinemates.views.tmdb.MovieBasicResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SuggestMovieFragment extends Fragment {

    private MovieFullViewModel movieFullViewModel;
    private ImageView suggestedImageView;
    private AtomicInteger lastSuggestedMovie;
    private TextView questionMarkTextView;
    private ArrayList<Movie> moviesInList;
    private Button suggestButton;

    private Snackbar checkYourConnectionErrorSnackbar;
    private Snackbar noMoviesInWatchListErrorSnackbar;

    private boolean isListEmpty = false;
    private TextView movieTitleTextView;

    public SuggestMovieFragment() {
    }

    public static SuggestMovieFragment newInstance() {
        return new SuggestMovieFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movieFullViewModel = new ViewModelProvider(requireActivity()).get(MovieFullViewModel.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_suggest_movie, container, false);

        setupGui(view);
        setupClickListenersAndCallbacks();

        return view;
    }

    private void setupClickListenersAndCallbacks() {
        suggestButton.setOnClickListener(v -> {

            FirebaseAnalytics.getInstance(requireContext()).logEvent("MovieSuggest", null);

            if (isListEmpty) {
                SuggestMovieFragment.this.noMoviesInWatchListErrorSnackbar.show();
                return;
            }

            AnimationUtils.animateFadeOut(suggestedImageView, 1000);

            questionMarkTextView.setVisibility(View.GONE);
            Collections.shuffle(moviesInList);
            int suggestedMovieId = moviesInList.get(0).getTmDbId();

            if (moviesInList.size() == 1)
                suggestedMovieId = moviesInList.get(0).getTmDbId();
            else {
                while (suggestedMovieId == lastSuggestedMovie.get()) {
                    Collections.shuffle(moviesInList);
                    suggestedMovieId = moviesInList.get(0).getTmDbId();
                }
            }

            lastSuggestedMovie.set(suggestedMovieId);

            suggestedImageView.setOnClickListener(v1 -> {
                TmDbRetrofitService.getTmDbDaoInstance().getMoviesDetailsById(lastSuggestedMovie.get())
                        .enqueue(TmDbCallbacks.movieDetailsCallBack(movieFullViewModel));
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.navigation_movie_full);
            });

            TmDbRetrofitService.getTmDbDaoInstance().getMoviePosterAndBackdrop(suggestedMovieId).enqueue(new Callback<MovieBasicResult>() {
                @Override
                public void onResponse(@NonNull Call<MovieBasicResult> call, @NonNull Response<MovieBasicResult> response) {
                    if (response.code() == 200) {
                        assert response.body() != null;
                        if (response.body().getPosterPath() != null) {
                            movieTitleTextView.setText("");
                            String firstPosterPath = response.body().getPosterPath();
                            GlideUtils.loadAndSetImage(TmDbConstants.Images.mediumBackdropPath(firstPosterPath), suggestedImageView, requireContext());
                        }else {
                            movieTitleTextView.setText(response.body().getTitle());
                            int drawableId = ImageUtils.getDrawableResourceId(requireActivity(), "suggest_movie_no_poster_available");
                            GlideUtils.loadAndSetImage(drawableId, suggestedImageView, requireContext());
                        }

                        AnimationUtils.animateFadeIn(suggestedImageView, 1000);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<MovieBasicResult> call, @NonNull Throwable t) {
                }
            });
        });


        BackendRetrofitService.getInstance().getMovieListAPI()
                .getMoviesInList(LoggedUser.getInstance().getToWatchListId())
                .enqueue(new Callback<MoviesInList>() {
                    @Override
                    public void onResponse(@NonNull Call<MoviesInList> call, @NonNull Response<MoviesInList> response) {
                        if (response.code() == 200) {
                            assert response.body() != null;
                            moviesInList.addAll(response.body().getMoviesInList());
                            if (moviesInList.size() == 0)
                                isListEmpty = true;

                            suggestButton.setEnabled(true);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MoviesInList> call, @NonNull Throwable t) {
                        SuggestMovieFragment.this.checkYourConnectionErrorSnackbar.show();
                    }
                });
    }

    private void setupGui(View view) {
        suggestedImageView = view.findViewById(R.id.suggest_movie_poster_imageview);
        lastSuggestedMovie = new AtomicInteger(-1);
        
        movieTitleTextView = view.findViewById(R.id.suggest_movie_name_textview);

        questionMarkTextView = view.findViewById(R.id.question_mark_textview);
        moviesInList = new ArrayList<>();
        suggestButton = view.findViewById(R.id.suggest_movie_button);
        suggestButton.setEnabled(false);

        View bottomNavigationView = ((MainActivity) requireActivity()).getBottomNavigationView();

        this.noMoviesInWatchListErrorSnackbar = SnackbarUtils.failureAnchorSnackbar(requireActivity(), getString(R.string.no_movies_to_watch_list_error), suggestButton);
        this.checkYourConnectionErrorSnackbar = SnackbarUtils.failureAnchorSnackbar(requireActivity(), getString(R.string.snackbar_server_unreachable), bottomNavigationView);
    }
}