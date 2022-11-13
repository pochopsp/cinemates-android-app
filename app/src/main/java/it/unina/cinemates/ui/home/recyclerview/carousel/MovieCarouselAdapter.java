package it.unina.cinemates.ui.home.recyclerview.carousel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.unina.cinemates.R;
import it.unina.cinemates.retrofit.tmdb.TmDbCallbacks;
import it.unina.cinemates.retrofit.tmdb.TmDbRetrofitService;
import it.unina.cinemates.retrofit.tmdb.TmDbConstants;
import it.unina.cinemates.ui.common.utils.GlideUtils;
import it.unina.cinemates.viewmodels.MovieFullViewModel;
import it.unina.cinemates.views.tmdb.MovieBasicResult;

public class MovieCarouselAdapter extends RecyclerView.Adapter<MovieCarouselHolder> {

    private List<MovieBasicResult> movies;
    private final Context context;
    private final FragmentActivity activity;

    public MovieCarouselAdapter(List<MovieBasicResult> movies, Context context, FragmentActivity activity) {
        this.movies = movies;
        this.context = context;
        this.activity = activity;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void loadMovies(List<MovieBasicResult> movies) {
        if (movies.size() < 5)
            this.movies = movies;
        else
            this.movies = movies.subList(0, 5);

        notifyDataSetChanged();
    }


    @SuppressLint("NotifyDataSetChanged")
    public void loadMovies(List<MovieBasicResult> movies, int start, int end) {
        if (movies.size() < 5)
            this.movies = movies;
        else
            this.movies = movies.subList(start, end + 1);

        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public MovieCarouselHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.homepage_carousel_holder, parent, false);
        return new MovieCarouselHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieCarouselHolder holder, int position) {

        GlideUtils.loadAndSetImage(TmDbConstants.Images.smallBackdropPath(movies.get(position).getPosterPath()), holder.getImageView(), context);

        MovieFullViewModel movieFullViewModel = new ViewModelProvider(activity).get(MovieFullViewModel.class);
        holder.itemView.setOnClickListener(v -> {
            TmDbRetrofitService.getTmDbDaoInstance().getMoviesDetailsById(movies.get(position).getId())
                    .enqueue(TmDbCallbacks.movieDetailsCallBack(movieFullViewModel));
            NavController navController = Navigation.findNavController(holder.itemView);
            navController.navigate(R.id.navigation_movie_full);
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }
}
