package it.unina.cinemates.ui.search.recyclerview.movie;

import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.unina.cinemates.R;
import it.unina.cinemates.retrofit.tmdb.TmDbConstants;
import it.unina.cinemates.ui.common.utils.GlideUtils;

public class MovieResultHolder extends RecyclerView.ViewHolder {

    private final ImageView imageView;
    private final TextView titleTextView;
    private final TextView yearTextView;
    private final TextView genresTextView;
    private final TextView tmdbRatingTextView;
    private final TextView tmdbRatingValueTextView;
    private final TextView descriptionTextView;
    private final TextView addToListButton;

    public MovieResultHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.movie_poster_imageview);
        titleTextView = itemView.findViewById(R.id.movie_title_textview);
        yearTextView = itemView.findViewById(R.id.movie_year_textview);
        genresTextView = itemView.findViewById(R.id.genres_text_textview);
        tmdbRatingTextView = itemView.findViewById(R.id.tmdb_rating_text_textview);
        tmdbRatingValueTextView = itemView.findViewById(R.id.tmdb_rating_rate_textview);
        descriptionTextView = itemView.findViewById(R.id.movie_description_textview);
        addToListButton = itemView.findViewById(R.id.add_to_list_button);
    }

    public TextView getAddToListButton() {
        return addToListButton;
    }

    public void setTitle(String title) {
        this.titleTextView.setText(title);
    }

    public void setDescription(String description) {
        Resources r = itemView.getResources();

        if (description.isEmpty())
            this.descriptionTextView.setText(r.getString(R.string.no_description_available));
        else
            this.descriptionTextView.setText(description);
    }

    public void setTmDbRating(String tmDbRating) {
        if (Float.valueOf(tmDbRating) == 0f) {
            this.tmdbRatingValueTextView.setText("n/a");
        } else
            this.tmdbRatingValueTextView.setText(tmDbRating);
    }

    public void setGenres(List<Integer> movieGenres) {
        StringBuilder genreString = new StringBuilder();
        boolean flag = false;

        String currentLanguage = Locale.getDefault().getLanguage();
        Map<Integer, String> localizedGenres;
        if (currentLanguage.equals(new Locale("it").getLanguage()))
            localizedGenres = TmDbConstants.Italian.idGenres;
        else
            localizedGenres = TmDbConstants.English.idGenres;

        if (movieGenres.size() == 1) {
            genreString.append(localizedGenres.get(movieGenres.get(0)));
        } else {

            for (Integer genreId : movieGenres) { //Used to take only 2 or less genres
                if (flag) {
                    genreString.append(localizedGenres.get(genreId));
                    break;
                }
                genreString.append(localizedGenres.get(genreId)).append(", ");
                flag = true;
            }
        }
        if(genreString.length() == 0)
            genreString.append("N/A");
        this.genresTextView.setText(genreString);
    }

    public void setReleaseYear(String releaseDate) {
        if (releaseDate != null) {
            String year = releaseDate.split("-")[0];
            if (year.equals(""))
                year = "n/a";
            this.yearTextView.setText(year);
        }
    }

    public void setImage(String smallPosterPath) {
        GlideUtils.loadAndSetImage(smallPosterPath, this.imageView, itemView.getContext());
    }
}

