package it.unina.cinemates.ui.user.movielist.common.recyclerview;


import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import it.unina.cinemates.R;
import it.unina.cinemates.retrofit.tmdb.TmDbConstants;
import it.unina.cinemates.ui.user.movielist.common.recyclerview.selection.MoviePosterDetails;
import it.unina.cinemates.ui.common.utils.GlideUtils;
import lombok.Getter;


@Getter
public class MoviePosterHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "MoviePoster_HOLDER";

    private MoviePosterDetails moviePosterDetails;
    public MoviePosterDetails getDetails() { return moviePosterDetails; }


    private final RelativeLayout selectedOverlay;
    public RelativeLayout getSelectedOverlay() { return selectedOverlay; }

    private final RelativeLayout unselectedOverlay;
    public RelativeLayout getUnselectedOverlay() { return unselectedOverlay; }

    private final View view;
    public View getView() { return view; }

    private final ImageView moviePosterImageView;

    public MoviePosterHolder(@NonNull View itemView) {
        super(itemView);

        moviePosterImageView = itemView.findViewById(R.id.movielist_movie_imageview);

        selectedOverlay = itemView.findViewById(R.id.movielist_movie_holder_selected_overlay);

        unselectedOverlay = itemView.findViewById(R.id.movielist_movie_holder_unselected_overlay);

        view = itemView;
    }

    public void setPosterImage(String posterImage, Integer id) {

        this.moviePosterDetails = new MoviePosterDetails(this.getBindingAdapterPosition(), Long.valueOf(id));

        clearImageViews();
        if (posterImage != null) {
            GlideUtils.loadAndSetImage(TmDbConstants.Images.smallPosterPath(posterImage), moviePosterImageView, view.getContext());
        }
    }

    private void clearImageViews() {
        moviePosterImageView.setImageDrawable(null);
    }
}
