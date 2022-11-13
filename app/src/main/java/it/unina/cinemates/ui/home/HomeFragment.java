package it.unina.cinemates.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.ui.common.recyclerview.ItemDecorationWithInitialSpace;
import it.unina.cinemates.ui.home.recyclerview.carousel.MovieCarouselAdapter;
import it.unina.cinemates.ui.home.recyclerview.slider.MovieSliderAdapter;
import it.unina.cinemates.viewmodels.HomeViewModel;
import it.unina.cinemates.viewmodels.enums.ViewAllType;
import it.unina.cinemates.views.tmdb.MovieBasicResult;


public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    private MovieSliderAdapter movieSliderAdapter;

    private SliderView nowPlayingSliderView;

    public HomeFragment() {

    }

    public static HomeFragment newInstance() {

        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_home, container, false);

        setupNowPlayingSliderView(view);
        setupPopularRecyclerView(view);
        setupUpcomingRecyclerView(view);
        setupTopRatedRecyclerView(view);

        setupListeners(view);

        return view;
    }



    private void setupNowPlayingSliderView(View view) {
        nowPlayingSliderView = view.findViewById(R.id.home_slider_image);
        nowPlayingSliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
        nowPlayingSliderView.setSliderTransformAnimation(SliderAnimations.DEPTHTRANSFORMATION);

        movieSliderAdapter = new MovieSliderAdapter(new ArrayList<>(), requireContext(),requireActivity());


        homeViewModel.getNowPlayingMovies().observe(getViewLifecycleOwner(), nowPlayingMovies -> {
            movieSliderAdapter.loadMovies(nowPlayingMovies);
            nowPlayingSliderView.setSliderAdapter(movieSliderAdapter);
            nowPlayingSliderView.startAutoCycle();
        });
    }

    private void setupPopularRecyclerView(View view) {
        RecyclerView popularRecyclerView = view.findViewById(R.id.popular_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        popularRecyclerView.addItemDecoration(new ItemDecorationWithInitialSpace(44, true));
        MovieCarouselAdapter movieCarouselAdapter = new MovieCarouselAdapter(new ArrayList<>(),requireContext(),requireActivity());
        popularRecyclerView.setLayoutManager(layoutManager);
        popularRecyclerView.setAdapter(movieCarouselAdapter);

        homeViewModel.getPopularMovies().observe(
                getViewLifecycleOwner(),
                movieBasicResults -> movieCarouselAdapter.loadMovies(movieBasicResults, 5, 9)
        );
    }

    private void setupUpcomingRecyclerView(View view) {
        RecyclerView upcomingRecyclerView = view.findViewById(R.id.upcoming_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        upcomingRecyclerView.addItemDecoration(new ItemDecorationWithInitialSpace(44, true));
        MovieCarouselAdapter movieCarouselAdapter = new MovieCarouselAdapter(new ArrayList<>(),requireContext(),requireActivity());
        upcomingRecyclerView.setLayoutManager(layoutManager);
        upcomingRecyclerView.setAdapter(movieCarouselAdapter);

        homeViewModel.getUpcomingMovies().observe(
                getViewLifecycleOwner(),
                movieBasicResults -> movieCarouselAdapter.loadMovies(movieBasicResults, 5, 9)
        );
    }

    private void setupTopRatedRecyclerView(View view) {
        RecyclerView topRatedRecyclerView = view.findViewById(R.id.top_rated_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        topRatedRecyclerView.addItemDecoration(new ItemDecorationWithInitialSpace(44, true));
        MovieCarouselAdapter movieCarouselAdapter = new MovieCarouselAdapter(new ArrayList<>(),requireContext(),requireActivity());
        topRatedRecyclerView.setLayoutManager(layoutManager);
        topRatedRecyclerView.setAdapter(movieCarouselAdapter);

        homeViewModel.getTopRatedMovies().observe(getViewLifecycleOwner(), movieCarouselAdapter::loadMovies);
    }


    private void setupListeners(View view){
        Button nowPlayingTextButton = view.findViewById(R.id.now_playing_more_text_button);
        nowPlayingTextButton.setOnClickListener(v -> {
            homeViewModel.setViewTypeLiveData(ViewAllType.NOW_PLAYING);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_view_all_movies);
        });

        Button popularTextButton = view.findViewById(R.id.popular_more_text_button);
        popularTextButton.setOnClickListener(v -> {
            homeViewModel.setViewTypeLiveData(ViewAllType.POPULAR);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_view_all_movies);
        });

        Button upcomingTextButton = view.findViewById(R.id.upcoming_more_text_button);
        upcomingTextButton.setOnClickListener(v -> {
            homeViewModel.setViewTypeLiveData(ViewAllType.UPCOMING);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_view_all_movies);
        });

        Button topRatedTextButton = view.findViewById(R.id.top_rated_more_text_button);
        topRatedTextButton.setOnClickListener(v -> {
            homeViewModel.setViewTypeLiveData(ViewAllType.TOP_RATED);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_view_all_movies);
        });
    }
}