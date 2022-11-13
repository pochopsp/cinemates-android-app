package it.unina.cinemates.ui.home.recyclerview.carousel;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import it.unina.cinemates.R;

public class MovieCarouselHolder extends RecyclerView.ViewHolder{

    private final ImageView imageView;

    public MovieCarouselHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.movieCarousel_imageview);
    }

    public ImageView getImageView() {
        return imageView;
    }
}
