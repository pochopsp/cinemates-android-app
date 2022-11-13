package it.unina.cinemates.ui.user.movielist.common.recyclerview.selection;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

import it.unina.cinemates.ui.user.movielist.common.recyclerview.MoviePosterHolder;

public class MoviePosterDetailsLookup extends ItemDetailsLookup<Long> {

    private final RecyclerView moviePostersRecyclerView;

    public MoviePosterDetailsLookup(RecyclerView moviePostersRecyclerView){
        this.moviePostersRecyclerView = moviePostersRecyclerView;
    }

    @Nullable
    @Override
    public ItemDetails<Long> getItemDetails(@NonNull MotionEvent e) {
        View view = moviePostersRecyclerView.findChildViewUnder(e.getX(), e.getY());
        if(view != null){
            RecyclerView.ViewHolder viewHolder = moviePostersRecyclerView.getChildViewHolder(view);
            if(viewHolder instanceof MoviePosterHolder)
                return  ((MoviePosterHolder)viewHolder).getDetails();
        }
        return  null;
    }
}
