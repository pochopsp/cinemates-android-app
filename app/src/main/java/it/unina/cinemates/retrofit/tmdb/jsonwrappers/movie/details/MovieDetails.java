package it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.details;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@NoArgsConstructor
@Getter
@Setter
public class MovieDetails {

    @SerializedName("id")
    Integer id;

    @SerializedName("backdrop_path")
    String backdropPath;

    @SerializedName("poster_path")
    String posterPath;

    @SerializedName("title")
    String title;

    @SerializedName("vote_average")
    Double tmDbRating;

    @SerializedName("runtime")
    Integer runtime;

    @SerializedName("genres")
    List<MovieGenre> genres;

    @SerializedName("videos")
    Videos trailerUrls;

    @SerializedName("release_date")
    String releaseDate;

    @SerializedName("status")
    String movieStatus;

    @SerializedName("budget")
    Long budget;

    @SerializedName("revenue")
    Long revenue;

    @SerializedName("overview")
    String description;

    @SerializedName("credits")
    Credits credits;

}
