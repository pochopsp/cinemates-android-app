package it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.details;

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
public class MovieGenre {
    private int id;
    private String name;
}
