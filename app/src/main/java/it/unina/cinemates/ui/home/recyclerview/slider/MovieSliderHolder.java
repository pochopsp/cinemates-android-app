package it.unina.cinemates.ui.home.recyclerview.slider;

import android.view.View;
import android.widget.ImageView;

import com.smarteist.autoimageslider.SliderViewAdapter;

import it.unina.cinemates.R;

public class MovieSliderHolder extends SliderViewAdapter.ViewHolder {


    private final ImageView imageView;

    public MovieSliderHolder(View itemView) {
        super(itemView);

        imageView = itemView.findViewById(R.id.home_slider_imageview);
    }

    public ImageView getImageView() {
        return imageView;
    }
}
