package it.unina.cinemates.ui.common.recyclerview;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ItemDecorationWithInitialSpace extends RecyclerView.ItemDecoration {

    private final int spaceBetweenItems;
    private final int initialSpace;
    private boolean horizontal = false;

    /** default spaceBetweenItems is initialSpace + 14 **/
    public ItemDecorationWithInitialSpace(int initialSpace) {
        this.spaceBetweenItems = initialSpace + 14;
        this.initialSpace = initialSpace;
    }

    public ItemDecorationWithInitialSpace(int spaceBetweenItems, int initialSpace) {
        this.spaceBetweenItems = spaceBetweenItems;
        this.initialSpace = initialSpace;
    }

    /** default spaceBetweenItems is initialSpace + 14 **/
    public ItemDecorationWithInitialSpace(int initialSpace, boolean horizontal) {
        this.spaceBetweenItems = initialSpace + 14;
        this.initialSpace = initialSpace;
        this.horizontal = horizontal;
    }

    public ItemDecorationWithInitialSpace(int spaceBetweenItems, int initialSpace, boolean horizontal) {
        this.spaceBetweenItems = spaceBetweenItems;
        this.initialSpace = initialSpace;
        this.horizontal = horizontal;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if(!horizontal){
            outRect.bottom = spaceBetweenItems;

            // Add margin only for the first item to avoid double space between items
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = initialSpace;
            }
        }else{
            outRect.right = spaceBetweenItems;

            // Add margin only for the first item to avoid double space between items
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.left = initialSpace;
            }
        }
    }
}
