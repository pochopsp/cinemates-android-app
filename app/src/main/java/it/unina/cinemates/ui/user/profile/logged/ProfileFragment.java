package it.unina.cinemates.ui.user.profile.logged;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import it.unina.cinemates.LoginActivity;
import it.unina.cinemates.MainActivity;
import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cloudinary.CloudinaryHelper;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.retrofit.tmdb.TmDbConstants;
import it.unina.cinemates.ui.common.utils.AlertDialogUtils;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.ui.common.utils.GlideUtils;
import it.unina.cinemates.viewmodels.user.profile.logged.ProfileViewModel;
import it.unina.cinemates.viewmodels.user.profile.logged.LoginStatusViewModel;
import it.unina.cinemates.views.backend.MovieListPreview;
import it.unina.cinemates.views.backend.UserProfile;
import it.unina.cinemates.views.backend.enums.FollowPageType;


public class ProfileFragment extends Fragment {

    private static final String TAG = "LOGGEDUSERPROFILE_FRAGMENT";

    private LoginStatusViewModel loginStatusViewModel;

    private ProfileViewModel profileViewModel;


    private ImageView profilePicImageView;
    private TextView usernameTextView;
    private Button followersNumberButton;
    private Snackbar noFollowersToShowSnackbar;
    private Button followingNumberButton;
    private Snackbar noFollowingToShowSnackbar;
    private TextView bioTextView;

    private AlertDialog logoutDialog;
    private Snackbar checkYourConnectionErrorSnackbar;


    private TextView toWatchReactionsNumberTextView;
    private TextView toWatchCommentsNumberTextView;
    private TextView toWatchAverageRatingTextView;
    private final List<Pair<ImageView, ImageView>> toWatchMoviePostersWithOverlays = new ArrayList<>();

    private TextView favoritesReactionsNumberTextView;
    private TextView favoritesCommentsNumberTextView;
    private TextView favoritesAverageRatingTextView;
    private final List<Pair<ImageView, ImageView>> favoritesMoviePostersWithOverlays = new ArrayList<>();
    private ImageView toWatchMovieListOverlay;
    private ImageView favoritesMovieListOverlay;
    private ScrollView profileScrollView;


    private ShowcaseView showcaseView = null;
    private Button otherListsButton;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        loginStatusViewModel = new ViewModelProvider(requireActivity()).get(LoginStatusViewModel.class);
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

        View view = inflater.inflate(R.layout.fragment_logged_user_profile, container, false);

