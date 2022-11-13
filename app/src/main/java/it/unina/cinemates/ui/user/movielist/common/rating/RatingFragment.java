package it.unina.cinemates.ui.user.movielist.common.rating;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.ui.common.recyclerview.ItemDecorationWithInitialSpace;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.ui.user.movielist.common.rating.recyclerview.RatingAdapter;
import it.unina.cinemates.views.backend.MovieListBasic;
import it.unina.cinemates.views.backend.Rating;
import it.unina.cinemates.viewmodels.user.movielist.common.RatingViewModel;

public class RatingFragment extends Fragment {

    private RatingViewModel ratingViewModel;

    private TextView listNameTextView;
    private TextView totalRatingsTextView;
    private ImageView backIconImageView;

    private ImageView noRatingsImageView;
    private TextView noRatingsTitleTextView;
    private TextView noRatingsTextView;
    private ConstraintLayout ratingsSummaryLayout;
    private TextView rateThisListTextView;

    private TextView myRatingTextView;
    private ImageView moreOptionIcon;
    private TextView confirmRatingTextView;
    private RelativeLayout ratingButtonsLayout;
    private SimpleRatingBar ratingBar;

    private LinearProgressIndicator numberFiveStarsIndicator;
    private LinearProgressIndicator numberFourStarsIndicator;
    private LinearProgressIndicator numberThreeStarsIndicator;
    private LinearProgressIndicator numberTwoStarsIndicator;
    private LinearProgressIndicator numberOneStarsIndicator;
    private SimpleRatingBar averageRatingBar;
    private TextView averageRatingTextView;

    private Float averageRating;
    private Integer movieListId;

    private RecyclerView recyclerView;
    private RatingAdapter ratingAdapter;
    private List<Rating> ratings;
    private Rating userRating;

    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private final AtomicBoolean loading = new AtomicBoolean(true);
    private int currentPage = 1;

    private MovieListBasic movieListBasic;
    private boolean done = false;

    public RatingFragment() {
    }

