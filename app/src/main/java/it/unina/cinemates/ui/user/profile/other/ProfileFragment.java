package it.unina.cinemates.ui.user.profile.other;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cloudinary.CloudinaryHelper;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.retrofit.backend.enums.BackendOperationResult;
import it.unina.cinemates.retrofit.tmdb.TmDbConstants;
import it.unina.cinemates.ui.common.utils.AlertDialogUtils;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.ui.common.utils.GlideUtils;
import it.unina.cinemates.viewmodels.user.follow.FollowViewModel;
import it.unina.cinemates.viewmodels.user.profile.other.ProfileViewModel;
import it.unina.cinemates.views.backend.MovieListPreview;
import it.unina.cinemates.views.backend.UserProfile;
import it.unina.cinemates.views.backend.enums.FollowPageType;
import it.unina.cinemates.views.backend.enums.FollowRequestStatus;


public class ProfileFragment extends Fragment {

    private static final String TAG = "OTHERUSERPROFILE_FRAGMENT";

    private ProfileViewModel profileViewModel;

    private FollowViewModel followViewModel;

    private ImageView profilePicImageView;
    private TextView usernameTextView;
    private Button followersNumberButton;
    private Button followingNumberButton;
    private TextView bioTextView;

    private AlertDialog removeFollowDialog;


    private TextView toWatchReactionsNumberTextView;
    private TextView toWatchCommentsNumberTextView;
    private TextView toWatchAverageRatingTextView;
    private final List<Pair<ImageView, ImageView>> toWatchMoviePostersWithOverlays = new ArrayList<>();

    private TextView favoritesReactionsNumberTextView;
    private TextView favoritesCommentsNumberTextView;
    private TextView favoritesAverageRatingTextView;
    private final List<Pair<ImageView, ImageView>> favoritesMoviePostersWithOverlays = new ArrayList<>();


    private String otherUserUsername;
    private Button followButton;
    private Button pendingButton;
    private Button followingButton;
    private Button otherListsButton;

    private View followToSeeMovieListsLayout;

    private Snackbar noFollowingToShowSnackbar;
    private Snackbar noFollowersToShowSnackbar;


    private boolean flag = false;
    private ScrollView profileScrollView;
    private RelativeLayout toWatchMovieListLayout;
    private RelativeLayout favoritesMovieListLayout;


