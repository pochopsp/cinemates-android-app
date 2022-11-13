package it.unina.cinemates.ui.user.customMovielists.common.recyclerview;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import it.unina.cinemates.R;
import it.unina.cinemates.retrofit.tmdb.TmDbConstants;
import it.unina.cinemates.ui.common.utils.GlideUtils;
import lombok.Getter;


@Getter
public class MovieListPreviewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "MOVIELISTPREVIEW_HOLDER";
    private List<Pair<ImageView, ImageView>> moviePostersWithOverlays = new ArrayList<>();

    private TextView listNameTextView;
    private TextView reactionsNumberTextView;
    private TextView commentsNumberTextView;
    private TextView averageRatingTextView;

    public View getView() {
        return view;
    }

    private View view;

    public MovieListPreviewHolder(@NonNull View itemView) {
        super(itemView);

        listNameTextView = itemView.findViewById(R.id.movielist_preview_title_textview);
        reactionsNumberTextView = itemView.findViewById(R.id.movielist_preview_reactions_number);
        commentsNumberTextView = itemView.findViewById(R.id.movielist_preview_comments_number);
        averageRatingTextView = itemView.findViewById(R.id.movielist_preview_avg_rating_value);

        Resources resources = itemView.getResources();

        //set all movies poster imageViews for the list
        IntStream.range(1, 5).forEach(num -> {
            int posterOverlayId = resources.getIdentifier("movie_poster_overlay_"+ num, "id", itemView.getContext().getPackageName());
            int posterImageId = resources.getIdentifier("movie_poster_image_"+ num, "id", itemView.getContext().getPackageName());

            ImageView posterImage = itemView.findViewById(posterImageId);
            ImageView posterOverlay = itemView.findViewById(posterOverlayId);

            moviePostersWithOverlays.add(new Pair<>(posterImage, posterOverlay));
        });

        view = itemView;
    }


    public void setPosterPaths(List<String> posterPaths) {
        clearImageViews();
        if(posterPaths.size() != 0){
            ImageView overlay = view.findViewById(R.id.notempty_movielist_imageview);
            overlay.setVisibility(View.VISIBLE);
            int i;
            for (i = 0; i < posterPaths.size(); ++i) {
                if(posterPaths.get(i) == null || posterPaths.get(i).equals(""))
                    moviePostersWithOverlays.get(i).first.setImageResource(R.drawable.no_poster_available_movielist_preview);
                else
                    GlideUtils.loadAndSetImage(TmDbConstants.Images.smallPosterPath(posterPaths.get(i)), moviePostersWithOverlays.get(i).first, view.getContext());
            }
            moviePostersWithOverlays.get(i-1).second.setVisibility(View.VISIBLE);
        }
    }

    private void clearImageViews() {
        ImageView overlay = view.findViewById(R.id.notempty_movielist_imageview);
        overlay.setVisibility(View.INVISIBLE);
        for (int i = 0; i < 4; ++i) {
            moviePostersWithOverlays.get(i).first.setImageDrawable(null);
            moviePostersWithOverlays.get(i).second.setVisibility(View.INVISIBLE);
        }
    }

}
