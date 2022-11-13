package it.unina.cinemates.retrofit.backend.jsonwrappers.post;

import java.util.List;

import it.unina.cinemates.models.Movie;
import it.unina.cinemates.views.backend.enums.ListType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MovieListPostJsonWrapper {

    private String name;
    private ListType type = ListType.Custom;
    private String ownerId;
    private List<Movie> moviesInList = null;

    public MovieListPostJsonWrapper(String ownerId, String name) {
        this.ownerId = ownerId;
        this.name = name;
    }

    public MovieListPostJsonWrapper(String ownerId, String name, List<Movie> moviesInList) {
        this.ownerId = ownerId;
        this.name = name;
        this.moviesInList = moviesInList;
    }
}