    public ProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        followViewModel = new ViewModelProvider(requireActivity()).get(FollowViewModel.class);
        overrideBackPressedBehavior();
    }

    private void overrideBackPressedBehavior() {
        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                ProfileFragment.this.goBack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void goBack() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigateUp();
        profileViewModel.resetAllData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_other_user_profile, container, false);

        profileScrollView = view.findViewById(R.id.other_user_profile_scrollview);
        noFollowersToShowSnackbar = SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.no_followers_to_show));
        noFollowingToShowSnackbar = SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.no_following_to_show));

        setupTextAndImageViews(view);

        setupBackIcon(view);

        followToSeeMovieListsLayout = view.findViewById(R.id.other_user_profile_follow_to_see_movie_lists_layout);
        followButton = view.findViewById(R.id.other_user_profile_follow_button);
        pendingButton = view.findViewById(R.id.other_user_profile_pending_follow_button);
        followingButton = view.findViewById(R.id.other_user_profile_following_button);

        setupRemoveFollowDialog();

        Bundle bundle = getArguments();
        assert bundle != null;
        this.otherUserUsername = (String) bundle.get("otherUserUsername");

        profileViewModel.fetchUserProfile(otherUserUsername);

        profileViewModel.getUserProfile().observe(getViewLifecycleOwner(), userProfile -> {
            if (!flag) {
                flag = true;
                profileViewModel.fetchUserProfile(otherUserUsername);
            } else {
                if (!userProfile.getUsername().equals(""))
                    loadUserDataIntoViews(view, userProfile);
            }
        });


        followViewModel.getPostFollowResult().observe(getViewLifecycleOwner(), backendOperationResult -> {

            if (backendOperationResult == BackendOperationResult.SUCCESS) {
                followButton.setVisibility(View.GONE);
                pendingButton.setVisibility(View.VISIBLE);
                followViewModel.resetPostFollowResult();
            } else if (backendOperationResult == BackendOperationResult.SERVER_UNREACHABLE) {
                SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.snackbar_server_unreachable)).show();
                followViewModel.resetPostFollowResult();
            }
        });


        followViewModel.getDeleteFollowResult().observe(getViewLifecycleOwner(), backendOperationResult -> {

            if (backendOperationResult == BackendOperationResult.SUCCESS) {

                followingButton.setVisibility(View.GONE);
                followButton.setVisibility(View.VISIBLE);
                followButton.setOnClickListener(v -> followViewModel.requestPostFollow(LoggedUser.getInstance().getUsername(), otherUserUsername));

                view.findViewById(R.id.other_user_profile_follow_to_see_movie_lists_layout).setVisibility(View.VISIBLE);
                toWatchMovieListLayout.setOnClickListener(null);
                favoritesMovieListLayout.setOnClickListener(null);

                otherListsButton.setVisibility(View.INVISIBLE);

                Objects.requireNonNull(profileViewModel.getUserProfile().getValue()).setFollowRequestStatus(FollowRequestStatus.NoRequest);
                int followersNumber = profileViewModel.getUserProfile().getValue().getFollowersNumber();
                profileViewModel.getUserProfile().getValue().setFollowersNumber(followersNumber - 1);
                followersNumberButton.setText(String.valueOf(followersNumber - 1));
                followersNumberButton.setOnClickListener(v -> {
                    if (followersNumber - 1 > 0) {
                        Bundle bundle1 = new Bundle();
                        bundle1.putInt("FollowPageTypeOrdinal", FollowPageType.OTHER_USER_FOLLOWERS.ordinal());
                        bundle1.putString("otherUserUsername", profileViewModel.getUserProfile().getValue().getUsername());
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigate(R.id.navigation_follow, bundle1);
                    } else
                        noFollowersToShowSnackbar.show();
                });

                followViewModel.resetDeleteFollowResult();
            } else if (backendOperationResult == BackendOperationResult.SERVER_UNREACHABLE) {
                SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.snackbar_server_unreachable)).show();
                followViewModel.resetDeleteFollowResult();
            }
        });


        profileViewModel.getToWatchPostersPaths().observe(getViewLifecycleOwner(), moviePosters -> {
            for (int i = 0; i < moviePosters.size(); ++i) {
                if(moviePosters.get(i).getPosterPath() == null || moviePosters.get(i).getPosterPath().equals(""))
                    toWatchMoviePostersWithOverlays.get(i).first.setImageResource(R.drawable.no_poster_available_movielist_preview);
                else
                    GlideUtils.loadAndSetImage(TmDbConstants.Images.smallPosterPath(moviePosters.get(i).getPosterPath()), toWatchMoviePostersWithOverlays.get(i).first, requireContext());

                if (i == moviePosters.size() - 1)
                        toWatchMoviePostersWithOverlays.get(i).second.setVisibility(View.VISIBLE);
            }
        });


        profileViewModel.getFavoritesPostersPaths().observe(getViewLifecycleOwner(), moviePosters -> {
            for (int i = 0; i < moviePosters.size(); ++i) {
                if(moviePosters.get(i).getPosterPath() == null || moviePosters.get(i).getPosterPath().equals(""))
                    favoritesMoviePostersWithOverlays.get(i).first.setImageResource(R.drawable.no_poster_available_movielist_preview);
                else
                    GlideUtils.loadAndSetImage(TmDbConstants.Images.smallPosterPath(moviePosters.get(i).getPosterPath()), favoritesMoviePostersWithOverlays.get(i).first, requireContext());

                if (i == moviePosters.size() - 1)
                    favoritesMoviePostersWithOverlays.get(i).second.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    private void setupOtherListsButton(View view) {
        otherListsButton = view.findViewById(R.id.other_user_profile_otherMovieLists_button);
        otherListsButton.setVisibility(View.VISIBLE);
        otherListsButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("otherUserUsername", otherUserUsername);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_other_user_other_lists, bundle);
        });
    }

    private void loadUserDataIntoViews(View view, UserProfile userProfile) {
        String idPhoto = userProfile.getIdPhoto() == null ? "" : userProfile.getIdPhoto().toString();

        GlideUtils.loadAndSetCircleImageWithUserPlaceholder(CloudinaryHelper.imagePath(idPhoto), profilePicImageView, this.requireContext());

        usernameTextView.setText(userProfile.getUsername());

        followersNumberButton.setText(String.valueOf(userProfile.getFollowersNumber()));
        followersNumberButton.setOnClickListener(v -> {
            if (userProfile.getFollowersNumber() > 0) {
                Bundle bundle = new Bundle();
                bundle.putInt("FollowPageTypeOrdinal", FollowPageType.OTHER_USER_FOLLOWERS.ordinal());
                bundle.putString("otherUserUsername", userProfile.getUsername());
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.navigation_follow, bundle);
            } else
                noFollowersToShowSnackbar.show();
        });

        followingNumberButton.setText(String.valueOf(userProfile.getFollowingNumber()));
        followingNumberButton.setOnClickListener(v -> {
            if (userProfile.getFollowingNumber() > 0) {
                Bundle bundle = new Bundle();
                bundle.putInt("FollowPageTypeOrdinal", FollowPageType.OTHER_USER_FOLLOWING.ordinal());
                bundle.putString("otherUserUsername", userProfile.getUsername());
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.navigation_follow, bundle);
            } else
                noFollowingToShowSnackbar.show();
        });

        FollowRequestStatus followRequestStatus = userProfile.getFollowRequestStatus();
        switch (followRequestStatus) {
            case NoRequest: {
                followingButton.setVisibility(View.INVISIBLE);
                pendingButton.setVisibility(View.INVISIBLE);
                followButton.setVisibility(View.VISIBLE);
                followButton.setOnClickListener(v -> followViewModel.requestPostFollow(LoggedUser.getInstance().getUsername(), otherUserUsername));
            }
            break;
            case Pending: {
                followingButton.setVisibility(View.INVISIBLE);
                followButton.setVisibility(View.INVISIBLE);
                pendingButton.setVisibility(View.VISIBLE);
            }
            break;
            case Accepted: {
                followButton.setVisibility(View.INVISIBLE);
                pendingButton.setVisibility(View.INVISIBLE);
                followingButton.setVisibility(View.VISIBLE);
                followingButton.setOnClickListener(v -> removeFollowDialog.show());
            }
            break;
        }

        if (userProfile.getBio() != null)
            bioTextView.setText(userProfile.getBio());
        else
            bioTextView.setText(R.string.userprofile_biotext);

        if (userProfile.getFollowRequestStatus() != FollowRequestStatus.Accepted) {
            followToSeeMovieListsLayout.setVisibility(View.VISIBLE);
        } else {
            Log.e(TAG, userProfile.getUsername());
            followToSeeMovieListsLayout.setVisibility(View.INVISIBLE);
            setupOtherListsButton(view);
            setupToWatchMovieListPreview(view, userProfile);
            setupFavoritesMovieListPreview(view, userProfile);
        }

        profileScrollView.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    private void setupFavoritesMovieListPreview(View view, UserProfile userProfile) {

        favoritesMovieListLayout = view.findViewById(R.id.other_user_profile_favorites_movielist_preview);
        favoritesMovieListLayout.setVisibility(View.VISIBLE);

        MovieListPreview favoritesMovieListPreview = userProfile.getMovieLists().get(1);

        favoritesReactionsNumberTextView.setText(String.valueOf(favoritesMovieListPreview.getReactionsNumber()));
        favoritesCommentsNumberTextView.setText(String.valueOf(favoritesMovieListPreview.getCommentsNumber()));
        favoritesAverageRatingTextView.setText(favoritesMovieListPreview.getAverageRating().toString());

        if (!favoritesMovieListPreview.getMoviesInList().isEmpty()) {
            ImageView overlay = view.findViewById(R.id.other_user_profile_favorites_notempty_list_imageview);
            overlay.setVisibility(View.VISIBLE);
        }

        favoritesMovieListLayout.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            Bundle bundle = new Bundle();
            bundle.putInt("movieListId", favoritesMovieListPreview.getId());
            navController.navigate(R.id.navigation_other_user_movie_list, bundle);
        });
    }

    @SuppressLint("SetTextI18n")
    private void setupToWatchMovieListPreview(View view, UserProfile userProfile) {

        toWatchMovieListLayout = view.findViewById(R.id.other_user_profile_towatch_movielist_preview);
        toWatchMovieListLayout.setVisibility(View.VISIBLE);

        MovieListPreview toWatchMovieListPreview = userProfile.getMovieLists().get(0);

        toWatchReactionsNumberTextView.setText(String.valueOf(toWatchMovieListPreview.getReactionsNumber()));
        toWatchCommentsNumberTextView.setText(String.valueOf(toWatchMovieListPreview.getCommentsNumber()));
        toWatchAverageRatingTextView.setText(toWatchMovieListPreview.getAverageRating().toString());


        if (!toWatchMovieListPreview.getMoviesInList().isEmpty()) {
            ImageView overlay = view.findViewById(R.id.other_user_profile_towatch_notempty_list_imageview);
            overlay.setVisibility(View.VISIBLE);
        }

        toWatchMovieListLayout.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            Bundle bundle = new Bundle();
            bundle.putInt("movieListId", toWatchMovieListPreview.getId());
            navController.navigate(R.id.navigation_other_user_movie_list, bundle);
        });

    }

    private void setupBackIcon(View view) {
        ImageView backIcon = view.findViewById(R.id.other_user_profile_back_icon);

        backIcon.setOnClickListener(v -> ProfileFragment.this.goBack());
    }


    private void setupTextAndImageViews(View view) {
        profilePicImageView = view.findViewById(R.id.other_user_profile_pic_image_view);
        usernameTextView = view.findViewById(R.id.other_user_profile_username_textview);
        followersNumberButton = view.findViewById(R.id.other_user_profile_followersNumber_button);
        followingNumberButton = view.findViewById(R.id.other_user_profile_followingNumber_button);
        bioTextView = view.findViewById(R.id.other_user_profile_bio_textview);


        toWatchReactionsNumberTextView = view.findViewById(R.id.other_user_profile_towatch_reactions_number);
        toWatchCommentsNumberTextView = view.findViewById(R.id.other_user_profile_towatch_comments_number);
        toWatchAverageRatingTextView = view.findViewById(R.id.other_user_profile_towatch_avg_rating_value);
        toWatchMoviePostersWithOverlays.clear();
        Resources resources = getResources();

        //set all movies poster imageViews for toWatch list
        IntStream.range(1, 5).forEach(num -> {
            int posterOverlayId = resources.getIdentifier("other_user_profile_towatch_" + num + "_movie_poster_overlay", "id", requireContext().getPackageName());
            int posterImageId = resources.getIdentifier("other_user_profile_towatch_" + num + "_movie_poster_image", "id", requireContext().getPackageName());

            ImageView posterImage = view.findViewById(posterImageId);
            ImageView posterOverlay = view.findViewById(posterOverlayId);

            toWatchMoviePostersWithOverlays.add(new Pair<>(posterImage, posterOverlay));
        });


        favoritesReactionsNumberTextView = view.findViewById(R.id.other_user_profile_favorites_reactions_number);
        favoritesCommentsNumberTextView = view.findViewById(R.id.other_user_profile_favorites_comments_number);
        favoritesAverageRatingTextView = view.findViewById(R.id.other_user_profile_favorites_avg_rating_value);
        favoritesMoviePostersWithOverlays.clear();
        //set all movies poster imageViews for favorites list
        IntStream.range(1, 5).forEach(num -> {
            int posterOverlayId = resources.getIdentifier("other_user_profile_favorites_" + num + "_movie_poster_overlay", "id", requireContext().getPackageName());
            int posterImageId = resources.getIdentifier("other_user_profile_favorites_" + num + "_movie_poster_image", "id", requireContext().getPackageName());

            ImageView posterImage = view.findViewById(posterImageId);
            ImageView posterOverlay = view.findViewById(posterOverlayId);

            favoritesMoviePostersWithOverlays.add(new Pair<>(posterImage, posterOverlay));
        });
    }


    private void setupRemoveFollowDialog() {
        removeFollowDialog = AlertDialogUtils.alertDialog(
                requireContext(),
                R.string.remove_follow_dialog_title,
                R.string.remove_follow_dialog_message,
                R.string.remove_follow_dialog_positive_button,
                (dialog, which) -> followViewModel.requestDeleteFollow(LoggedUser.getInstance().getUsername(), otherUserUsername)
        );
    }

}