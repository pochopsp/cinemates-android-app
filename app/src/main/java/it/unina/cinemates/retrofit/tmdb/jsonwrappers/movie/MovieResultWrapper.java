package it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import it.unina.cinemates.views.tmdb.MovieResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MovieResultWrapper {

    @SerializedName("results")
    List<MovieResult> movieResults;
}
