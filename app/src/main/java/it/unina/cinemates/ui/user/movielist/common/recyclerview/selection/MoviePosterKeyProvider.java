package it.unina.cinemates.ui.user.movielist.common.recyclerview.selection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;

import java.util.List;

import it.unina.cinemates.ui.user.movielist.common.recyclerview.MoviePostersAdapter;
import it.unina.cinemates.views.tmdb.MoviePoster;

// Provides recycler view selection library access to stable selection keys identifying items presented
public class MoviePosterKeyProvider extends ItemKeyProvider<Long> {

    private MoviePostersAdapter moviePostersAdapter;

    public MoviePosterKeyProvider(MoviePostersAdapter moviePostersAdapter) {
        super(SCOPE_CACHED);
        this.moviePostersAdapter = moviePostersAdapter;
    }

    @Nullable
    @Override
    public Long getKey(int position) {
        return Long.valueOf(moviePostersAdapter.getMoviePosters().get(position).getId());
    }

    @Override
    public int getPosition(@NonNull Long key) {
        List<MoviePoster> moviePosters = moviePostersAdapter.getMoviePosters();

        for(int i = 0; i < moviePosters.size(); ++i){
            if(Long.valueOf(moviePosters.get(i).getId()).equals(key))
                return i;
        }
        return -1;
    }

}
