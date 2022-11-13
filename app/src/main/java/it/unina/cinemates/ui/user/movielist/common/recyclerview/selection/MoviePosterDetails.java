package it.unina.cinemates.ui.user.movielist.common.recyclerview.selection;

import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;

/**
    The RecyclerView Selection library calls getItemDetails(MotionEvent) when it needs access to information
    about the area and/or ItemDetailsLookup.ItemDetails under a MotionEvent. Your implementation
    must negotiate ViewHolder lookup with the corresponding RecyclerView instance, and the
    subsequent conversion of the ViewHolder instance to an ItemDetailsLookup.ItemDetails instance.
    */
public class MoviePosterDetails extends ItemDetailsLookup.ItemDetails<Long> {

    private int position;
    private Long key;

    public MoviePosterDetails(int position, Long key) {
        this.position = position;
        this.key = key;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Nullable
    @Override
    public Long getSelectionKey() {
        return key;
    }
}
