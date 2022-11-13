package it.unina.cinemates.ui.home.recyclerview.slider;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.List;

import it.unina.cinemates.R;
import it.unina.cinemates.retrofit.tmdb.TmDbCallbacks;
import it.unina.cinemates.retrofit.tmdb.TmDbConstants;
import it.unina.cinemates.retrofit.tmdb.TmDbRetrofitService;
import it.unina.cinemates.ui.common.utils.GlideUtils;
import it.unina.cinemates.viewmodels.MovieFullViewModel;
import it.unina.cinemates.views.tmdb.MovieBasicResult;

public class MovieSliderAdapter extends SliderViewAdapter<MovieSliderHolder> {


    private List<MovieBasicResult> movies;
    private final Context context;
    private final FragmentActivity activity;

    public MovieSliderAdapter(List<MovieBasicResult> movies, Context context, FragmentActivity activity){
        this.movies = movies;
        this.context = context;
        this.activity = activity;
    }

    public void loadMovies(List<MovieBasicResult> movies){
        this.movies = movies.subList(0,5);
        notifyDataSetChanged();
    }


    @Override
    public MovieSliderHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.homepage_slider_holder,parent,false);

        return new MovieSliderHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieSliderHolder viewHolder, int position) {

        GlideUtils.loadAndSetImage(TmDbConstants.Images.mediumBackdropPath(movies.get(position).getBackdropPath()), viewHolder.getImageView(), context);

        MovieFullViewModel movieFullViewModel = new ViewModelProvider(activity).get(MovieFullViewModel.class);
        viewHolder.itemView.setOnClickListener(v -> {
            TmDbRetrofitService.getTmDbDaoInstance().getMoviesDetailsById(movies.get(position).getId())
                    .enqueue(TmDbCallbacks.movieDetailsCallBack(movieFullViewModel));
            NavController navController = Navigation.findNavController(viewHolder.itemView);
            navController.navigate(R.id.navigation_movie_full);
            }
        );

    }

    @Override
    public int getCount() {
        return movies.size();
    }
}
