package it.unina.cinemates.views.backend;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import it.unina.cinemates.models.Movie;
import it.unina.cinemates.views.backend.enums.ListType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class MovieListPreview {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private ListType type;

    @SerializedName("moviesInList")
    private List<Movie> moviesInList;

    @Getter(AccessLevel.NONE)
    @SerializedName("averageRating")
    private Float averageRating;

    public Float getAverageRating() {
        return averageRating == null ? 0f : Float.parseFloat(String.format(Locale.US, "%.2g%n", averageRating).trim());
    }

    @SerializedName("commentsNumber")
    private int commentsNumber;

    @SerializedName("reactionsNumber")
    private int reactionsNumber;

    private List<String> moviePosterPaths = new ArrayList<>();

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, moviesInList, averageRating, commentsNumber, reactionsNumber, moviePosterPaths);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieListPreview that = (MovieListPreview) o;
        boolean result = id == that.id && commentsNumber == that.commentsNumber && reactionsNumber == that.reactionsNumber && name.equals(that.name) && type == that.type && moviesInList.equals(that.moviesInList) && moviePosterPaths.equals(that.moviePosterPaths);
        if (result) {
            if ((averageRating == null && that.averageRating != null) || (averageRating != null && that.averageRating == null))
                result = false;
            if (averageRating != null && that.averageRating != null)
                result = averageRating.equals(that.averageRating);
        }
        return result;
    }

}
