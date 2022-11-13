package it.unina.cinemates.ui.user.movie;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.analytics.FirebaseAnalytics;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.viewmodels.user.movielist.logged.AddMovieToMovieListViewModel;

public class AddMovieToListBottomSheet extends BottomSheetDialogFragment {

    private Integer movieId;
    private AddMovieToMovieListViewModel addMovieToMovieListViewModel;

    private final Bundle addToCustomListBundle = new Bundle();

    public AddMovieToListBottomSheet() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
        addMovieToMovieListViewModel = new ViewModelProvider(requireActivity()).get(AddMovieToMovieListViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_movies_to_list_bottom_sheet,container, false);
        Bundle bundle = getArguments();
        assert bundle != null;
        movieId = (Integer) bundle.get("movieId");


        int toWatchListId = LoggedUser.getInstance().getToWatchListId();
        Button toWatchButton = view.findViewById(R.id.add_to_watch_button);
        toWatchButton.setOnClickListener(v -> {

            FirebaseAnalytics.getInstance(requireContext()).logEvent("AddedMovieToDefaultList", null);

            this.addMovieToMovieListViewModel.addToMovieList(toWatchListId,movieId);
            AddMovieToListBottomSheet.this.dismiss();
        });

        int favoritesListId = LoggedUser.getInstance().getFavoritesListId();
        Button toFavoritesButton = view.findViewById(R.id.add_to_favorites_button);
        toFavoritesButton.setOnClickListener(v -> {

            FirebaseAnalytics.getInstance(requireContext()).logEvent("AddedMovieToDefaultList", null);

            this.addMovieToMovieListViewModel.addToMovieList(favoritesListId,movieId);
            AddMovieToListBottomSheet.this.dismiss();
        });


        addToCustomListBundle.putInt("movieId", movieId);
        Button toCustomListsButton = view.findViewById(R.id.add_to_custom_button);
        toCustomListsButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(AddMovieToListBottomSheet.this);
            navController.navigate(R.id.navigation_add_to_custom_movie_list, addToCustomListBundle);
            AddMovieToListBottomSheet.this.dismiss();
        });

        return view;
    }
}