        profileScrollView = view.findViewById(R.id.logged_user_profile_scrollview);
        noFollowersToShowSnackbar = SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.no_followers_to_show));
        noFollowingToShowSnackbar = SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.no_following_to_show));

        setupTextAndImageViews(view);
        setupLogoutDialogAndErrorSnackbar();
        setupBackIcon(view);
        setupMoreOptionsIcon(view);
        setupOtherListsButton(view);

        profileViewModel.fetchUserProfile(LoggedUser.getInstance().getUsername());

        profileViewModel.getUserProfile().observe(getViewLifecycleOwner(), userProfile -> {
            if (!userProfile.getUsername().equals("")) {
                loadUserDataIntoViews(view, userProfile);
            }
        });

        profileViewModel.getToWatchPostersPaths().observe(getViewLifecycleOwner(), moviePosters -> {
            clearToWatchMovieListImageViews();
            if (!moviePosters.isEmpty())
                toWatchMovieListOverlay.setVisibility(View.VISIBLE);
            for (int i = 0; i < moviePosters.size(); ++i) {
                if (moviePosters.get(i).getPosterPath() == null || moviePosters.get(i).getPosterPath().equals(""))
                    toWatchMoviePostersWithOverlays.get(i).first.setImageResource(R.drawable.no_poster_available_movielist_preview);
                else
                    GlideUtils.loadAndSetImage(TmDbConstants.Images.smallPosterPath(moviePosters.get(i).getPosterPath()), toWatchMoviePostersWithOverlays.get(i).first, requireContext());

                if (i == moviePosters.size() - 1)
                    toWatchMoviePostersWithOverlays.get(i).second.setVisibility(View.VISIBLE);
            }
        });


        profileViewModel.getFavoritesPostersPaths().observe(getViewLifecycleOwner(), moviePosters -> {
            clearFavoritesMovieListImageViews();
            if (!moviePosters.isEmpty())
                favoritesMovieListOverlay.setVisibility(View.VISIBLE);
            for (int i = 0; i < moviePosters.size(); ++i) {
                if (moviePosters.get(i).getPosterPath() == null || moviePosters.get(i).getPosterPath().equals(""))
                    favoritesMoviePostersWithOverlays.get(i).first.setImageResource(R.drawable.no_poster_available_movielist_preview);
                else
                    GlideUtils.loadAndSetImage(TmDbConstants.Images.smallPosterPath(moviePosters.get(i).getPosterPath()), favoritesMoviePostersWithOverlays.get(i).first, requireContext());

                if (i == moviePosters.size() - 1)
                    favoritesMoviePostersWithOverlays.get(i).second.setVisibility(View.VISIBLE);
            }
        });


        loginStatusViewModel.getUserStatus().observe(getViewLifecycleOwner(), loginStatus -> {
            switch (loginStatus) {
                case SERVER_UNREACHABLE:
                    ProfileFragment.this.checkYourConnectionErrorSnackbar.show();
                    break;
                case LOGGED_OUT:
                    Intent loginActivityIntent = new Intent(requireActivity(), LoginActivity.class);
                    requireActivity().startActivity(loginActivityIntent);
                    requireActivity().finish();
                    break;
            }
        });

        return view;
    }

    private void setupOtherListsButton(View view) {
        otherListsButton = view.findViewById(R.id.logged_user_profile_otherMovieLists_button);
        otherListsButton.setOnClickListener(v -> {
            if(showcaseView != null)
                showcaseView.hide();
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_logged_user_other_lists);
        });
    }

    private void loadUserDataIntoViews(View view, it.unina.cinemates.views.backend.UserProfile userProfile) {
        String idPhoto = userProfile.getIdPhoto() == null ? "" : userProfile.getIdPhoto().toString();

        GlideUtils.loadAndSetCircleImageWithUserPlaceholder(CloudinaryHelper.imagePath(idPhoto), profilePicImageView, this.requireContext());

        usernameTextView.setText(userProfile.getUsername());

        followersNumberButton.setText(String.valueOf(userProfile.getFollowersNumber()));
        followersNumberButton.setOnClickListener(v -> {
            if (userProfile.getFollowersNumber() > 0) {
                Bundle bundle = new Bundle();
                bundle.putInt("FollowPageTypeOrdinal", FollowPageType.MY_FOLLOWERS.ordinal());
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.navigation_follow, bundle);
            } else
                noFollowersToShowSnackbar.show();
        });

        followingNumberButton.setText(String.valueOf(userProfile.getFollowingNumber()));
        followingNumberButton.setOnClickListener(v -> {
            if (userProfile.getFollowingNumber() > 0) {
                Bundle bundle = new Bundle();
                bundle.putInt("FollowPageTypeOrdinal", FollowPageType.MY_FOLLOWING.ordinal());
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.navigation_follow, bundle);
            } else
                noFollowingToShowSnackbar.show();
        });

        if (userProfile.getBio() != null)
            bioTextView.setText(userProfile.getBio());
        else
            bioTextView.setText(R.string.userprofile_biotext);

        setupToWatchMovieListPreview(view, userProfile);
        setupFavoritesMovieListPreview(view, userProfile);


        // begin custom lists tutorial
        requireActivity();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(
                "sharedPreferences",
                Context.MODE_PRIVATE
        );

        if (!sharedPreferences.getBoolean("customListsTutorial", false)) {

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("customListsTutorial", true);
            editor.apply();

            showcaseView = new ShowcaseView.Builder(requireActivity())
                    .setTarget(new ViewTarget(otherListsButton))
                    .setContentTitle(getString(R.string.customMovielists_tutorial_title))
                    .setContentText(R.string.customMovielists_tutorial_body)
                    .hideOnTouchOutside()
                    .build();
        }
        // end custom lists tutorial


        profileScrollView.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    private void setupFavoritesMovieListPreview(View view, UserProfile userProfile) {
        MovieListPreview favoritesMovieListPreview = userProfile.getMovieLists().get(1);

        favoritesReactionsNumberTextView.setText(String.valueOf(favoritesMovieListPreview.getReactionsNumber()));
        favoritesCommentsNumberTextView.setText(String.valueOf(favoritesMovieListPreview.getCommentsNumber()));
        favoritesAverageRatingTextView.setText(favoritesMovieListPreview.getAverageRating().toString());

        RelativeLayout favoritesMovieListLayout = view.findViewById(R.id.logged_user_profile_favorites_movielist_preview);

        if (!favoritesMovieListPreview.getMoviesInList().isEmpty()) {
            favoritesMovieListOverlay.setVisibility(View.VISIBLE);
        }

        favoritesMovieListLayout.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            Bundle bundle = new Bundle();
            bundle.putInt("movieListId", favoritesMovieListPreview.getId());
            navController.navigate(R.id.navigation_logged_user_movie_list, bundle);
            profileViewModel.resetAllData();
        });
    }

    @SuppressLint("SetTextI18n")
    private void setupToWatchMovieListPreview(View view, UserProfile userProfile) {
        MovieListPreview toWatchMovieListPreview = userProfile.getMovieLists().get(0);

        toWatchReactionsNumberTextView.setText(String.valueOf(toWatchMovieListPreview.getReactionsNumber()));
        toWatchCommentsNumberTextView.setText(String.valueOf(toWatchMovieListPreview.getCommentsNumber()));
        toWatchAverageRatingTextView.setText(toWatchMovieListPreview.getAverageRating().toString());

        RelativeLayout toWatchMovieListLayout = view.findViewById(R.id.logged_user_profile_towatch_movielist_preview);

        if (!toWatchMovieListPreview.getMoviesInList().isEmpty()) {
            toWatchMovieListOverlay.setVisibility(View.VISIBLE);
        }

        toWatchMovieListLayout.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            Bundle bundle = new Bundle();
            bundle.putInt("movieListId", toWatchMovieListPreview.getId());
            navController.navigate(R.id.navigation_logged_user_movie_list, bundle);
            profileViewModel.resetAllData();
        });

    }

    private void setupBackIcon(View view) {
        ImageView backIcon = view.findViewById(R.id.logged_user_profile_back_icon);

        backIcon.setOnClickListener(v -> ProfileFragment.this.goBack());
    }

    @SuppressLint("NonConstantResourceId")
    private void setupMoreOptionsIcon(View view) {
        ImageView moreOptionsIcon = view.findViewById(R.id.logged_user_profile_more_option_icon);

        PopupMenu popup = new PopupMenu(requireContext(), moreOptionsIcon);
        popup.getMenuInflater().inflate(R.menu.userprofile_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            Log.e(TAG, "settando i listener del moreOptions popup...");
            switch (item.getItemId()) {
                case R.id.userprofile_settings_menu_item:
                    NavController navController = Navigation.findNavController(requireView());
                    Bundle bioBundle = new Bundle();
                    bioBundle.putString("userBio", Objects.requireNonNull(profileViewModel.getUserProfile().getValue()).getBio());
                    if (profileViewModel.getUserProfile().getValue().getBio() == null)
                        Log.e(TAG, "userBio NULL");
                    else
                        Log.e(TAG, "userBio NON_NULL");

                    navController.navigate(R.id.navigation_logged_user_profile_settings, bioBundle);
                    break;
                case R.id.userprofile_logout_menu_item:
                    logoutDialog.show();
                    break;
            }
            return true;
        });

        moreOptionsIcon.setOnClickListener(v -> popup.show());
    }


    private void setupTextAndImageViews(View view) {
        profilePicImageView = view.findViewById(R.id.logged_user_profile_pic_image_view);
        usernameTextView = view.findViewById(R.id.logged_user_profile_username_textview);
        followersNumberButton = view.findViewById(R.id.logged_user_profile_followersNumber_button);
        followingNumberButton = view.findViewById(R.id.logged_user_profile_followingNumber_button);
        bioTextView = view.findViewById(R.id.logged_user_profile_bio_textview);


        toWatchReactionsNumberTextView = view.findViewById(R.id.logged_user_profile_towatch_reactions_number);
        toWatchCommentsNumberTextView = view.findViewById(R.id.logged_user_profile_towatch_comments_number);
        toWatchAverageRatingTextView = view.findViewById(R.id.logged_user_profile_towatch_avg_rating_value);
        toWatchMoviePostersWithOverlays.clear();
        Resources resources = getResources();

        //set all movies poster imageViews for toWatch list
        IntStream.range(1, 5).forEach(num -> {
            int posterOverlayId = resources.getIdentifier("logged_user_profile_towatch_" + num + "_movie_poster_overlay", "id", requireContext().getPackageName());
            int posterImageId = resources.getIdentifier("logged_user_profile_towatch_" + num + "_movie_poster_image", "id", requireContext().getPackageName());

            ImageView posterImage = view.findViewById(posterImageId);
            ImageView posterOverlay = view.findViewById(posterOverlayId);

            toWatchMoviePostersWithOverlays.add(new Pair<>(posterImage, posterOverlay));
        });
        toWatchMovieListOverlay = view.findViewById(R.id.logged_user_profile_towatch_notempty_list_imageview);


        favoritesReactionsNumberTextView = view.findViewById(R.id.logged_user_profile_favorites_reactions_number);
        favoritesCommentsNumberTextView = view.findViewById(R.id.logged_user_profile_favorites_comments_number);
        favoritesAverageRatingTextView = view.findViewById(R.id.logged_user_profile_favorites_avg_rating_value);
        favoritesMoviePostersWithOverlays.clear();
        //set all movies poster imageViews for favorites list
        IntStream.range(1, 5).forEach(num -> {
            int posterOverlayId = resources.getIdentifier("logged_user_profile_favorites_" + num + "_movie_poster_overlay", "id", requireContext().getPackageName());
            int posterImageId = resources.getIdentifier("logged_user_profile_favorites_" + num + "_movie_poster_image", "id", requireContext().getPackageName());

            ImageView posterImage = view.findViewById(posterImageId);
            ImageView posterOverlay = view.findViewById(posterOverlayId);

            favoritesMoviePostersWithOverlays.add(new Pair<>(posterImage, posterOverlay));
        });
        favoritesMovieListOverlay = view.findViewById(R.id.logged_user_profile_favorites_notempty_list_imageview);

        clearMovieListsImageViews();
    }


    private void setupLogoutDialogAndErrorSnackbar() {
        DialogInterface.OnClickListener positiveButtonListener = (dialog, which) -> ((MainActivity) requireActivity()).logout();

        this.logoutDialog = AlertDialogUtils.alertDialog(requireContext(), R.string.logged_userprofile_logout_dialog_title,
                R.string.logged_userprofile_logout_dialog_message, R.string.logged_userprofile_logout_dialog_button_logout, positiveButtonListener);

        this.checkYourConnectionErrorSnackbar = SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.snackbar_server_unreachable));
    }


    private void clearMovieListsImageViews() {
        clearFavoritesMovieListImageViews();
        clearToWatchMovieListImageViews();
    }

    private void clearToWatchMovieListImageViews() {
        toWatchMovieListOverlay.setVisibility(View.INVISIBLE);
        for (int i = 0; i < 4; ++i) {
            toWatchMoviePostersWithOverlays.get(i).first.setImageResource(android.R.color.transparent);
            toWatchMoviePostersWithOverlays.get(i).second.setVisibility(View.INVISIBLE);
        }
    }

    private void clearFavoritesMovieListImageViews() {
        favoritesMovieListOverlay.setVisibility(View.INVISIBLE);
        for (int i = 0; i < 4; ++i) {
            favoritesMoviePostersWithOverlays.get(i).first.setImageResource(android.R.color.transparent);
            favoritesMoviePostersWithOverlays.get(i).second.setVisibility(View.INVISIBLE);
        }
    }

}