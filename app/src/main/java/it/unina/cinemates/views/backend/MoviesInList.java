package it.unina.cinemates.views.backend;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import it.unina.cinemates.models.Movie;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class MoviesInList {

    @SerializedName("moviesInList")
    private List<Movie> moviesInList;

}
