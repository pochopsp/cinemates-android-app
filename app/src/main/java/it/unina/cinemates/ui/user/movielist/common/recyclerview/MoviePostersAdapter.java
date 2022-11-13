package it.unina.cinemates.ui.user.movielist.common.recyclerview;


import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.unina.cinemates.R;
import it.unina.cinemates.ui.common.interfaces.OnClickListenerGenerator;
import it.unina.cinemates.views.tmdb.MoviePoster;

public class MoviePostersAdapter extends RecyclerView.Adapter<MoviePosterHolder> {

    //private static final String TAG = "MOVIEPOSTERS_ADAPTER";

    private List<MoviePoster> moviePosters;
    public List<MoviePoster> getMoviePosters(){
        return moviePosters;
    }
    @SuppressLint("NotifyDataSetChanged")
    public void loadMoviePosters(List<MoviePoster> newMoviePosters){
        this.moviePosters = newMoviePosters;
        this.notifyDataSetChanged();
    }

    //this is the tracker that will memorize what elements are selected in recycler view
    private SelectionTracker<Long> selectionTracker; //Integer parameter since the keys for the movies are Integer ids.

    private final OnClickListenerGenerator<Integer> onClickListenerGenerator;

    public MoviePostersAdapter(List<MoviePoster> moviePosters, OnClickListenerGenerator<Integer> onClickListenerGenerator){
        this.onClickListenerGenerator = onClickListenerGenerator;
        this.moviePosters = moviePosters;
        this.setHasStableIds(true); //this in combination with overriding getItemId in adapter provide us with animations
    }

    //this in combination with calling this.setHasStableIds(true) provide us with animations
    @Override
    public long getItemId(int position) {
        return moviePosters.get(position).getId();
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    @NonNull
    @Override
    public MoviePosterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movielist_movie_holder,parent,false);
        return new MoviePosterHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MoviePosterHolder holder, int position) {

        MoviePoster movie = moviePosters.get(position);

        holder.setPosterImage(movie.getPosterPath(), movie.getId());

        holder.getView().setOnClickListener(this.onClickListenerGenerator.generate(movie.getId()));

        if(selectionMode)
            holder.getUnselectedOverlay().setVisibility(View.VISIBLE);
        else
            holder.getUnselectedOverlay().setVisibility(View.INVISIBLE);

        if(selectionTracker != null){
            if(selectionTracker.isSelected(Long.valueOf(movie.getId())))
                holder.getSelectedOverlay().setVisibility(View.VISIBLE);
            else
                holder.getSelectedOverlay().setVisibility(View.INVISIBLE);
        }

    }

    //-----------------------------------------------------------------------------------------
    // this field and these two methods are used to get unselected overlay when entering selection mode
    private boolean selectionMode = false;
    @SuppressLint("NotifyDataSetChanged")
    public void enterSelectionMode(){
        this.selectionMode = true;
        notifyDataSetChanged();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void exitSelectionMode(){
        this.selectionMode = false;
        notifyDataSetChanged();
    }
    //-----------------------------------------------------------------------------------------


    @Override
    public int getItemCount() {
        return moviePosters.size();
    }

}