    public static RatingFragment newInstance() {
        return new RatingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ratingViewModel = new ViewModelProvider(requireActivity()).get(RatingViewModel.class);
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n", "DefaultLocale"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rating, container, false);

        setupGui(view);

        setupListeners();

        Bundle bundle = getArguments();
        assert bundle != null;
        this.movieListBasic = (MovieListBasic) bundle.get("movieList");

        movieListId = movieListBasic.getId();
        String listName = movieListBasic.getName();

        listNameTextView.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            Bundle bundle1 = new Bundle();
            bundle1.putInt("movieListId", movieListBasic.getId());
            if(movieListBasic.getOwnerId().equals(LoggedUser.getInstance().getUsername()))
                navController.navigate(R.id.navigation_logged_user_movie_list, bundle1);
            else
                navController.navigate(R.id.navigation_other_user_movie_list, bundle1);
        });

        if (listName == null) {
            switch (movieListBasic.getType()) {
                case ToWatch:
                    listNameTextView.setText(getString(R.string.to_watch_list_title));
                    break;
                case Favorites:
                    listNameTextView.setText(getString(R.string.favorites_list_title));
                    break;
            }
        } else
            listNameTextView.setText(movieListBasic.getName());

        if (movieListBasic.getOwnerId().equals(LoggedUser.getInstance().getUsername())) {
            ConstraintLayout insertRatingLayout = view.findViewById(R.id.insert_rating_constraintlayout);
            insertRatingLayout.setVisibility(View.GONE);
        }


        ratingViewModel.resetRatingList();
        ratingViewModel.requestMovieListRatings(movieListId, 1);


        ratingViewModel.getFoundRatingsLiveData().observe(getViewLifecycleOwner(), found -> {
            if (found == null)
                return;

            if (found)
                hideNoRatingsGui();
            else
                showNoRatingsGui();

            ratingViewModel.setFoundRatingsLiveData(null);
        });

        ratingViewModel.getMovieListTotalRatingsLiveData().observe(getViewLifecycleOwner(), integer -> {
            totalRatingsTextView.setText(getString(R.string.total_number_ratings, integer));
            totalRatingsTextView.setVisibility(View.VISIBLE);
        });

        ratingViewModel.getAverageRatingLiveData().observe(getViewLifecycleOwner(), avg -> {
            averageRating = avg == null ? 0 : avg;
            averageRatingTextView.setText(String.format(Locale.US, "%.2g%n", avg).trim());
            averageRatingBar.setRating(averageRating);
        });

        ratingViewModel.getRatingLiveData().observe(getViewLifecycleOwner(), ratingList -> {
            if (ratingList == null || ratingList.size() == 0) {
                userRating = null;
                return;
            }
            ratings = ratingList;
            ratingAdapter.setRatingsList(ratings);
            ratingAdapter.notifyDataSetChanged();
            userRating = getLoggedUserRating();
            if (userRating != null) {
                rateThisListTextView.setVisibility(View.INVISIBLE);
                myRatingTextView.setVisibility(View.VISIBLE);
                moreOptionIcon.setVisibility(View.VISIBLE);
                ratingBar.setRating(userRating.getValue());
            } else {
                myRatingTextView.setVisibility(View.INVISIBLE);
                moreOptionIcon.setVisibility(View.GONE);
                rateThisListTextView.setVisibility(View.VISIBLE);
                ratingBar.setRating(0);
            }
            setSummaryRatingsIndicators(ratings);
        });


        ratingViewModel.getRatingActionsLiveData().observe(getViewLifecycleOwner(), action -> {
            if (action == RatingActions.IDLE)
                return;

            switch (action) {
                case RATING_FAILURE:
                    SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.snackbar_server_unreachable)).show();
                    if (userRating == null) {
                        confirmRatingTextView.setVisibility(View.INVISIBLE);
                        myRatingTextView.setVisibility(View.INVISIBLE);
                        ratingBar.setRating(0);
                        rateThisListTextView.setVisibility(View.VISIBLE);
                        moreOptionIcon.setVisibility(View.GONE);
                    } else {
                        ratingBar.setRating(userRating.getValue());
                        confirmRatingTextView.setVisibility(View.INVISIBLE);
                        myRatingTextView.setVisibility(View.VISIBLE);
                        moreOptionIcon.setVisibility(View.VISIBLE);
                    }
                    break;
                case RATING_POST_SUCCESS:
                    currentPage = 1;
                    myRatingTextView.setVisibility(View.VISIBLE);
                    moreOptionIcon.setVisibility(View.VISIBLE);
                    confirmRatingTextView.setVisibility(View.INVISIBLE);
                    SnackbarUtils.successSnackbar(requireActivity(), getString(R.string.inserted_rating_success)).show();
                    ratingViewModel.resetRatingList();
                    ratingViewModel.requestMovieListRatings(movieListId, 1);
                    break;
                case RATING_PUT_SUCCESS:
                    currentPage = 1;
                    myRatingTextView.setVisibility(View.VISIBLE);
                    moreOptionIcon.setVisibility(View.VISIBLE);
                    confirmRatingTextView.setVisibility(View.INVISIBLE);
                    SnackbarUtils.successSnackbar(requireActivity(), getString(R.string.modified_rating_success)).show();
                    ratingViewModel.resetRatingList();
                    ratingViewModel.requestMovieListRatings(movieListId, 1);
                    break;
                case RATING_DELETE_SUCCESS:
                    currentPage = 1;
                    userRating = null;
                    myRatingTextView.setVisibility(View.INVISIBLE);
                    ratingBar.setRating(0);
                    rateThisListTextView.setVisibility(View.VISIBLE);
                    moreOptionIcon.setVisibility(View.GONE);
                    ratings.remove(userRating);
                    ratingAdapter.notifyDataSetChanged();
                    SnackbarUtils.successSnackbar(requireActivity(), getString(R.string.deleted_rating_success)).show();
                    ratingViewModel.resetRatingList();
                    ratingViewModel.requestMovieListRatings(movieListId, 1);
            }
            ratingViewModel.setRatingsActionLiveData(RatingActions.IDLE);
        });


        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupListeners() {
        backIconImageView.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
        });

        ratingBar.setOnClickListener(v -> {
            myRatingTextView.setVisibility(View.INVISIBLE);
            moreOptionIcon.setVisibility(View.GONE);
            rateThisListTextView.setVisibility(View.INVISIBLE);
            confirmRatingTextView.setVisibility(View.VISIBLE);
            ratingButtonsLayout.setVisibility(View.VISIBLE);
        });

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
                            ratingViewModel.requestMovieListRatings(movieListId, ++currentPage);
                        }
                    }
                }
            }
        });


        moreOptionIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), moreOptionIcon);
            //Inflating the Popup using xml file
            popup.getMenuInflater().inflate(R.menu.rating_menu, popup.getMenu());

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(item -> {
                ratingViewModel.requestDeleteRating(userRating.getId());
                return true;
            });

            popup.show();//showing popup menu
        });

        MaterialButton cancelRatingButton = ratingButtonsLayout.findViewById(R.id.rating_cancel_button);
        cancelRatingButton.setOnClickListener(v -> {
            ratingButtonsLayout.setVisibility(View.GONE);
            confirmRatingTextView.setVisibility(View.INVISIBLE);
            if (userRating == null) {
                rateThisListTextView.setVisibility(View.VISIBLE);
                ratingBar.setRating(0);
            } else {
                rateThisListTextView.setVisibility(View.INVISIBLE);
                myRatingTextView.setVisibility(View.VISIBLE);
                moreOptionIcon.setVisibility(View.VISIBLE);
                ratingBar.setRating(userRating.getValue());
            }
        });
        MaterialButton confirmRatingButton = ratingButtonsLayout.findViewById(R.id.rating_confirm_button);
        confirmRatingButton.setOnClickListener(v -> {
            if (ratingBar.getRating() == 0f) {
                SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.minimum_rating_error)).show();
                return;
            }
            ratingButtonsLayout.setVisibility(View.GONE);
            it.unina.cinemates.models.Rating newRating = new it.unina.cinemates.models.Rating();
            newRating.setAuthorId(LoggedUser.getInstance().getUsername());
            newRating.setValue(Math.round(ratingBar.getRating()));
            newRating.setRatedListId(movieListId);

            FirebaseAnalytics.getInstance(requireContext()).logEvent("RatingToList", null);

            if (userRating == null) {
                ratingViewModel.requestPostRating(newRating);
            } else {
                ratingViewModel.requestPutRating(newRating);
            }
        });
    }

    private void setupGui(View view) {
        listNameTextView = view.findViewById(R.id.ratings_title_list_name_textview);
        totalRatingsTextView = view.findViewById(R.id.total_ratings_textview);
        backIconImageView = view.findViewById(R.id.back_icon_ratings);


        noRatingsImageView = view.findViewById(R.id.no_ratings_image_view);
        noRatingsTitleTextView = view.findViewById(R.id.no_ratings_title_text_view);
        noRatingsTextView = view.findViewById(R.id.no_rating_text_textview);
        ratingsSummaryLayout = view.findViewById(R.id.rating_summary_constraintlayout);
        rateThisListTextView = view.findViewById(R.id.rate_this_list_textview);

        myRatingTextView = view.findViewById(R.id.my_rating_textview);
        moreOptionIcon = view.findViewById(R.id.ratings_more_option_icon);
        confirmRatingTextView = view.findViewById(R.id.confirm_rating_textview);
        ratingButtonsLayout = view.findViewById(R.id.ratings_cancel_confirm_layout);
        ratingBar = view.findViewById(R.id.rating_bar);


        numberFiveStarsIndicator = view.findViewById(R.id.five_star_indicator);
        numberFourStarsIndicator = view.findViewById(R.id.four_star_indicator);
        numberThreeStarsIndicator = view.findViewById(R.id.three_star_indicator);
        numberTwoStarsIndicator = view.findViewById(R.id.two_star_indicator);
        numberOneStarsIndicator = view.findViewById(R.id.one_star_indicator);
        averageRatingBar = view.findViewById(R.id.average_rating_bar);
        averageRatingBar.setIndicator(true);
        averageRatingTextView = view.findViewById(R.id.average_rating_textview);

        recyclerView = view.findViewById(R.id.ratings_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(new ItemDecorationWithInitialSpace(38, 16));
        recyclerView.setLayoutManager(layoutManager);
        ratings = new ArrayList<>();
        ratingAdapter = new RatingAdapter(ratings);
        recyclerView.setAdapter(ratingAdapter);
    }

    private void setSummaryRatingsIndicators(List<Rating> ratings) {
        int totalRatings = ratings.size();
        float fiveStarRatings = 0f;
        float fourStarRatings = 0f;
        float threeStarRatings = 0f;
        float twoStarRatings = 0f;
        float oneStarRatings = 0f;

        for (Rating rating : ratings) {
            switch (rating.getValue()) {
                case 1:
                    oneStarRatings++;
                    break;
                case 2:
                    twoStarRatings++;
                    break;
                case 3:
                    threeStarRatings++;
                    break;
                case 4:
                    fourStarRatings++;
                    break;
                case 5:
                    fiveStarRatings++;
                    break;
            }
        }

        numberOneStarsIndicator.setProgressCompat(Math.round(oneStarRatings / totalRatings * 100), true);
        numberTwoStarsIndicator.setProgressCompat(Math.round(twoStarRatings / totalRatings * 100), true);
        numberThreeStarsIndicator.setProgressCompat(Math.round(threeStarRatings / totalRatings * 100), true);
        numberFourStarsIndicator.setProgressCompat(Math.round(fourStarRatings / totalRatings * 100), true);
        numberFiveStarsIndicator.setProgressCompat(Math.round(fiveStarRatings / totalRatings * 100), true);
    }

    private Rating getLoggedUserRating() {
        if (ratings == null || ratings.size() == 0)
            return null;

        for (Rating rating : ratings) {
            if (rating.getAuthor().getUsername().equals(LoggedUser.getInstance().getUsername()))
                return rating;
        }

        return null;
    }

    private void showNoRatingsGui() {
        noRatingsImageView.setVisibility(View.VISIBLE);
        noRatingsTitleTextView.setVisibility(View.VISIBLE);
        ratingsSummaryLayout.setVisibility(View.GONE);
        if (movieListBasic.getOwnerId().equals(LoggedUser.getInstance().getUsername()))
            noRatingsTextView.setVisibility(View.GONE);
        else
            noRatingsTextView.setVisibility(View.VISIBLE);
        ratingBar.setRating(0);
        rateThisListTextView.setVisibility(View.VISIBLE);
        myRatingTextView.setVisibility(View.INVISIBLE);
        moreOptionIcon.setVisibility(View.GONE);
    }

    private void hideNoRatingsGui() {
        noRatingsTextView.setVisibility(View.GONE);
        noRatingsImageView.setVisibility(View.GONE);
        noRatingsTitleTextView.setVisibility(View.GONE);
        ratingsSummaryLayout.setVisibility(View.VISIBLE);
    }
}