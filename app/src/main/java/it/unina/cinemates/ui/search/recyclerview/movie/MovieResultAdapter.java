package it.unina.cinemates.ui.search.recyclerview.movie;

import android.content.Context;
import android.os.Bundle;
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
import it.unina.cinemates.ui.user.movie.AddMovieToListBottomSheet;
import it.unina.cinemates.viewmodels.MovieFullViewModel;
import it.unina.cinemates.views.tmdb.MovieResult;

public class MovieResultAdapter extends RecyclerView.Adapter<MovieResultHolder>{

    private final Context context;
    private final FragmentActivity activity;
    private List<MovieResult> movieResults;

    public MovieResultAdapter(List<MovieResult> movieResults, Context context, FragmentActivity activity){
        this.movieResults = movieResults;
        this.activity = activity;
        this.context = context;
    }

    @NonNull
    @Override
    public MovieResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_result_holder,parent,false);

        return new MovieResultHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieResultHolder holder, int position) {

        MovieResult movie = movieResults.get(position);

        holder.setImage(TmDbConstants.Images.smallPosterPath(movie.getPosterPath()));

        holder.setTitle(movie.getTitle());

        holder.setDescription(movie.getDescription());

        holder.setTmDbRating(movie.getTmDbRating());

        holder.setGenres(movie.getGenres());

        holder.setReleaseYear(movie.getReleaseDate());

        MovieFullViewModel movieFullViewModel = new ViewModelProvider(activity).get(MovieFullViewModel.class);
        holder.itemView.setOnClickListener(v -> {
            TmDbRetrofitService.getTmDbDaoInstance().getMoviesDetailsById(movie.getId())
                .enqueue(TmDbCallbacks.movieDetailsCallBack(movieFullViewModel));
            NavController navController = Navigation.findNavController(holder.itemView);
            navController.navigate(R.id.navigation_movie_full);});

        holder.getAddToListButton().setOnClickListener(v -> {
            AddMovieToListBottomSheet addMovieToLIstBottomSheet = new AddMovieToListBottomSheet();
            Bundle bundle = new Bundle();
            bundle.putInt("movieId",movie.getId());
            addMovieToLIstBottomSheet.setArguments(bundle);
            addMovieToLIstBottomSheet.show(activity.getSupportFragmentManager(), "Add to list");
        });

    }

    @Override
    public int getItemCount() {
        return movieResults.size();
    }

    @Override
    public long getItemId(int position) {
        return movieResults.get(position).getId();
    }

    public void setMovieList(List<MovieResult> movieResults) {
        this.movieResults = movieResults;
    }
}
